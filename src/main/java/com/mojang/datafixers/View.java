// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

import com.mojang.datafixers.functions.Functions;
import com.mojang.datafixers.functions.PointFree;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class View<A, B> implements App2<View.Mu, A, B> {
    static final class Mu implements K2 {}

    static <A, B> View<A, B> unbox(final App2<Mu, A, B> box) {
        return (View<A, B>) box;
    }

    public static <A> View<A, A> nopView(final Type<A> type) {
        return create(type, type, Functions.id());
    }

    private final Type<A> type;
    protected final Type<B> newType;
    private final PointFree<Function<A, B>> function;

    public View(final Type<A> type, final Type<B> newType, final PointFree<Function<A, B>> function) {
        this.type = type;
        this.newType = newType;
        this.function = function;
    }

    public Type<A> type() {
        return type;
    }

    public Type<B> newType() {
        return newType;
    }

    public PointFree<Function<A, B>> function() {
        return function;
    }

    public Type<Function<A, B>> getFuncType() {
        return DSL.func(type, newType);
    }

    @Override
    public String toString() {
        return "View[" + function + "," + newType + "]";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final View<?, ?> view = (View<?, ?>) o;
        return Objects.equals(type, view.type) && Objects.equals(newType, view.newType) && Objects.equals(function, view.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, newType, function);
    }

    public Optional<? extends View<A, B>> rewrite(final PointFreeRule rule) {
        return rule.rewrite(DSL.func(type, newType), function()).map(f -> create(type, newType, f));
    }

    public View<A, B> rewriteOrNop(final PointFreeRule rule) {
        return DataFixUtils.orElse(rewrite(rule), this);
    }

    public <C> View<A, C> flatMap(final Function<Type<B>, View<B, C>> function) {
        final View<B, C> instance = function.apply(newType);
        return new View<>(type, instance.newType, Functions.comp(newType, instance.function(), function()));
    }

    public static <A, B> View<A, B> create(final Type<A> type, final Type<B> newType, final PointFree<Function<A, B>> function) {
        return new View<>(type, newType, function);
    }

    public static <A, B> View<A, B> create(final String name, final Type<A> type, final Type<B> newType, final Function<DynamicOps<?>, Function<A, B>> function) {
        return new View<>(type, newType, Functions.fun(name, function));
    }

    @SuppressWarnings("unchecked")
    public <C> View<C, B> compose(final View<C, A> that) {
        if (Objects.equals(function(), Functions.id())) {
            return new View<>(that.type(), newType(), ((View<C, B>) that).function());
        }
        if (Objects.equals(that.function(), Functions.id())) {
            return new View<>(that.type(), newType(), ((View<C, B>) this).function());
        }
        return create(that.type, newType, Functions.comp(that.newType, function(), that.function()));
    }
}
