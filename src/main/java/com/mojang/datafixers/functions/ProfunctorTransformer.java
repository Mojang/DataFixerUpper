// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.functions;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.optics.Optic;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DynamicOps;

import java.util.Objects;
import java.util.function.Function;

final class ProfunctorTransformer<S, T, A, B> extends PointFree<Function<Function<A, B>, Function<S, T>>> {
    protected final Optic<? super FunctionType.Instance.Mu, S, T, A, B> optic;
    protected final Type<Function<Function<A, B>, Function<S, T>>> type;

    public ProfunctorTransformer(final Optic<? super FunctionType.Instance.Mu, S, T, A, B> optic, final Type<Function<Function<A, B>, Function<S, T>>> type) {
        this.optic = optic;
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public <S2, T2> ProfunctorTransformer<S2, T2, A, B> castOuterUnchecked(final Type<Function<Function<A, B>, Function<S2, T2>>> newType) {
        return new ProfunctorTransformer<>((Optic<? super FunctionType.Instance.Mu, S2, T2, A, B>) optic, newType);
    }

    @Override
    public Type<Function<Function<A, B>, Function<S, T>>> type() {
        return type;
    }

    @Override
    public String toString(final int level) {
        return "Optic[" + optic + "]";
    }

    @Override
    public Function<DynamicOps<?>, Function<Function<A, B>, Function<S, T>>> eval() {
        final Function<App2<FunctionType.Mu, A, B>, App2<FunctionType.Mu, S, T>> func = optic.eval(FunctionType.Instance.INSTANCE);
        final Function<Function<A, B>, Function<S, T>> unwrappedFunction = input -> FunctionType.unbox(func.apply(FunctionType.create(input)));
        return ops -> unwrappedFunction;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ProfunctorTransformer<?, ?, ?, ?> that = (ProfunctorTransformer<?, ?, ?, ?>) o;
        return Objects.equals(optic, that.optic);
    }

    @Override
    public int hashCode() {
        return optic.hashCode();
    }
}
