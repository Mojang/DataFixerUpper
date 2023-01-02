// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import org.apache.commons.lang3.mutable.MutableObject;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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

    <U> U convertTo(DynamicOps<U> outOps, T input);

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
     */
    DataResult<T> mergeToList(T list, T value);

    default DataResult<T> mergeToList(final T list, final List<T> values) {
        DataResult<T> result = DataResult.success(list);

        for (final T value : values) {
            result = result.flatMap(r -> mergeToList(r, value));
        }
        return result;
    }

    /**
     * Only successful if first argument is a map or empty
     */
    DataResult<T> mergeToMap(T map, T key, T value);

    default DataResult<T> mergeToMap(final T map, final Map<T, T> values) {
        return mergeToMap(map, MapLike.forMap(values, this));
    }

    default DataResult<T> mergeToMap(final T map, final MapLike<T> values) {
        // TODO: AtomicReference.getPlain/setPlain in java9+
        final MutableObject<DataResult<T>> result = new MutableObject<>(DataResult.success(map));

        values.entries().forEach(entry ->
            result.setValue(result.getValue().flatMap(r -> mergeToMap(r, entry.getFirst(), entry.getSecond())))
        );
        return result.getValue();
    }

    /**
     * Only successful if first argument is empty
     */
    default DataResult<T> mergeToPrimitive(final T prefix, final T value) {
        if (!Objects.equals(prefix, empty())) {
            return DataResult.error(() -> "Do not know how to append a primitive value " + value + " to " + prefix, value);
        }
        return DataResult.success(value);
    }

    DataResult<Stream<Pair<T, T>>> getMapValues(T input);

    default DataResult<Consumer<BiConsumer<T, T>>> getMapEntries(final T input) {
        return getMapValues(input).map(s -> c -> s.forEach(p -> c.accept(p.getFirst(), p.getSecond())));
    }

    T createMap(Stream<Pair<T, T>> map);

    default DataResult<MapLike<T>> getMap(final T input) {
        return getMapValues(input).flatMap(s -> {
            try {
                return DataResult.success(MapLike.forMap(s.collect(Pair.toMap()), this));
            } catch (final IllegalStateException e) {
                return DataResult.error(() -> "Error while building map: " + e.getMessage());
            }
        });
    }

    default T createMap(final Map<T, T> map) {
        return createMap(map.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue())));
    }

    DataResult<Stream<T>> getStream(T input);

    default DataResult<Consumer<Consumer<T>>> getList(final T input) {
        return getStream(input).map(s -> s::forEach);
    }

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
            return DataResult.error(() -> "Some elements are not bytes: " + input);
        });
    }

    default T createByteList(final ByteBuffer input) {
        return createList(IntStream.range(0, input.capacity()).mapToObj(i -> createByte(input.get(i))));
    }

    default DataResult<IntStream> getIntStream(final T input) {
        return getStream(input).flatMap(stream -> {
            final List<T> list = stream.collect(Collectors.toList());
            if (list.stream().allMatch(element -> getNumberValue(element).result().isPresent())) {
                return DataResult.success(list.stream().mapToInt(element -> getNumberValue(element).result().get().intValue()));
            }
            return DataResult.error(() -> "Some elements are not ints: " + input);
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
            return DataResult.error(() -> "Some elements are not longs: " + input);
        });
    }

    default T createLongList(final LongStream input) {
        return createList(input.mapToObj(this::createLong));
    }

    T remove(T input, String key);

    default boolean compressMaps() {
        return false;
    }

    default DataResult<T> get(final T input, final String key) {
        return getGeneric(input, createString(key));
    }

    default DataResult<T> getGeneric(final T input, final T key) {
        return getMap(input).flatMap(map -> Optional.ofNullable(map.get(key))
            .map(DataResult::success)
            .orElseGet(() -> DataResult.error(() -> "No element " + key + " in the map " + input))
        );
    }

    // TODO: eats error if input is not a map
    default T set(final T input, final String key, final T value) {
        return mergeToMap(input, createString(key), value).result().orElse(input);
    }

    // TODO: eats error if input is not a map
    default T update(final T input, final String key, final Function<T, T> function) {
        return get(input, key).map(value -> set(input, key, function.apply(value))).result().orElse(input);
    }

    default T updateGeneric(final T input, final T key, final Function<T, T> function) {
        return getGeneric(input, key).flatMap(value -> mergeToMap(input, key, function.apply(value))).result().orElse(input);
    }

    default ListBuilder<T> listBuilder() {
        return new ListBuilder.Builder<>(this);
    }

    default RecordBuilder<T> mapBuilder() {
        return new RecordBuilder.MapBuilder<>(this);
    }

    default <E> Function<E, DataResult<T>> withEncoder(final Encoder<E> encoder) {
        return e -> encoder.encodeStart(this, e);
    }

    default <E> Function<T, DataResult<Pair<E, T>>> withDecoder(final Decoder<E> decoder) {
        return t -> decoder.decode(this, t);
    }

    default <E> Function<T, DataResult<E>> withParser(final Decoder<E> decoder) {
        return t -> decoder.parse(this, t);
    }

    default <U> U convertList(final DynamicOps<U> outOps, final T input) {
        return outOps.createList(getStream(input).result().orElse(Stream.empty()).map(e -> convertTo(outOps, e)));
    }

    default <U> U convertMap(final DynamicOps<U> outOps, final T input) {
        return outOps.createMap(getMapValues(input).result().orElse(Stream.empty()).map(e ->
            Pair.of(convertTo(outOps, e.getFirst()), convertTo(outOps, e.getSecond()))
        ));
    }
}
