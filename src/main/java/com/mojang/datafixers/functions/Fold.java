package com.mojang.datafixers.functions;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.View;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.families.Algebra;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.templates.RecursivePoint;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

final class Fold<A, B> extends PointFree<App2<FunctionType.Mu, A, B>> {
    private static final Map<Pair<RecursiveTypeFamily, Algebra>, IntFunction<RewriteResult<?, ?>>> HMAP_CACHE = Maps.newConcurrentMap();
    private static final Map<Pair<IntFunction<RewriteResult<?, ?>>, Integer>, RewriteResult<?, ?>> HMAP_APPLY_CACHE = Maps.newConcurrentMap();

    protected final RecursivePoint.RecursivePointType<A> aType;
    protected final RewriteResult<?, B> function;
    protected final Algebra algebra;
    protected final int index;

    public Fold(final RecursivePoint.RecursivePointType<A> aType, final RewriteResult<?, B> function, final Algebra algebra, final int index) {
        this.aType = aType;
        this.function = function;
        this.algebra = algebra;
        this.index = index;
    }

    private <FB> View<A, B> cap(final RewriteResult<?, B> op, final RewriteResult<?, FB> resResult) {
        return RecursiveTypeFamily.viewUnchecked(resResult.view().type(), op.view().newType(), Functions.comp(resResult.view().newType(), ((View<FB, B>) op.view()).function(), ((View<A, FB>) resResult.view()).function()));
    }

    @Override
    public Function<DynamicOps<?>, App2<FunctionType.Mu, A, B>> eval() {
        return ops -> Optics.func(a -> {
            final RecursiveTypeFamily family = aType.family();

            final IntFunction<RewriteResult<?, ?>> hmapped = HMAP_CACHE.computeIfAbsent(Pair.of(family, algebra), key -> key.getFirst().template().hmap(key.getFirst(), key.getFirst().fold(key.getSecond())));
            final RewriteResult<?, ?> result = HMAP_APPLY_CACHE.computeIfAbsent(Pair.of(hmapped, index), key -> key.getFirst().apply(key.getSecond()));

            final App2<FunctionType.Mu, A, B> eval = cap(function, result).function().eval().apply(ops);
            return Optics.getFunc(eval).apply(a);
        });
    }

    @Override
    protected String toString(final int level) {
        return "fold(" + aType + ", " + index + ", \n" + indent(level + 1) + function.view().function().toString(level + 1) + "\n" + indent(level) + ")";
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
        return Objects.hash(aType, algebra);
    }
}
