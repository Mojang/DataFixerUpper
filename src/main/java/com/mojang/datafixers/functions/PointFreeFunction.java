package com.mojang.datafixers.functions;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.types.DynamicOps;

import java.util.function.Function;

abstract class PointFreeFunction<A, B> extends PointFree<App2<FunctionType.Mu, A, B>> {
    abstract B eval(final DynamicOps<?> ops, final A input);

    @Override
    public Function<DynamicOps<?>, App2<FunctionType.Mu, A, B>> eval() {
        return ops -> Optics.func(a -> eval(ops, a));
    }
}
