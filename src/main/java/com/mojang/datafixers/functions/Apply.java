// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.functions;

import com.mojang.datafixers.types.Func;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DynamicOps;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

final class Apply<A, B> extends PointFree<B> {
    protected final PointFree<Function<A, B>> func;
    protected final PointFree<A> arg;
    protected final Type<B> type;

    public Apply(final PointFree<Function<A, B>> func, final PointFree<A> arg) {
        this(func, arg, ((Func<A, B>) func.type()).second());
    }

    Apply(final PointFree<Function<A, B>> func, final PointFree<A> arg, final Type<B> type) {
        this.func = func;
        this.arg = arg;
        this.type = type;
    }

    @Override
    public Function<DynamicOps<?>, B> eval() {
        return ops -> func.evalCached().apply(ops).apply(arg.evalCached().apply(ops));
    }

    @Override
    public Type<B> type() {
        return type;
    }

    @Override
    public String toString(final int level) {
        return "(ap " + func.toString(level + 1) + "\n" + indent(level + 1) + arg.toString(level + 1) + "\n" + indent(level) + ")";
    }

    @Override
    public Optional<? extends PointFree<B>> all(final PointFreeRule rule) {
        final PointFree<Function<A, B>> f = rule.rewriteOrNop(func);
        final PointFree<A> a = rule.rewriteOrNop(arg);
        if (f == func && a == arg) {
            return Optional.of(this);
        }
        return Optional.of(new Apply<>(f, a, type));
    }

    @Override
    public Optional<? extends PointFree<B>> one(final PointFreeRule rule) {
        return rule.rewrite(func).map(f -> new Apply<>(f, arg, type))
            .or(() -> rule.rewrite(arg).map(a -> new Apply<>(func, a, type)));
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
        int result = func.hashCode();
        result = 31 * result + arg.hashCode();
        return result;
    }
}
