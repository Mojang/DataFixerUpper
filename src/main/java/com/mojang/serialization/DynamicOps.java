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

    DataResult<Number> getNumberValue(T input);

    default Number getNumberValue(final T input, final Number defaultValue) {
        return getNumberValue(input).result().orElse(defaultValue);
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

    default DataResult<Boolean> getBooleanValue(final T input) {
        return getNumberValue(input).map(number -> number.byteValue() != 0);
    }

    default T createBoolean(final boolean value) {
        return createByte((byte) (value ? 1 : 0));
    }

    DataResult<String> getStringValue(T input);

    T createString(String value);

    /**
     * Only successful if first argument is a list/array or empty
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
     * Only successful if first argument is a map or empty
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

    DataResult<Stream<Pair<T, T>>> getMapValues(T input);

    T createMap(Map<T, T> map);

    DataResult<Stream<T>> getStream(T input);

    T createList(Stream<T> input);

    default DataResult<ByteBuffer> getByteBuffer(final T input) {
        return getStream(input).flatMap(stream -> {
            final List<T> list = stream.collect(Collectors.toList());
            if (list.stream().allMatch(element -> getNumberValue(element).result().isPresent())) {
                final ByteBuffer buffer = ByteBuffer.wrap(new byte[list.size()]);
                for (int i = 0; i < list.size(); i++) {
                    buffer.put(i, getNumberValue(list.get(i)).result().get().byteValue());
                }
                return DataResult.success(buffer);
            }
            return DataResult.error("Some elements are not bytes: " + input);
        });
    }

    default T createByteList(final ByteBuffer input) {
        final int[] i = {0};
        return createList(Stream.generate(() -> createByte(input.get(i[0]++))).limit(input.capacity()));
    }

    default DataResult<IntStream> getIntStream(final T input) {
        return getStream(input).flatMap(stream -> {
            final List<T> list = stream.collect(Collectors.toList());
            if (list.stream().allMatch(element -> getNumberValue(element).result().isPresent())) {
                return DataResult.success(list.stream().mapToInt(element -> getNumberValue(element).result().get().intValue()));
            }
            return DataResult.error("Some elements are not ints: " + input);
        });
    }

    default T createIntList(final IntStream input) {
        return createList(input.mapToObj(this::createInt));
    }

    default DataResult<LongStream> getLongStream(final T input) {
        return getStream(input).flatMap(stream -> {
            final List<T> list = stream.collect(Collectors.toList());
            if (list.stream().allMatch(element -> getNumberValue(element).result().isPresent())) {
                return DataResult.success(list.stream().mapToLong(element -> getNumberValue(element).result().get().longValue()));
            }
            return DataResult.error("Some elements are not longs: " + input);
        });
    }

    default T createLongList(final LongStream input) {
        return createList(input.mapToObj(this::createLong));
    }

    T remove(T input, String key);

    default DataResult<T> get(final T input, final String key) {
        return getGeneric(input, createString(key));
    }

    default DataResult<T> getGeneric(final T input, final T key) {
        return getMapValues(input).<T>flatMap(map -> map
            .filter(p -> Objects.equals(key, p.getFirst()))
            .map(Pair::getSecond)
            .findFirst()
            .map(DataResult::success)
            .orElseGet(() -> DataResult.error("No element " + key + " in the map " + input))
        );
    }

    // TODO: eats error if input is not a map
    default T set(final T input, final String key, final T value) {
        return mergeInto(input, createString(key), value).result().orElse(input);
    }

    // TODO: eats error if input is not a map
    default T update(final T input, final String key, final Function<T, T> function) {
        return get(input, key).map(value -> set(input, key, function.apply(value))).result().orElse(input);
    }

    default T updateGeneric(final T input, final T key, final Function<T, T> function) {
        return getGeneric(input, key).flatMap(value -> mergeInto(input, key, function.apply(value))).result().orElse(input);
    }

    default ListBuilder<T> listBuilder() {
        return new ListBuilder.Builder<>(this);
    }

    default DataResult<T> list(final Iterable<? extends Serializable> list) {
        return list(list, empty(), empty());
    }

    default DataResult<T> list(final Iterable<? extends Serializable> list, final T prefix, final T elementPrefix) {
        return list(list, prefix, e -> e.serialize(this, elementPrefix));
    }

    default <E> DataResult<T> list(final Iterable<E> list, final T prefix, final Function<? super E, ? extends DataResult<T>> elementSerializer) {
        final ListBuilder<T> builder = listBuilder();
        list.forEach(element -> builder.add(elementSerializer.apply(element)));
        return builder.build(prefix);
    }

    default RecordBuilder<T> mapBuilder() {
        return new RecordBuilder.Builder<>(this);
    }

    default DataResult<T> map(final Map<String, ? extends Serializable> map, final T prefix) {
        return map(map, prefix, Function.identity(), e -> e.serialize(this, empty()));
    }

    default <K, V> DataResult<T> map(final Map<K, V> map, final T prefix, final Function<? super K, ? extends String> keySerializer, final Function<? super V, ? extends DataResult<T>> elementSerializer) {
        final RecordBuilder<T> builder = mapBuilder();
        map.forEach((key, value) -> builder.add(keySerializer.apply(key), elementSerializer.apply(value)));
        return builder.build(prefix);
    }
}
