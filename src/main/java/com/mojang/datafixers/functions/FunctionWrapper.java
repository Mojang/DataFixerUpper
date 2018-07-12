package com.mojang.datafixers.functions;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.types.DynamicOps;

import java.util.Objects;
import java.util.function.Function;

final class FunctionWrapper<A, B> extends PointFreeFunction<A, B> {
    private final String name;
    protected final Function<DynamicOps<?>, FunctionType<A, B>> fun;

    FunctionWrapper(final String name, final Function<DynamicOps<?>, FunctionType<A, B>> fun) {
        this.name = name;
        this.fun = fun;
    }

    @Override
    public B eval(final DynamicOps<?> ops, final A input) {
        return fun.apply(ops).apply(input);
    }

    @Override
    public Function<DynamicOps<?>, Function<A, B>> eval() {
        return fun::apply;
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
}
