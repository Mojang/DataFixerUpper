// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

public class DataFixerBuilder {
    private static final Logger LOGGER = LogManager.getLogger();

    private final int dataVersion;
    private final Int2ObjectSortedMap<Schema> schemas = new Int2ObjectAVLTreeMap<>();
    private final List<DataFix> globalList = Lists.newArrayList();
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

    public DataFixer build(final Executor executor) {
        final DataFixerUpper fixerUpper = new DataFixerUpper(new Int2ObjectAVLTreeMap<>(schemas), new ArrayList<>(globalList), new IntAVLTreeSet(fixerVersions));

        executor.execute(() -> {
            List<Runnable> allTasks = new ArrayList<>();
            final IntBidirectionalIterator iterator = fixerUpper.fixerVersions().iterator();
            while (iterator.hasNext()) {
                final int versionKey = iterator.nextInt();
                final Schema schema = schemas.get(versionKey);
                for (final String typeName: schema.types()) {
                    allTasks.add(() -> {
                        final Type<?> dataType = schema.getType(() -> typeName);
                        final TypeRewriteRule rule = fixerUpper.getRule(DataFixUtils.getVersion(versionKey), dataVersion);
                        dataType.rewrite(rule, DataFixerUpper.OPTIMIZATION_RULE);
                    });
                }
            }

            // Divide up into sets of tasks by number of CPU cores
            // Some tasks are faster than others, randomize it to try to divide it more
            Collections.shuffle(allTasks);
            List<List<Runnable>> queueList = new ArrayList<>();
            List<Runnable> current = new ArrayList<>();
            queueList.add(current);
            int maxTasks = (int) Math.max(1, Math.floor(allTasks.size() / (float)Math.min(6, Runtime.getRuntime().availableProcessors()-2)));
            for (Runnable task : allTasks) {
                if (current.size() >= maxTasks) {
                    current = new ArrayList<>();
                    queueList.add(current);
                }
                current.add(task);
            }

            queueList.forEach(queue -> executor.execute(() -> queue.forEach(Runnable::run)));
        });

        return fixerUpper;
    }
}
