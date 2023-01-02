// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

import com.mojang.datafixers.functions.Functions;
import com.mojang.datafixers.functions.PointFree;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DynamicOps;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public record View<A, B>(Type<A> type, Type<B> newType, PointFree<Function<A, B>> function) implements App2<View.Mu, A, B> {
    static final class Mu implements K2 {}

    static <A, B> View<A, B> unbox(final App2<Mu, A, B> box) {
        return (View<A, B>) box;
    }

    public static <A> View<A, A> nopView(final Type<A> type) {
        return create(type, type, Functions.id());
    }

    public Type<Function<A, B>> funcType() {
        return DSL.func(type, newType);
    }

    @Override
    public String toString() {
        return "View[" + function + "," + newType + "]";
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
        if (Functions.isId(function())) {
            return new View<>(that.type(), newType(), ((View<C, B>) that).function());
        }
        if (Functions.isId(that.function())) {
            return new View<>(that.type(), newType(), ((View<C, B>) this).function());
        }
        return create(that.type, newType, Functions.comp(that.newType, function(), that.function()));
    }
}
