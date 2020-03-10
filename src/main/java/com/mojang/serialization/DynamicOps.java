// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;

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

public interface DynamicOps<T> {
    T empty();

    default T emptyMap() {
        return createMap(ImmutableMap.of());
    }

    default T emptyList() {
        return createList(Stream.empty());
    }

    Type<?> getType(final T input);

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

    default Optional<Boolean> getBooleanValue(final T input) {
        return getNumberValue(input).map(number -> number.byteValue() != 0);
    }

    default T createBoolean(final boolean value) {
        return createByte((byte) (value ? 1 : 0));
    }

    Optional<String> getStringValue(T input);

    T createString(String value);

    /**
     * Only successful if first argument is a list/array
     * @return
     */
    DataResult<T> mergeInto(T list, T value);

    default DataResult<T> mergeInto(final T list, final List<T> values) {
        DataResult<T> result = DataResult.success(list);

        for (final T value : values) {
            result = result.flatMap(r -> mergeInto(r, value));
        }
        return result;
    }

    /**
     * Only successful if first argument is a map
     * @return
     */
    DataResult<T> mergeInto(T map, T key, T value);

    default DataResult<T> mergeInto(final T map, final Map<T, T> values) {
        DataResult<T> result = DataResult.success(map);

        for (final Map.Entry<T, T> entry : values.entrySet()) {
            result = result.flatMap(r -> mergeInto(r, entry.getKey(), entry.getValue()));
        }
        return result;
    }

    Optional<Stream<Pair<T, T>>> getMapValues(T input);

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
        return getMapValues(input).flatMap(map -> map.filter(p -> Objects.equals(key, p.getFirst())).map(Pair::getSecond).findFirst());
    }

    // TODO: eats error if input is not a map
    default T set(final T input, final String key, final T value) {
        return mergeInto(input, createString(key), value).result().orElse(input);
    }

    // TODO: eats error if input is not a map
    default T update(final T input, final String key, final Function<T, T> function) {
        return get(input, key).map(value -> set(input, key, function.apply(value))).orElse(input);
    }

    default T updateGeneric(final T input, final T key, final Function<T, T> function) {
        return getGeneric(input, key).flatMap(value -> mergeInto(input, key, function.apply(value)).result()).orElse(input);
    }
}
