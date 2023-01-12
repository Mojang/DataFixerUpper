// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.functions;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.Func;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DynamicOps;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

final class Comp<A, B, C> extends PointFree<Function<A, C>> {
    protected final PointFree<Function<B, C>> first;
    protected final PointFree<Function<A, B>> second;
    private final Type<Function<A, C>> type;

    public Comp(final PointFree<Function<B, C>> first, final PointFree<Function<A, B>> second) {
        this(first, second, DSL.func(
            ((Func<A, B>) second.type()).first(),
            ((Func<B, C>) first.type()).second()
        ));
    }

    private Comp(final PointFree<Function<B, C>> first, final PointFree<Function<A, B>> second, final Type<Function<A, C>> type) {
        this.first = first;
        this.second = second;
        this.type = type;
    }

    @Override
    public Type<Function<A, C>> type() {
        return type;
    }

    @Override
    public String toString(final int level) {
        return "(\n" + indent(level + 1) + first.toString(level + 1) + "\n" + indent(level + 1) + "\u25E6\n" + indent(level + 1) + second.toString(level + 1) + "\n" + indent(level) + ")";
    }

    @Override
    public Optional<? extends PointFree<Function<A, C>>> all(final PointFreeRule rule) {
        return Optional.of(new Comp<>(
            rule.rewrite(first).map(f -> (PointFree<Function<B, C>>) f).orElse(first),
            rule.rewrite(second).map(f1 -> (PointFree<Function<A, B>>) f1).orElse(second),
            type
        ));
    }

    @Override
    public Optional<? extends PointFree<Function<A, C>>> one(final PointFreeRule rule) {
        return rule.rewrite(first).map(f -> new Comp<>(f, second, type))
            .or(() -> rule.rewrite(second).map(s -> new Comp<>(first, s, type)));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Comp<?, ?, ?> comp = (Comp<?, ?, ?>) o;
        return Objects.equals(first, comp.first) && Objects.equals(second, comp.second);
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        return result;
    }

    @Override
    public Function<DynamicOps<?>, Function<A, C>> eval() {
        return ops -> input -> {
            final Function<A, B> s = second.evalCached().apply(ops);
            final Function<B, C> f = first.evalCached().apply(ops);
            return f.apply(s.apply(input));
        };
    }
}
