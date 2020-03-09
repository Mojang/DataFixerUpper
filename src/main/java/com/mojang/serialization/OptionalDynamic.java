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
    private final Optional<Dynamic<T>> delegate;

    public OptionalDynamic(final DynamicOps<T> ops, final Optional<Dynamic<T>> delegate) {
        super(ops);
        this.delegate = delegate;
    }

    public Optional<Dynamic<T>> get() {
        return delegate;
    }

    public <U> Optional<U> map(Function<? super Dynamic<T>, ? extends U> mapper) {
        return delegate.map(mapper);
    }

    public <U> Optional<U> flatMap(Function<? super Dynamic<T>, Optional<U>> mapper) {
        return delegate.flatMap(mapper);
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
        return new OptionalDynamic<>(ops, flatMap(k -> k.get(key).get()));
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
        return delegate.orElseGet(this::emptyMap);
    }

    public Dynamic<T> orElseEmptyList() {
        return delegate.orElseGet(this::emptyList);
    }

    public <V> Optional<V> into(final Function<? super Dynamic<T>, ? extends V> action) {
        return get().map(action);
    }
}
