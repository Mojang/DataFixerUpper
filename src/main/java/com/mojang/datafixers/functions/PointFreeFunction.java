package com.mojang.datafixers.functions;

import com.mojang.datafixers.types.DynamicOps;

import java.util.function.Function;

abstract class PointFreeFunction<A, B> extends PointFree<Function<A, B>> {
    abstract B eval(final DynamicOps<?> ops, final A input);

    @Override
    public Function<DynamicOps<?>, Function<A, B>> eval() {
        return ops -> a -> eval(ops, a);
    }
}
