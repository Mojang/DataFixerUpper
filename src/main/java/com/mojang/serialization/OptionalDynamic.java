// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
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

    public <U> Optional<U> flatMap(final Function<? super Dynamic<T>, Optional<U>> mapper) {
        return result().flatMap(mapper);
    }

    @Override
    public Optional<Number> asNumber() {
        return flatMap(DynamicLike::asNumber);
    }

    @Override
    public Optional<String> asString() {
        return flatMap(DynamicLike::asString);
    }

    @Override
    public Optional<Stream<Dynamic<T>>> asStreamOpt() {
        return flatMap(DynamicLike::asStreamOpt);
    }

    @Override
    public Optional<ByteBuffer> asByteBufferOpt() {
        return flatMap(DynamicLike::asByteBufferOpt);
    }

    @Override
    public Optional<IntStream> asIntStreamOpt() {
        return flatMap(DynamicLike::asIntStreamOpt);
    }

    @Override
    public Optional<LongStream> asLongStreamOpt() {
        return flatMap(DynamicLike::asLongStreamOpt);
    }

    @Override
    public OptionalDynamic<T> get(final String key) {
        return new OptionalDynamic<>(ops, delegate.flatMap(k -> k.get(key).delegate));
    }

    @Override
    public Optional<T> getGeneric(final T key) {
        return flatMap(v -> v.getGeneric(key));
    }

    @Override
    public Optional<T> getElement(final String key) {
        return flatMap(v -> v.getElement(key));
    }

    @Override
    public Optional<T> getElementGeneric(final T key) {
        return flatMap(v -> v.getElementGeneric(key));
    }

    @Override
    public <U> Optional<List<U>> asListOpt(final Function<Dynamic<T>, U> deserializer) {
        return flatMap(t -> t.asListOpt(deserializer));
    }

    @Override
    public <K, V> Optional<Map<K, V>> asMapOpt(final Function<Dynamic<T>, K> keyDeserializer, final Function<Dynamic<T>, V> valueDeserializer) {
        return flatMap(input -> input.asMapOpt(keyDeserializer, valueDeserializer));
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
}
