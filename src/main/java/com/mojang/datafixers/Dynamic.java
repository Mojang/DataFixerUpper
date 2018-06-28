package com.mojang.datafixers;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.types.DynamicOps;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
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

    public Dynamic<T> empty() {
        return new Dynamic<>(ops);
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

    public Dynamic<T> getOrEmpty(final String key) {
        return get(key).orElseGet(this::empty);
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

    public Dynamic<T> createChar(final char value) {
        return new Dynamic<>(ops, ops.createChar(value));
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
}
