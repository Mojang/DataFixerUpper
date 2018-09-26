// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.functions;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

final class Apply<A, B> extends PointFree<B> {
    protected final PointFree<Function<A, B>> func;
    protected final PointFree<A> arg;
    protected final Type<A> argType;

    public Apply(final PointFree<Function<A, B>> func, final PointFree<A> arg, final Type<A> argType) {
        this.func = func;
        this.arg = arg;
        this.argType = argType;
    }

    @Override
    public Function<DynamicOps<?>, B> eval() {
        return ops -> func.evalCached().apply(ops).apply(arg.evalCached().apply(ops));
    }

    @Override
    public String toString(final int level) {
        return "(ap " + func.toString(level + 1) + "\n" + indent(level + 1) + arg.toString(level + 1) + "\n" + indent(level) + ")";
    }

    @Override
    public Optional<? extends PointFree<B>> all(final PointFreeRule rule, final Type<B> type) {
        return Optional.of(Functions.app(
            rule.rewrite(DSL.func(argType, type), func).map(f1 -> (PointFree<Function<A, B>>) f1).orElse(func),
            rule.rewrite(argType, arg).map(f -> (PointFree<A>) f).orElse(arg),
            argType
        ));
    }

    @Override
    public Optional<? extends PointFree<B>> one(final PointFreeRule rule, final Type<B> type) {
        return rule.rewrite(DSL.func(argType, type), func).map(f -> Optional.of(Functions.app(f, arg, argType)))
            .orElseGet(() -> rule.rewrite(argType, arg).map(a -> Functions.app(func, a, argType)));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Apply<?, ?>)) {
            return false;
        }
        final Apply<?, ?> apply = (Apply<?, ?>) o;
        return Objects.equals(func, apply.func) && Objects.equals(arg, apply.arg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(func, arg);
    }
}
