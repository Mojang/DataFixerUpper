// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class Dynamic<T> {
    private final DynamicOps<T> ops;
    private final T value;

    public Dynamic(final DynamicOps<T> ops) {
        this(ops, ops.empty());
    }

    public Dynamic(final DynamicOps<T> ops, @Nullable final T value) {
        this.ops = ops;
        this.value = value == null ? ops.empty() : value;
    }

    public DynamicOps<T> getOps() {
        return ops;
    }

    public T getValue() {
        return value;
    }

    public Dynamic<T> map(final Function<? super T, ? extends T> function) {
        return new Dynamic<>(ops, function.apply(value));
    }

    public Optional<Number> getNumberValue() {
        return ops.getNumberValue(value);
    }

    public Number getNumberValue(final Number defaultValue) {
        return getNumberValue().orElse(defaultValue);
    }

    public Optional<String> getStringValue() {
        return ops.getStringValue(value);
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

    public Dynamic<T> merge(final Dynamic<?> value) {
        return map(v -> ops.mergeInto(v, value.cast(ops)));
    }

    public Dynamic<T> merge(final Dynamic<?> key, final Dynamic<?> value) {
        return map(v -> ops.mergeInto(v, key.cast(ops), value.cast(ops)));
    }

    public Optional<Map<Dynamic<T>, Dynamic<T>>> getMapValues() {
        return ops.getMapValues(value).map(map -> {
            final ImmutableMap.Builder<Dynamic<T>, Dynamic<T>> builder = ImmutableMap.builder();
            for (final Map.Entry<T, T> entry : map.entrySet()) {
                builder.put(new Dynamic<>(ops, entry.getKey()), new Dynamic<>(ops, entry.getValue()));
            }
            return builder.build();
        });
    }

    public Dynamic<T> createMap(final Map<? extends Dynamic<?>, ? extends Dynamic<?>> map) {
        final ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        for (final Map.Entry<? extends Dynamic<?>, ? extends Dynamic<?>> entry : map.entrySet()) {
            builder.put(entry.getKey().cast(ops), entry.getValue().cast(ops));
        }
        return new Dynamic<>(ops, ops.createMap(builder.build()));
    }

    public Dynamic<T> updateMapValues(final Function<Pair<Dynamic<?>, Dynamic<?>>, Pair<Dynamic<?>, Dynamic<?>>> updater) {
        return DataFixUtils.orElse(getMapValues().map(map -> map.entrySet().stream().map(e -> {
            final Pair<Dynamic<?>, Dynamic<?>> pair = updater.apply(Pair.of(e.getKey(), e.getValue()));
            return Pair.of(pair.getFirst().castTyped(ops), pair.getSecond().castTyped(ops));
        }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))).map(this::createMap), this);
    }

    public Optional<Stream<Dynamic<T>>> getStream() {
        return ops.getStream(value).map(s -> s.map(e -> new Dynamic<>(ops, e)));
    }

    public Dynamic<T> createList(final Stream<? extends Dynamic<?>> input) {
        return new Dynamic<>(ops, ops.createList(input.map(element -> element.cast(ops))));
    }

    public Optional<ByteBuffer> getByteBuffer() {
        return ops.getByteBuffer(value);
    }

    public Dynamic<?> createByteList(final ByteBuffer input) {
        return new Dynamic<>(ops, ops.createByteList(input));
    }

    public Optional<IntStream> getIntStream() {
        return ops.getIntStream(value);
    }

    public Dynamic<?> createIntList(final IntStream input) {
        return new Dynamic<>(ops, ops.createIntList(input));
    }

    public Optional<LongStream> getLongStream() {
        return ops.getLongStream(value);
    }

    public Dynamic<?> createLongList(final LongStream input) {
        return new Dynamic<>(ops, ops.createLongList(input));
    }

    public Dynamic<T> remove(final String key) {
        return map(v -> ops.remove(v, key));
    }

    public Optional<Dynamic<T>> get(final String key) {
        return ops.get(value, key).map(v -> new Dynamic<>(ops, v));
    }

    public Optional<T> getGeneric(final T key) {
        return ops.getGeneric(value, key);
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


    public Dynamic<T> createNumeric(final Number i) {
        return new Dynamic<>(ops, ops.createNumeric(i));
    }

    public Dynamic<T> createByte(final byte value) {
        return new Dynamic<>(ops, ops.createByte(value));
    }

    public Dynamic<T> createShort(final short value) {
        return new Dynamic<>(ops, ops.createShort(value));
    }

    public Dynamic<T> createInt(final int value) {
        return new Dynamic<>(ops, ops.createInt(value));
    }

    public Dynamic<T> createLong(final long value) {
        return new Dynamic<>(ops, ops.createLong(value));
    }

    public Dynamic<T> createFloat(final float value) {
        return new Dynamic<>(ops, ops.createFloat(value));
    }

    public Dynamic<T> createDouble(final double value) {
        return new Dynamic<>(ops, ops.createDouble(value));
    }

    public Dynamic<T> createBoolean(final boolean value) {
        return new Dynamic<>(ops, ops.createBoolean(value));
    }

    public Dynamic<T> createString(final String value) {
        return new Dynamic<>(ops, ops.createString(value));
    }

    public int getInt(final String key) {
        return getNumber(key, 0).intValue();
    }

    public long getLong(final String key) {
        return getNumber(key, 0).longValue();
    }

    public float getFloat(final String key) {
        return getNumber(key, 0).floatValue();
    }

    public double getDouble(final String key) {
        return getNumber(key, 0).doubleValue();
    }

    public byte getByte(final String key) {
        return getNumber(key, 0).byteValue();
    }

    public short getShort(final String key) {
        return getNumber(key, 0).shortValue();
    }

    public boolean getBoolean(final String key) {
        return getNumber(key, 0).intValue() != 0;
    }

    public String getString(final String key) {
        return getElement(key).flatMap(ops::getStringValue).orElse("");
    }

    public int getInt(final String key, final int defaultValue) {
        return getNumber(key, defaultValue).intValue();
    }

    public long getLong(final String key, final long defaultValue) {
        return getNumber(key, defaultValue).longValue();
    }

    public float getFloat(final String key, final float defaultValue) {
        return getNumber(key, defaultValue).floatValue();
    }

    public double getDouble(final String key, final double defaultValue) {
        return getNumber(key, defaultValue).doubleValue();
    }

    public byte getByte(final String key, final byte defaultValue) {
        return getNumber(key, defaultValue).byteValue();
    }

    public short getShort(final String key, final short defaultValue) {
        return getNumber(key, defaultValue).shortValue();
    }

    public boolean getBoolean(final String key, final boolean defaultValue) {
        return getNumber(key, defaultValue ? 1 : 0).intValue() != 0;
    }

    public String getString(final String key, final String defaultValue) {
        return getElement(key).flatMap(ops::getStringValue).orElse(defaultValue);
    }

    public Number getNumber(final String key, final Number defaultValue) {
        return getNumber(key).orElse(defaultValue);
    }

    public Optional<Number> getNumber(final String key) {
        return getElement(key).flatMap(ops::getNumberValue);
    }

    public T getElement(final String key, final T defaultValue) {
        return getElement(key).orElse(defaultValue);
    }

    public Optional<T> getElement(final String key) {
        return getElementGeneric(ops.createString(key));
    }

    public T getElementGeneric(final T key, final T defaultValue) {
        return getElementGeneric(key).orElse(defaultValue);
    }

    public Optional<T> getElementGeneric(final T key) {
        return ops.getMapValues(value).flatMap(m -> Optional.ofNullable(m.get(key)));
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

    @SuppressWarnings("unchecked")
    public static <S, T> T convert(final DynamicOps<S> inOps, final DynamicOps<T> outOps, final S input) {
        if (Objects.equals(inOps, outOps)) {
            return (T) input;
        }
        final Type<?> type = inOps.getType(input);
        if (Objects.equals(type, DSL.nilType())) {
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
            return outOps.createBoolean(inOps.getNumberValue(input, 0).byteValue() != 0);
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
            return outOps.createMap(inOps.getMapValues(input).orElse(ImmutableMap.of()).entrySet().stream().map(e ->
                Pair.of(convert(inOps, outOps, e.getKey()), convert(inOps, outOps, e.getValue()))
            ).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
        }
        throw new IllegalStateException("Could not convert value of type " + type);
    }

    public Dynamic<T> emptyList() {
        return new Dynamic<>(ops, ops.emptyList());
    }

    public Dynamic<T> emptyMap() {
        return new Dynamic<>(ops, ops.emptyMap());
    }
}
