// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.functions;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DynamicOps;

import java.util.Objects;
import java.util.function.Function;

final class FunctionWrapper<A, B> extends PointFree<Function<A, B>> {
    private final String name;
    protected final Function<DynamicOps<?>, Function<A, B>> fun;
    private final Type<Function<A, B>> type;

    FunctionWrapper(final String name, final Function<DynamicOps<?>, Function<A, B>> fun, final Type<A> input, final Type<B> output) {
        this.name = name;
        this.fun = fun;
        type = DSL.func(input, output);
    }

    @Override
    public Type<Function<A, B>> type() {
        return type;
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
        return Objects.equals(fun, that.fun) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return fun.hashCode();
    }

    @Override
    public Function<DynamicOps<?>, Function<A, B>> eval() {
        return fun;
    }
}
