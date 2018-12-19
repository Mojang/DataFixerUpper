package com.mojang.datafixers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.types.DynamicOps;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
public class OptionalDynamic<T> {
    private final DynamicOps<T> ops;
    private final Optional<Dynamic<T>> delegate;

    public OptionalDynamic(final DynamicOps<T> ops, final Optional<Dynamic<T>> delegate) {
        this.ops = ops;
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

    public <U> Optional<List<U>> asListOpt(final Function<Dynamic<T>, U> deserializer) {
        return flatMap(t -> t.asListOpt(deserializer));
    }

    public <U> List<U> asList(final Function<Dynamic<T>, U> deserializer) {
        return asListOpt(deserializer).orElse(ImmutableList.of());
    }

    public <K, V> Optional<Map<K, V>> asMapOpt(final Function<Dynamic<T>, K> keyDeserializer, final Function<Dynamic<T>, V> valueDeserializer) {
        return flatMap(input -> input.asMapOpt(keyDeserializer, valueDeserializer));
    }

    public <K, V> Map<K, V> asMap(final Function<Dynamic<T>, K> keyDeserializer, final Function<Dynamic<T>, V> valueDeserializer) {
        return asMapOpt(keyDeserializer, valueDeserializer).orElseGet(ImmutableMap::of);
    }

    public Number asNumber(final Number defaultValue) {
        return asNumberOpt().orElse(defaultValue);
    }

    public Optional<Number> asNumberOpt() {
        return delegate.flatMap(Dynamic::asNumber);
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
        return delegate.flatMap(Dynamic::asString).orElse(defaultValue);
    }

    public Dynamic<T> orElseEmptyMap() {
        return delegate.orElseGet(() -> new Dynamic<>(ops, ops.emptyMap()));
    }

    public Dynamic<T> orElseEmptyList() {
        return delegate.orElseGet(() -> new Dynamic<>(ops, ops.emptyList()));
    }
}
