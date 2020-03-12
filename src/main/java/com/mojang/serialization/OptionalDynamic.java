// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.util.Pair;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public final class OptionalDynamic<T> extends DynamicLike<T> {
    private final DataResult<Dynamic<T>> delegate;

    public OptionalDynamic(final DynamicOps<T> ops, final DataResult<Dynamic<T>> delegate) {
        super(ops);
        this.delegate = delegate;
    }

    public Optional<Dynamic<T>> result() {
        return delegate.result();
    }

    public <U> DataResult<U> map(final Function<? super Dynamic<T>, U> mapper) {
        return delegate.map(mapper);
    }

    public <U> Optional<U> flatMapOpt(final Function<? super Dynamic<T>, Optional<U>> mapper) {
        return result().flatMap(mapper);
    }

    public <U> DataResult<U> flatMap(final Function<? super Dynamic<T>, ? extends DataResult<U>> mapper) {
        return delegate.flatMap(mapper);
    }

    @Override
    public Optional<Number> asNumber() {
        return flatMapOpt(DynamicLike::asNumber);
    }

    @Override
    public Optional<String> asString() {
        return flatMapOpt(DynamicLike::asString);
    }

    @Override
    public Optional<Stream<Dynamic<T>>> asStreamOpt() {
        return flatMapOpt(DynamicLike::asStreamOpt);
    }

    @Override
    public Optional<Stream<Pair<Dynamic<T>, Dynamic<T>>>> asMapOpt() {
        return flatMapOpt(DynamicLike::asMapOpt);
    }

    @Override
    public Optional<ByteBuffer> asByteBufferOpt() {
        return flatMapOpt(DynamicLike::asByteBufferOpt);
    }

    @Override
    public Optional<IntStream> asIntStreamOpt() {
        return flatMapOpt(DynamicLike::asIntStreamOpt);
    }

    @Override
    public Optional<LongStream> asLongStreamOpt() {
        return flatMapOpt(DynamicLike::asLongStreamOpt);
    }

    @Override
    public OptionalDynamic<T> get(final String key) {
        return new OptionalDynamic<>(ops, delegate.flatMap(k -> k.get(key).delegate));
    }

    @Override
    public Optional<T> getGeneric(final T key) {
        return flatMapOpt(v -> v.getGeneric(key));
    }

    @Override
    public Optional<T> getElement(final String key) {
        return flatMapOpt(v -> v.getElement(key));
    }

    @Override
    public Optional<T> getElementGeneric(final T key) {
        return flatMapOpt(v -> v.getElementGeneric(key));
    }

    public Dynamic<T> orElseEmptyMap() {
        return result().orElseGet(this::emptyMap);
    }

    public Dynamic<T> orElseEmptyList() {
        return result().orElseGet(this::emptyList);
    }

    public <V> Optional<V> into(final Function<? super Dynamic<T>, ? extends V> action) {
        return result().map(action);
    }

    @Override
    public <A> DataResult<Pair<A, T>> decode(final Decoder<? extends A> decoder) {
        return delegate.flatMap(t -> t.decode(decoder));
    }
}
