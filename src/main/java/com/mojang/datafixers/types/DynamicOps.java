// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface DynamicOps<T> {
    T empty();

    default T emptyMap() {
        return createMap(ImmutableMap.of());
    }

    default T emptyList() {
        return createList(Stream.empty());
    }

    Type<?> getType(final T input);

    default <R> Optional<R> cast(final T input, final Type<R> type) {
        if (type == getType(input)) {
            return Optional.of(type.readTyped(new Dynamic<>(this, input)).getSecond().orElseThrow(() -> new IllegalStateException("Parse error during dynamic cast")).getValue());
        }
        return Optional.empty();
    }

    Optional<Number> getNumberValue(T input);

    default Number getNumberValue(final T input, final Number defaultValue) {
        return getNumberValue(input).orElse(defaultValue);
    }

    T createNumeric(Number i);

    default T createByte(final byte value) {
        return createNumeric(value);
    }

    default T createShort(final short value) {
        return createNumeric(value);
    }

    default T createInt(final int value) {
        return createNumeric(value);
    }

    default T createLong(final long value) {
        return createNumeric(value);
    }

    default T createFloat(final float value) {
        return createNumeric(value);
    }

    default T createDouble(final double value) {
        return createNumeric(value);
    }

    default T createBoolean(final boolean value) {
        return createByte((byte) (value ? 1 : 0));
    }

    Optional<String> getStringValue(T input);

    T createString(String value);

    /**
     * keeps input unchanged if it's not list-like
     */
    T mergeInto(T input, T value);

    /**
     * keeps input unchanged if it's not map-like
     */
    T mergeInto(T input, T key, T value);

    /**
     * merges 2 values together, if possible (list + list, map + map, empty + anything)
     */
    T merge(T first, T second);

    Optional<Map<T, T>> getMapValues(T input);

    T createMap(Map<T, T> map);

    Optional<Stream<T>> getStream(T input);

    T createList(Stream<T> input);

    default Optional<ByteBuffer> getByteBuffer(final T input) {
        return getStream(input).flatMap(stream -> {
            final List<T> list = stream.collect(Collectors.toList());
            if (list.stream().allMatch(element -> getNumberValue(element).isPresent())) {
                final ByteBuffer buffer = ByteBuffer.wrap(new byte[list.size()]);
                for (int i = 0; i < list.size(); i++) {
                    buffer.put(i, getNumberValue(list.get(i)).get().byteValue());
                }
                return Optional.of(buffer);
            }
            return Optional.empty();
        });
    }

    default T createByteList(final ByteBuffer input) {
        final int[] i = {0};
        return createList(Stream.generate(() -> createByte(input.get(i[0]++))).limit(input.capacity()));
    }

    default Optional<IntStream> getIntStream(final T input) {
        return getStream(input).flatMap(stream -> {
            final List<T> list = stream.collect(Collectors.toList());
            if (list.stream().allMatch(element -> getNumberValue(element).isPresent())) {
                return Optional.of(list.stream().mapToInt(element -> getNumberValue(element).get().intValue()));
            }
            return Optional.empty();
        });
    }

    default T createIntList(final IntStream input) {
        return createList(input.mapToObj(this::createInt));
    }

    default Optional<LongStream> getLongStream(final T input) {
        return getStream(input).flatMap(stream -> {
            final List<T> list = stream.collect(Collectors.toList());
            if (list.stream().allMatch(element -> getNumberValue(element).isPresent())) {
                return Optional.of(list.stream().mapToLong(element -> getNumberValue(element).get().longValue()));
            }
            return Optional.empty();
        });
    }

    default T createLongList(final LongStream input) {
        return createList(input.mapToObj(this::createLong));
    }

    T remove(T input, String key);

    default Optional<T> get(final T input, final String key) {
        return getGeneric(input, createString(key));
    }

    default Optional<T> getGeneric(final T input, final T key) {
        return getMapValues(input).flatMap(map -> {
            if (map.containsKey(key)) {
                return Optional.of(map.get(key));
            }
            return Optional.empty();
        });
    }

    default T set(final T input, final String key, final T value) {
        return mergeInto(input, createString(key), value);
    }

    default T update(final T input, final String key, final Function<T, T> function) {
        return get(input, key).map(value -> set(input, key, function.apply(value))).orElse(input);
    }

    default T updateGeneric(final T input, final T key, final Function<T, T> function) {
        return getGeneric(input, key).map(value -> mergeInto(input, key, function.apply(value))).orElse(input);
    }
}
