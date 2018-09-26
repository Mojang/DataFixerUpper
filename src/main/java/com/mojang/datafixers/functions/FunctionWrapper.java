// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.functions;

import com.mojang.datafixers.types.DynamicOps;

import java.util.Objects;
import java.util.function.Function;

final class FunctionWrapper<A, B> extends PointFree<Function<A, B>> {
    private final String name;
    protected final Function<DynamicOps<?>, Function<A, B>> fun;

    FunctionWrapper(final String name, final Function<DynamicOps<?>, Function<A, B>> fun) {
        this.name = name;
        this.fun = fun;
    }

    @Override
    public String toString(final int level) {
        return "fun[" + name + "]";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FunctionWrapper<?, ?> that = (FunctionWrapper<?, ?>) o;
        return Objects.equals(fun, that.fun);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fun);
    }

    @Override
    public Function<DynamicOps<?>, Function<A, B>> eval() {
        return fun;
    }
}
