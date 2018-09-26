// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.functions;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.optics.Optic;
import com.mojang.datafixers.types.DynamicOps;

import java.util.Objects;
import java.util.function.Function;

final class ProfunctorTransformer<S, T, A, B> extends PointFree<Function<Function<A, B>, Function<S, T>>> {
    protected final Optic<? super FunctionType.Instance.Mu, S, T, A, B> optic;
    protected final Function<App2<FunctionType.Mu, A, B>, App2<FunctionType.Mu, S, T>> func;
    private final Function<Function<A, B>, Function<S, T>> unwrappedFunction;

    public ProfunctorTransformer(final Optic<? super FunctionType.Instance.Mu, S, T, A, B> optic) {
        this.optic = optic;
        func = optic.eval(FunctionType.Instance.INSTANCE);
        unwrappedFunction = input -> FunctionType.unbox(func.apply(FunctionType.create(input)));
    }

    @Override
    public String toString(final int level) {
        return "Optic[" + optic + "]";
    }

    @Override
    public Function<DynamicOps<?>, Function<Function<A, B>, Function<S, T>>> eval() {
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
        return Objects.hash(optic);
    }
}
