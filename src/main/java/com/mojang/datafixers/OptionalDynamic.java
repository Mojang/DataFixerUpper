package com.mojang.datafixers;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class OptionalDynamic<T> {
    private final Optional<Dynamic<T>> delegate;

    public OptionalDynamic(final Optional<Dynamic<T>> delegate) {
        this.delegate = delegate;
    }

    public Optional<Dynamic<T>> get() {
        return delegate;
    }

    public <U> U as(final Function<Dynamic<T>, U> deserializer, final U def) {
        return delegate.map(deserializer).orElse(def);
    }

    public <U> U asOpt(final Function<Dynamic<T>, Optional<U>> deserializer, final U def) {
        return delegate.flatMap(deserializer).orElse(def);
    }

    public <U> List<U> toList(final Function<Dynamic<T>, U> deserializer) {
        return as(t -> t.toList(deserializer), ImmutableList.of());
    }
}
