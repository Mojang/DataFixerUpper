// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.functions;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.optics.Optic;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.Algebra;
import com.mojang.datafixers.types.templates.RecursivePoint;
import com.mojang.serialization.DynamicOps;

import java.util.function.Function;

public abstract class Functions {
    @SuppressWarnings("unchecked")
    public static <A, B, C> PointFree<Function<A, C>> comp(final PointFree<Function<B, C>> f1, final PointFree<Function<A, B>> f2) {
        if (Functions.isId(f1)) {
            return (PointFree<Function<A, C>>) (PointFree<?>) f2;
        }
        if (Functions.isId(f2)) {
            return (PointFree<Function<A, C>>) (PointFree<?>) f1;
        }
        return new Comp<>(f1, f2);
    }

    public static <A, B> PointFree<Function<A, B>> fun(final String name, final Function<DynamicOps<?>, Function<A, B>> fun, final Type<A> input, final Type<B> output) {
        return new FunctionWrapper<>(name, fun, input, output);
    }

    public static <A, B> PointFree<B> app(final PointFree<Function<A, B>> fun, final PointFree<A> arg) {
        return new Apply<>(fun, arg);
    }

    public static <S, T, A, B> PointFree<Function<Function<A, B>, Function<S, T>>> profunctorTransformer(final Optic<? super FunctionType.Instance.Mu, S, T, A, B> lens, final Type<Function<Function<A, B>, Function<S, T>>> type) {
        return new ProfunctorTransformer<>(lens, type);
    }

    public static <A> Bang<A> bang(final Type<A> type) {
        return new Bang<>(type);
    }

    public static <A> PointFree<Function<A, A>> in(final RecursivePoint.RecursivePointType<A> type) {
        return new In<>(type);
    }

    public static <A> PointFree<Function<A, A>> out(final RecursivePoint.RecursivePointType<A> type) {
        return new Out<>(type);
    }

    public static <A, B> PointFree<Function<A, B>> fold(final RecursivePoint.RecursivePointType<A> aType, final RecursivePoint.RecursivePointType<B> bType, final RewriteResult<A, B> function, final Algebra algebra, final int index) {
        return new Fold<>(aType, bType, function, algebra, index);
    }

    public static <A> PointFree<Function<A, A>> id(final Type<A> type) {
        return new Id<>(DSL.func(type, type));
    }

    public static boolean isId(final PointFree<?> function) {
        return function instanceof Id<?>;
    }
}
