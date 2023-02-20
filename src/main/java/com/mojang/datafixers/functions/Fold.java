// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.functions;

import com.google.common.collect.Maps;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.View;
import com.mojang.datafixers.types.families.Algebra;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.templates.RecursivePoint;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

final class Fold<A, B> extends PointFree<Function<A, B>> {
    private static final Map<Triple<RecursiveTypeFamily, RecursiveTypeFamily, Algebra>, IntFunction<RewriteResult<?, ?>>> HMAP_CACHE = Maps.newConcurrentMap();
    private static final Map<Pair<IntFunction<RewriteResult<?, ?>>, Integer>, RewriteResult<?, ?>> HMAP_APPLY_CACHE = Maps.newConcurrentMap();

    protected final RecursivePoint.RecursivePointType<A> aType;
    protected final RecursivePoint.RecursivePointType<B> bType;
    protected final RewriteResult<A, B> function;
    protected final Algebra algebra;
    protected final int index;

    public Fold(final RecursivePoint.RecursivePointType<A> aType, final RecursivePoint.RecursivePointType<B> bType, final RewriteResult<A, B> function, final Algebra algebra, final int index) {
        this.aType = aType;
        this.bType = bType;
        this.function = function;
        this.algebra = algebra;
        this.index = index;
    }

    private <FB> PointFree<Function<A, B>> cap(final RewriteResult<?, B> op, final RewriteResult<?, FB> resResult) {
        return Functions.comp(resResult.view().newType(), ((View<FB, B>) op.view()).function(), ((View<A, FB>) resResult.view()).function());
    }

    @Override
    public Function<DynamicOps<?>, Function<A, B>> eval() {
        return ops -> a -> {
            final RecursiveTypeFamily family = aType.family();
            final RecursiveTypeFamily newFamily = bType.family();

            final IntFunction<RewriteResult<?, ?>> hmapped = HMAP_CACHE.computeIfAbsent(Triple.of(family, newFamily, algebra), key -> key.getLeft().template().hmap(key.getLeft(), key.getLeft().fold(key.getRight(), key.getMiddle())));
            final RewriteResult<?, ?> result = HMAP_APPLY_CACHE.computeIfAbsent(Pair.of(hmapped, index), key -> key.getFirst().apply(key.getSecond()));

            final PointFree<Function<A, B>> eval = cap(function, result);
            return eval.evalCached().apply(ops).apply(a);
        };
    }

    @Override
    public String toString(final int level) {
        return "fold(" + aType + ", " + index + ", \n" + indent(level + 1) + algebra.toString(level + 1) + "\n" + indent(level) + ")";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Fold<?, ?> fold = (Fold<?, ?>) o;
        return Objects.equals(aType, fold.aType) && Objects.equals(algebra, fold.algebra);
    }

    @Override
    public int hashCode() {
        int result = aType.hashCode();
        result = 31 * result + algebra.hashCode();
        return result;
    }
}
