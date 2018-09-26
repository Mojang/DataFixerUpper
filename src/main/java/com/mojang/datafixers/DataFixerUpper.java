// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/*
 * Optimizing functions
 *   Cunha, A., & Pinto, J. S. (2005). Point-free program transformation
 *   Lämmel, R., Visser, E., & Visser, J. (2002). The essence of strategic programming
 *
 * How to handle recursive types
 *   Cunha, A., & Pacheco, H. (2011). Algebraic specialization of generic functions for recursive types
 *   Yakushev, A. R., Holdermans, S., Löh, A., & Jeuring, J. (2009, August). Generic programming with fixed points for mutually recursive datatypes
 *   Magalhães, J. P., & Löh, A. (2012). A formal comparison of approaches to datatype-generic programming
 *
 * Optics
 *   Pickering, M., Gibbons, J., & Wu, N. (2017). Profunctor Optics: Modular Data Accessors
 *   Pacheco, H., & Cunha, A. (2010, June). Generic point-free lenses
 *
 * Tying it together
 *   Cunha, A., Oliveira, J. N., & Visser, J. (2006, August). Type-safe two-level data transformation
 *   Cunha, A., & Visser, J. (2011). Transformation of structure-shy programs with application to XPath queries and strategic functions
 *   Pacheco, H., & Cunha, A. (2011, January). Calculating with lenses: optimising bidirectional transformations
 */
public class DataFixerUpper implements DataFixer {
    public static boolean ERRORS_ARE_FATAL = false;

    private static final Logger LOGGER = LogManager.getLogger();

    protected static final PointFreeRule OPTIMIZATION_RULE = DataFixUtils.make(() -> {
        final PointFreeRule opSimple = PointFreeRule.orElse(
            PointFreeRule.orElse(
                PointFreeRule.CataFuseSame.INSTANCE,
                PointFreeRule.orElse(
                    PointFreeRule.CataFuseDifferent.INSTANCE,
                    PointFreeRule.LensAppId.INSTANCE
                )
            ),
            PointFreeRule.orElse(
                PointFreeRule.LensComp.INSTANCE,
                PointFreeRule.orElse(
                    PointFreeRule.AppNest.INSTANCE,
                    PointFreeRule.LensCompFunc.INSTANCE
                )
            )
        );
        final PointFreeRule opLeft = PointFreeRule.many(PointFreeRule.once(PointFreeRule.orElse(opSimple, PointFreeRule.CompAssocLeft.INSTANCE)));
        final PointFreeRule opComp = PointFreeRule.many(PointFreeRule.once(PointFreeRule.orElse(PointFreeRule.SortInj.INSTANCE, PointFreeRule.SortProj.INSTANCE)));
        final PointFreeRule opRight = PointFreeRule.many(PointFreeRule.once(PointFreeRule.orElse(opSimple, PointFreeRule.CompAssocRight.INSTANCE)));
        return PointFreeRule.seq(ImmutableList.of(() -> opLeft, () -> opComp, () -> opRight, () -> opLeft, () -> opRight));
    });

    private final Int2ObjectSortedMap<Schema> schemas;
    private final List<DataFix> globalList;
    private final IntSortedSet fixerVersions;
    private final Long2ObjectMap<TypeRewriteRule> rules = new Long2ObjectOpenHashMap<>();

    protected DataFixerUpper(final Int2ObjectSortedMap<Schema> schemas, final List<DataFix> globalList, final IntSortedSet fixerVersions) {
        this.schemas = schemas;
        this.globalList = globalList;
        this.fixerVersions = fixerVersions;
    }

    @Override
    public <T> Dynamic<T> update(final DSL.TypeReference type, final Dynamic<T> input, final int version, final int newVersion) {
        try {
            if (version < newVersion) {
                final Type<?> dataType = getType(type, version);
                final Optional<T> read = dataType.readAndWrite(input.getOps(), getType(type, newVersion), getRule(version, newVersion), OPTIMIZATION_RULE, input.getValue());
                if (!read.isPresent()) {
                    throw new IllegalStateException("Could not parse for fixing " + dataType);
                }

                return new Dynamic<>(input.getOps(), read.get());
            }
        } catch (final Throwable t) {
            LOGGER.error("Something went wrong upgrading!", t);
        }
        return input;
    }

    @Override
    public Schema getSchema(final int key) {
        return schemas.get(getLowestSchemaSameVersion(schemas, key));
    }

    protected Type<?> getType(final DSL.TypeReference type, final int version) {
        return getSchema(DataFixUtils.makeKey(version)).getType(type);
    }

    protected static int getLowestSchemaSameVersion(final Int2ObjectSortedMap<Schema> schemas, final int versionKey) {
        if (versionKey < schemas.firstIntKey()) {
            // can't have a data type before anything else
            return schemas.firstIntKey();
        }
        return schemas.subMap(0, versionKey + 1).lastIntKey();
    }

    private int getLowestFixSameVersion(final int versionKey) {
        if (versionKey < fixerVersions.firstInt()) {
            // can have a version before everything else
            return fixerVersions.firstInt() - 1;
        }
        return fixerVersions.subSet(0, versionKey + 1).lastInt();
    }

    protected TypeRewriteRule getRule(final int version, final int dataVersion) {
        if (version >= dataVersion) {
            return TypeRewriteRule.nop();
        }

        final int expandedVersion = getLowestFixSameVersion(DataFixUtils.makeKey(version));
        final int expandedDataVersion = DataFixUtils.makeKey(dataVersion);

        final long key = (long) expandedVersion << 32 | expandedDataVersion;
        if (!rules.containsKey(key)) {
            final List<TypeRewriteRule> rules = Lists.newArrayList();
            for (final DataFix fix : globalList) {
                final int fixVersion = fix.getVersionKey();
                if (fixVersion > expandedVersion && fixVersion <= expandedDataVersion) {
                    final TypeRewriteRule fixRule = fix.getRule();
                    if (fixRule == TypeRewriteRule.nop()) {
                        continue;
                    }
                    rules.add(fixRule);
                }
            }
            this.rules.put(key, TypeRewriteRule.seq(rules));
        }
        return rules.get(key);
    }

    protected IntSortedSet fixerVersions() {
        return fixerVersions;
    }
}
