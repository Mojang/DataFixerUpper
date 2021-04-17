// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.ListBox;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Pair;
import org.apache.commons.lang3.mutable.MutableObject;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public abstract class DynamicLike<T> {
    protected final DynamicOps<T> ops;

    public DynamicLike(final DynamicOps<T> ops) {
        this.ops = ops;
    }

    public DynamicOps<T> getOps() {
        return ops;
    }

    public abstract DataResult<Number> asNumber();
    public abstract DataResult<String> asString();
    public abstract DataResult<Stream<Dynamic<T>>> asStreamOpt();
    public abstract DataResult<Stream<Pair<Dynamic<T>, Dynamic<T>>>> asMapOpt();
    public abstract DataResult<ByteBuffer> asByteBufferOpt();
    public abstract DataResult<IntStream> asIntStreamOpt();
    public abstract DataResult<LongStream> asLongStreamOpt();
    public abstract OptionalDynamic<T> get(String key);
    public abstract DataResult<T> getGeneric(T key);
    public abstract DataResult<T> getElement(String key);
    public abstract DataResult<T> getElementGeneric(T key);

    public abstract <A> DataResult<Pair<A, T>> decode(final Decoder<? extends A> decoder);

    public <U> DataResult<List<U>> asListOpt(final Function<Dynamic<T>, U> deserializer) {
        return asStreamOpt().map(stream -> stream.map(deserializer).collect(Collectors.toList()));
    }

    public <K, V> DataResult<Map<K, V>> asMapOpt(final Function<Dynamic<T>, K> keyDeserializer, final Function<Dynamic<T>, V> valueDeserializer) {
        return asMapOpt().map(map -> {
            final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
            map.forEach(entry ->
                builder.put(keyDeserializer.apply(entry.getFirst()), valueDeserializer.apply(entry.getSecond()))
            );
            return builder.build();
        });
    }

    public <A> DataResult<A> read(final Decoder<? extends A> decoder) {
        return decode(decoder).map(Pair::getFirst);
    }

    public <E> DataResult<List<E>> readList(final Decoder<E> decoder) {
        return asStreamOpt()
            .map(s -> s.map(d -> d.read(decoder)).collect(Collectors.<App<DataResult.Mu, E>>toList()))
            .flatMap(l -> DataResult.unbox(ListBox.flip(DataResult.instance(), l)));
    }

    public <E> DataResult<List<E>> readList(final Function<? super Dynamic<?>, ? extends DataResult<? extends E>> decoder) {
        return asStreamOpt()
            .map(s -> s.map(decoder).map(r -> r.map(e -> (E) e)).collect(Collectors.<App<DataResult.Mu, E>>toList()))
            .flatMap(l -> DataResult.unbox(ListBox.flip(DataResult.instance(), l)));
    }

    public <K, V> DataResult<List<Pair<K, V>>> readMap(final Decoder<K> keyDecoder, final Decoder<V> valueDecoder) {
        return asMapOpt()
            .map(stream -> stream.map(p -> p.getFirst().read(keyDecoder).flatMap(f -> p.getSecond().read(valueDecoder).map(s -> Pair.of(f, s)))).collect(Collectors.<App<DataResult.Mu, Pair<K, V>>>toList()))
            .flatMap(l -> DataResult.unbox(ListBox.flip(DataResult.instance(), l)));
    }

    public <K, V> DataResult<List<Pair<K, V>>> readMap(final Decoder<K> keyDecoder, final Function<K, Decoder<V>> valueDecoder) {
        return asMapOpt()
            .map(stream -> stream.map(p -> p.getFirst().read(keyDecoder).flatMap(f -> p.getSecond().read(valueDecoder.apply(f)).map(s -> Pair.of(f, s)))).collect(Collectors.<App<DataResult.Mu, Pair<K, V>>>toList()))
            .flatMap(l -> DataResult.unbox(ListBox.flip(DataResult.instance(), l)));
    }

    public <R> DataResult<R> readMap(final DataResult<R> empty, final Function3<R, Dynamic<T>, Dynamic<T>, DataResult<R>> combiner) {
        return asMapOpt().flatMap(stream -> {
            // TODO: AtomicReference.getPlain/setPlain in java9+
            final MutableObject<DataResult<R>> result = new MutableObject<>(empty);
            stream.forEach(p -> result.setValue(result.getValue().flatMap(r -> combiner.apply(r, p.getFirst(), p.getSecond()))));
            return result.getValue();
        });
    }

    public Number asNumber(final Number defaultValue) {
        return asNumber().result().orElse(defaultValue);
    }

    public int asInt(final int defaultValue) {
        return asNumber(defaultValue).intValue();
    }

    public long asLong(final long defaultValue) {
        return asNumber(defaultValue).longValue();
    }

    public float asFloat(final float defaultValue) {
        return asNumber(defaultValue).floatValue();
    }

    public double asDouble(final double defaultValue) {
        return asNumber(defaultValue).doubleValue();
    }

    public byte asByte(final byte defaultValue) {
        return asNumber(defaultValue).byteValue();
    }

    public short asShort(final short defaultValue) {
        return asNumber(defaultValue).shortValue();
    }

    public boolean asBoolean(final boolean defaultValue) {
        return asNumber(defaultValue ? 1 : 0).intValue() != 0;
    }

    public String asString(final String defaultValue) {
        return asString().result().orElse(defaultValue);
    }

    public Stream<Dynamic<T>> asStream() {
        return asStreamOpt().result().orElseGet(Stream::empty);
    }

    public ByteBuffer asByteBuffer() {
        return asByteBufferOpt().result().orElseGet(() -> ByteBuffer.wrap(new byte[0]));
    }

    public IntStream asIntStream() {
        return asIntStreamOpt().result().orElseGet(IntStream::empty);
    }

    public LongStream asLongStream() {
        return asLongStreamOpt().result().orElseGet(LongStream::empty);
    }

    public <U> List<U> asList(final Function<Dynamic<T>, U> deserializer) {
        return asListOpt(deserializer).result().orElseGet(ImmutableList::of);
    }

    public <K, V> Map<K, V> asMap(final Function<Dynamic<T>, K> keyDeserializer, final Function<Dynamic<T>, V> valueDeserializer) {
        return asMapOpt(keyDeserializer, valueDeserializer).result().orElseGet(ImmutableMap::of);
    }

    public T getElement(final String key, final T defaultValue) {
        return getElement(key).result().orElse(defaultValue);
    }

    public T getElementGeneric(final T key, final T defaultValue) {
        return getElementGeneric(key).result().orElse(defaultValue);
    }

    public Dynamic<T> emptyList() {
        return new Dynamic<>(ops, ops.emptyList());
    }

    public Dynamic<T> emptyMap() {
        return new Dynamic<>(ops, ops.emptyMap());
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

    public Dynamic<T> createList(final Stream<? extends Dynamic<?>> input) {
        return new Dynamic<>(ops, ops.createList(input.map(element -> element.cast(ops))));
    }

    public Dynamic<T> createMap(final Map<? extends Dynamic<?>, ? extends Dynamic<?>> map) {
        final ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        for (final Map.Entry<? extends Dynamic<?>, ? extends Dynamic<?>> entry : map.entrySet()) {
            builder.put(entry.getKey().cast(ops), entry.getValue().cast(ops));
        }
        return new Dynamic<>(ops, ops.createMap(builder.build()));
    }

    public Dynamic<?> createByteList(final ByteBuffer input) {
        return new Dynamic<>(ops, ops.createByteList(input));
    }

    public Dynamic<?> createIntList(final IntStream input) {
        return new Dynamic<>(ops, ops.createIntList(input));
    }

    public Dynamic<?> createLongList(final LongStream input) {
        return new Dynamic<>(ops, ops.createLongList(input));
    }
}
