// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
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
        final DataResult<T> merged = ops.mergeInto(this.value, value.cast(ops));
        return new OptionalDynamic<>(ops, merged.map(m -> new Dynamic<>(ops, m)));
    }

    public OptionalDynamic<T> merge(final Dynamic<?> key, final Dynamic<?> value) {
        final DataResult<T> merged = ops.mergeInto(this.value, key.cast(ops), value.cast(ops));
        return new OptionalDynamic<>(ops, merged.map(m -> new Dynamic<>(ops, m)));
    }

    public Optional<Map<Dynamic<T>, Dynamic<T>>> getMapValues() {
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
        }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))).map(this::createMap), this);
    }

    @Override
    public Optional<Number> asNumber() {
        return ops.getNumberValue(value);
    }

    @Override
    public Optional<String> asString() {
        return ops.getStringValue(value);
    }

    @Override
    public Optional<Stream<Dynamic<T>>> asStreamOpt() {
        return ops.getStream(value).map(s -> s.map(e -> new Dynamic<>(ops, e)));
    }

    @Override
    public Optional<ByteBuffer> asByteBufferOpt() {
        return ops.getByteBuffer(value);
    }

    @Override
    public Optional<IntStream> asIntStreamOpt() {
        return ops.getIntStream(value);
    }

    @Override
    public Optional<LongStream> asLongStreamOpt() {
        return ops.getLongStream(value);
    }

    @Override
    public OptionalDynamic<T> get(final String key) {
        final Optional<Stream<Pair<T, T>>> map = ops.getMapValues(value);
        if (!map.isPresent()) {
            return new OptionalDynamic<>(ops, DataResult.error("not a map: " + value));
        }
        final T keyString = ops.createString(key);
        final Optional<T> value = map.get().filter(p -> Objects.equals(keyString, p.getFirst())).map(Pair::getSecond).findFirst();
        if (!value.isPresent()) {
            return new OptionalDynamic<>(ops, DataResult.error("key missing: " + key + " in " + this.value));
        }
        return new OptionalDynamic<T>(ops, DataResult.success(new Dynamic<>(ops, value.get())));
    }

    @Override
    public Optional<T> getGeneric(final T key) {
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
    public Optional<T> getElement(final String key) {
        return getElementGeneric(ops.createString(key));
    }

    @Override
    public Optional<T> getElementGeneric(final T key) {
        return ops.getGeneric(value, key);
    }

    @Override
    public <U> Optional<List<U>> asListOpt(final Function<Dynamic<T>, U> deserializer) {
        return asStreamOpt().map(stream -> stream.map(deserializer).collect(Collectors.toList()));
    }

    @Override
    public <K, V> Optional<Map<K, V>> asMapOpt(final Function<Dynamic<T>, K> keyDeserializer, final Function<Dynamic<T>, V> valueDeserializer) {
        return ops.getMapValues(value).map(map -> {
            final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
            map.forEach(entry ->
                builder.put(keyDeserializer.apply(new Dynamic<>(ops, entry.getFirst())), valueDeserializer.apply(new Dynamic<>(ops, entry.getSecond())))
            );
            return builder.build();
        });
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
        return Objects.hash(ops, value);
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

    @SuppressWarnings("unchecked")
    public static <S, T> T convert(final DynamicOps<S> inOps, final DynamicOps<T> outOps, final S input) {
        if (Objects.equals(inOps, outOps)) {
            return (T) input;
        }
        final Type<?> type = inOps.getType(input);
        if (Objects.equals(type, DSL.emptyPartType())) {
            return outOps.empty();
        }
        if (Objects.equals(type, DSL.byteType())) {
            return outOps.createByte(inOps.getNumberValue(input, 0).byteValue());
        }
        if (Objects.equals(type, DSL.shortType())) {
            return outOps.createShort(inOps.getNumberValue(input, 0).shortValue());
        }
        if (Objects.equals(type, DSL.intType())) {
            return outOps.createInt(inOps.getNumberValue(input, 0).intValue());
        }
        if (Objects.equals(type, DSL.longType())) {
            return outOps.createLong(inOps.getNumberValue(input, 0).longValue());
        }
        if (Objects.equals(type, DSL.floatType())) {
            return outOps.createFloat(inOps.getNumberValue(input, 0).floatValue());
        }
        if (Objects.equals(type, DSL.doubleType())) {
            return outOps.createDouble(inOps.getNumberValue(input, 0).doubleValue());
        }
        if (Objects.equals(type, DSL.bool())) {
            return outOps.createBoolean(inOps.getBooleanValue(input).orElse(false));
        }
        if (Objects.equals(type, DSL.string())) {
            return outOps.createString(inOps.getStringValue(input).orElse(""));
        }
        if (Objects.equals(type, DSL.list(DSL.byteType()))) {
            return outOps.createByteList(inOps.getByteBuffer(input).orElse(ByteBuffer.wrap(new byte[0])));
        }
        if (Objects.equals(type, DSL.list(DSL.intType()))) {
            return outOps.createIntList(inOps.getIntStream(input).orElse(IntStream.empty()));
        }
        if (Objects.equals(type, DSL.list(DSL.longType()))) {
            return outOps.createLongList(inOps.getLongStream(input).orElse(LongStream.empty()));
        }
        if (Objects.equals(type, DSL.list(DSL.remainderType()))) {
            return outOps.createList(inOps.getStream(input).orElse(Stream.empty()).map(e -> convert(inOps, outOps, e)));
        }
        if (Objects.equals(type, DSL.compoundList(DSL.remainderType(), DSL.remainderType()))) {
            return outOps.createMap(inOps.getMapValues(input).orElse(Stream.empty()).map(e ->
                Pair.of(convert(inOps, outOps, e.getFirst()), convert(inOps, outOps, e.getSecond()))
            ).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
        }
        throw new IllegalStateException("Could not convert value of type " + type);
    }
}
