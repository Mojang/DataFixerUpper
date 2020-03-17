// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization.codecs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class CompoundListCodec<K, V> implements Codec<List<Pair<K, V>>> {
    private final Codec<K> keyCodec;
    private final Codec<V> elementCodec;

    public CompoundListCodec(final Codec<K> keyCodec, final Codec<V> elementCodec) {
        this.keyCodec = keyCodec;
        this.elementCodec = elementCodec;
    }

    @Override
    public <T> DataResult<Pair<List<Pair<K, V>>, T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getMapEntries(input).flatMap(map -> {
            final AtomicReference<DataResult<ImmutableList.Builder<Pair<K, V>>>> result = new AtomicReference<>(DataResult.success(ImmutableList.builder()));
            final ImmutableMap.Builder<T, T> errors = ImmutableMap.builder();

            map.accept((key, value) -> {
                final DataResult<K> k = keyCodec.parse(ops, key);
                final DataResult<V> v = elementCodec.parse(ops, value);

                final DataResult<Pair<K, V>> readEntry = k.ap2(v, Pair::new);

                readEntry.error().ifPresent(e -> errors.put(key, value));

                result.set(result.get().ap2(readEntry, ImmutableList.Builder::add));
            });

            return result.get().map(builder -> Pair.of(builder.build(), ops.createMap(errors.build())));
        });
    }

    @Override
    public <T> DataResult<T> encode(final List<Pair<K, V>> input, final DynamicOps<T> ops, final T prefix) {
        final RecordBuilder<T> builder = ops.mapBuilder();

        for (final Pair<K, V> pair : input) {
            builder.add(keyCodec.encodeStart(ops, pair.getFirst()), elementCodec.encodeStart(ops, pair.getSecond()));
        }

        return builder.build(prefix);
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CompoundListCodec<?, ?> that = (CompoundListCodec<?, ?>) o;
        return Objects.equals(keyCodec, that.keyCodec) && Objects.equals(elementCodec, that.elementCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyCodec, elementCodec);
    }

    @Override
    public String toString() {
        return "CompoundListCodec[" + keyCodec + " -> " + elementCodec + ']';
    }
}
