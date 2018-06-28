package com.mojang.datafixers.types;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Objects;
import java.util.Optional;

public final class Func<A, B> extends Type<App2<FunctionType.Mu, A, B>> {
    protected final Type<A> first;
    protected final Type<B> second;

    public Func(final Type<A> first, final Type<B> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public TypeTemplate buildTemplate() {
        throw new UnsupportedOperationException("No template for function types.");
    }

    @Override
    public <T> Pair<T, Optional<App2<FunctionType.Mu, A, B>>> read(final DynamicOps<T> ops, final T input) {
        return Pair.of(input, Optional.empty());
    }

    @Override
    public <T> T write(final DynamicOps<T> ops, final T rest, final App2<FunctionType.Mu, A, B> value) {
        return rest;
    }

    @Override
    public String toString() {
        return "(" + first + " -> " + second + ")";
    }

    @Override
    public boolean equals(final Object obj, final boolean ignoreRecursionPoints) {
        if (!(obj instanceof com.mojang.datafixers.types.Func<?, ?>)) {
            return false;
        }
        final com.mojang.datafixers.types.Func<?, ?> that = (com.mojang.datafixers.types.Func<?, ?>) obj;
        return first.equals(that.first, ignoreRecursionPoints) && second.equals(that.second, ignoreRecursionPoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    public Type<A> first() {
        return first;
    }

    public Type<B> second() {
        return second;
    }
}
