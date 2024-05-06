// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class DataFixerBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataFixerBuilder.class);

    private final int dataVersion;
    private final Int2ObjectSortedMap<Schema> schemas = new Int2ObjectAVLTreeMap<>();
    private final List<DataFix> globalList = new ArrayList<>();
    private final IntSortedSet fixerVersions = new IntAVLTreeSet();

    public DataFixerBuilder(final int dataVersion) {
        this.dataVersion = dataVersion;
    }

    public Schema addSchema(final int version, final BiFunction<Integer, Schema, Schema> factory) {
        return addSchema(version, 0, factory);
    }

    public Schema addSchema(final int version, final int subVersion, final BiFunction<Integer, Schema, Schema> factory) {
        final int key = DataFixUtils.makeKey(version, subVersion);
        final Schema parent = schemas.isEmpty() ? null : schemas.get(DataFixerUpper.getLowestSchemaSameVersion(schemas, key - 1));
        final Schema schema = factory.apply(DataFixUtils.makeKey(version, subVersion), parent);
        addSchema(schema);
        return schema;
    }

    public void addSchema(final Schema schema) {
        schemas.put(schema.getVersionKey(), schema);
    }

    public void addFixer(final DataFix fix) {
        final int version = DataFixUtils.getVersion(fix.getVersionKey());

        if (version > dataVersion) {
            LOGGER.warn("Ignored fix registered for version: {} as the DataVersion of the game is: {}", version, dataVersion);
            return;
        }

        globalList.add(fix);
        fixerVersions.add(fix.getVersionKey());
    }

    public Result build() {
        final DataFixerUpper fixer = new DataFixerUpper(new Int2ObjectAVLTreeMap<>(schemas), new ArrayList<>(globalList), new IntAVLTreeSet(fixerVersions));
        return new Result(fixer);
    }

    public class Result {
        private final DataFixerUpper fixerUpper;

        public Result(final DataFixerUpper fixerUpper) {
            this.fixerUpper = fixerUpper;
        }

        public DataFixer fixer() {
            return fixerUpper;
        }

        public CompletableFuture<?> optimize(final Set<DSL.TypeReference> requiredTypes, final Executor executor) {
            final Instant started = Instant.now();
            final List<CompletableFuture<?>> doneFutures = new ArrayList<>();
            final List<CompletableFuture<?>> failFutures = new ArrayList<>();

            final Set<String> requiredTypeNames = requiredTypes.stream().map(DSL.TypeReference::typeName).collect(Collectors.toSet());

            final IntIterator iterator = fixerUpper.fixerVersions().iterator();
            while (iterator.hasNext()) {
                final int versionKey = iterator.nextInt();
                final Schema schema = schemas.get(versionKey);
                for (final String typeName : schema.types()) {
                    if (!requiredTypeNames.contains(typeName)) {
                        continue;
                    }
                    final CompletableFuture<Void> doneFuture = CompletableFuture.runAsync(() -> {
                        final Type<?> dataType = schema.getType(() -> typeName);
                        final TypeRewriteRule rule = fixerUpper.getRule(DataFixUtils.getVersion(versionKey), dataVersion);
                        dataType.rewrite(rule, DataFixerUpper.OPTIMIZATION_RULE);
                    }, executor);
                    doneFutures.add(doneFuture);

                    final CompletableFuture<?> failFuture = new CompletableFuture<>();
                    doneFuture.exceptionally(e -> {
                        failFuture.completeExceptionally(e);
                        return null;
                    });
                    failFutures.add(failFuture);
                }
            }

            final CompletableFuture<?> doneFuture = CompletableFuture.allOf(doneFutures.toArray(CompletableFuture[]::new)).thenAccept(ignored -> {
                LOGGER.info("{} Datafixer optimizations took {} milliseconds", doneFutures.size(), Duration.between(started, Instant.now()).toMillis());
            });

            final CompletableFuture<?> failFuture = CompletableFuture.anyOf(failFutures.toArray(CompletableFuture[]::new));

            return CompletableFuture.anyOf(doneFuture, failFuture);
        }
    }
}
