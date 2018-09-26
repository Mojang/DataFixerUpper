// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.functions;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.optics.Optic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.Algebra;
import com.mojang.datafixers.types.templates.RecursivePoint;

import java.util.Objects;
import java.util.function.Function;

public abstract class Functions {
    private static final Id<?> ID = new Id<>();

    @SuppressWarnings("unchecked")
    public static <A, B, C> PointFree<Function<A, C>> comp(final Type<B> middleType, final PointFree<Function<B, C>> f1, final PointFree<Function<A, B>> f2) {
        if (Objects.equals(f1, id())) {
            return (PointFree<Function<A, C>>) (PointFree<?>) f2;
        }
        if (Objects.equals(f2, id())) {
            return (PointFree<Function<A, C>>) (PointFree<?>) f1;
        }
        return new Comp<>(middleType, f1, f2);
    }

    public static <A, B> PointFree<Function<A, B>> fun(final String name, final Function<DynamicOps<?>, Function<A, B>> fun) {
        return new FunctionWrapper<>(name, fun);
    }

    public static <A, B> PointFree<B> app(final PointFree<Function<A, B>> fun, final PointFree<A> arg, final Type<A> argType) {
        return new Apply<>(fun, arg, argType);
    }

    public static <S, T, A, B> PointFree<Function<Function<A, B>, Function<S, T>>> profunctorTransformer(final Optic<? super FunctionType.Instance.Mu, S, T, A, B> lens) {
        return new ProfunctorTransformer<>(lens);
    }

    public static <A> Bang<A> bang() {
        return new Bang<>();
    }

    public static <A> PointFree<Function<A, A>> in(final RecursivePoint.RecursivePointType<A> type) {
        return new In<>(type);
    }

    public static <A> PointFree<Function<A, A>> out(final RecursivePoint.RecursivePointType<A> type) {
        return new Out<>(type);
    }

    public static <A, B> PointFree<Function<A, B>> fold(final RecursivePoint.RecursivePointType<A> aType, final RewriteResult<?, B> function, final Algebra algebra, final int index) {
        return new Fold<>(aType, function, algebra, index);
    }

    @SuppressWarnings("unchecked")
    public static <A> PointFree<Function<A, A>> id() {
        return (Id<A>) ID;
    }
}
