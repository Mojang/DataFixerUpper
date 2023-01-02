// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class Dynamic<T> extends DynamicLike<T> {
    private final T value;

    public Dynamic(final DynamicOps<T> ops) {
        this(ops, ops.empty());
    }

    public Dynamic(final DynamicOps<T> ops, @Nullable final T value) {
        super(ops);
        this.value = value == null ? ops.empty() : value;
    }

    public T getValue() {
        return value;
    }

    public Dynamic<T> map(final Function<? super T, ? extends T> function) {
        return new Dynamic<>(ops, function.apply(value));
    }

    @SuppressWarnings("unchecked")
    public <U> Dynamic<U> castTyped(final DynamicOps<U> ops) {
        if (!Objects.equals(this.ops, ops)) {
            throw new IllegalStateException("Dynamic type doesn't match");
        }
        return (Dynamic<U>) this;
    }

    public <U> U cast(final DynamicOps<U> ops) {
        return castTyped(ops).getValue();
    }

    public OptionalDynamic<T> merge(final Dynamic<?> value) {
        final DataResult<T> merged = ops.mergeToList(this.value, value.cast(ops));
        return new OptionalDynamic<>(ops, merged.map(m -> new Dynamic<>(ops, m)));
    }

    public OptionalDynamic<T> merge(final Dynamic<?> key, final Dynamic<?> value) {
        final DataResult<T> merged = ops.mergeToMap(this.value, key.cast(ops), value.cast(ops));
        return new OptionalDynamic<>(ops, merged.map(m -> new Dynamic<>(ops, m)));
    }

    public DataResult<Map<Dynamic<T>, Dynamic<T>>> getMapValues() {
        return ops.getMapValues(value).map(map -> {
            final ImmutableMap.Builder<Dynamic<T>, Dynamic<T>> builder = ImmutableMap.builder();
            map.forEach(entry -> builder.put(new Dynamic<>(ops, entry.getFirst()), new Dynamic<>(ops, entry.getSecond())));
            return builder.build();
        });
    }

    public Dynamic<T> updateMapValues(final Function<Pair<Dynamic<?>, Dynamic<?>>, Pair<Dynamic<?>, Dynamic<?>>> updater) {
        return DataFixUtils.orElse(getMapValues().map(map -> map.entrySet().stream().map(e -> {
            final Pair<Dynamic<?>, Dynamic<?>> pair = updater.apply(Pair.of(e.getKey(), e.getValue()));
            return Pair.of(pair.getFirst().castTyped(ops), pair.getSecond().castTyped(ops));
        }).collect(Pair.toMap())).map(this::createMap).result(), this);
    }

    @Override
    public DataResult<Number> asNumber() {
        return ops.getNumberValue(value);
    }

    @Override
    public DataResult<String> asString() {
        return ops.getStringValue(value);
    }

    @Override
    public DataResult<Stream<Dynamic<T>>> asStreamOpt() {
        return ops.getStream(value).map(s -> s.map(e -> new Dynamic<>(ops, e)));
    }

    @Override
    public DataResult<Stream<Pair<Dynamic<T>, Dynamic<T>>>> asMapOpt() {
        return ops.getMapValues(value).map(s -> s.map(p -> Pair.of(new Dynamic<>(ops, p.getFirst()), new Dynamic<>(ops, p.getSecond()))));
    }

    @Override
    public DataResult<ByteBuffer> asByteBufferOpt() {
        return ops.getByteBuffer(value);
    }

    @Override
    public DataResult<IntStream> asIntStreamOpt() {
        return ops.getIntStream(value);
    }

    @Override
    public DataResult<LongStream> asLongStreamOpt() {
        return ops.getLongStream(value);
    }

    @Override
    public OptionalDynamic<T> get(final String key) {
        return new OptionalDynamic<>(ops, ops.getMap(value).flatMap(m -> {
            final T value = m.get(key);
            if (value == null) {
                return DataResult.error(() -> "key missing: " + key + " in " + this.value);
            }
            return DataResult.success(new Dynamic<>(ops, value));
        }));
    }

    @Override
    public DataResult<T> getGeneric(final T key) {
        return ops.getGeneric(value, key);
    }

    public Dynamic<T> remove(final String key) {
        return map(v -> ops.remove(v, key));
    }

    public Dynamic<T> set(final String key, final Dynamic<?> value) {
        return map(v -> ops.set(v, key, value.cast(ops)));
    }

    public Dynamic<T> update(final String key, final Function<Dynamic<?>, Dynamic<?>> function) {
        return map(v -> ops.update(v, key, value -> function.apply(new Dynamic<>(ops, value)).cast(ops)));
    }

    public Dynamic<T> updateGeneric(final T key, final Function<T, T> function) {
        return map(v -> ops.updateGeneric(v, key, function));
    }

    @Override
    public DataResult<T> getElement(final String key) {
        return getElementGeneric(ops.createString(key));
    }

    @Override
    public DataResult<T> getElementGeneric(final T key) {
        return ops.getGeneric(value, key);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Dynamic<?> dynamic = (Dynamic<?>) o;
        return Objects.equals(ops, dynamic.ops) && Objects.equals(value, dynamic.value);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + ops.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", ops, value);
    }

    public <R> Dynamic<R> convert(final DynamicOps<R> outOps) {
        return new Dynamic<>(outOps, convert(ops, outOps, value));
    }

    public <V> V into(final Function<? super Dynamic<T>, ? extends V> action) {
        return action.apply(this);
    }

    @Override
    public <A> DataResult<Pair<A, T>> decode(final Decoder<? extends A> decoder) {
        return decoder.decode(ops, value).map(p -> p.mapFirst(Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public static <S, T> T convert(final DynamicOps<S> inOps, final DynamicOps<T> outOps, final S input) {
        if (Objects.equals(inOps, outOps)) {
            return (T) input;
        }

        return inOps.convertTo(outOps, input);
    }
}
