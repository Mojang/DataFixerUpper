// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization.codecs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.List;
import java.util.Map;
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
        return ops.getMapValues(input).flatMap(map -> {
            final AtomicReference<DataResult<Pair<ImmutableList.Builder<Pair<K, V>>, ImmutableMap.Builder<T, T>>>> result =
                new AtomicReference<>(DataResult.success(Pair.of(ImmutableList.builder(), ImmutableMap.builder())));

            map.forEach(entry -> {
                result.set(result.get().flatMap(pair -> {
                    final DataResult<Pair<K, V>> readEntry = keyCodec.parse(ops, entry.getFirst()).flatMap(keyValue ->
                        elementCodec.parse(ops, entry.getSecond()).map(elementValue ->
                            Pair.of(keyValue, elementValue)
                        )
                    );
                    readEntry.error().ifPresent(e -> {
                        pair.getSecond().put(entry.getFirst(), entry.getSecond());
                    });
                    return readEntry.map(r -> {
                        pair.getFirst().add(r);
                        return pair;
                    });
                }));
            });

            return result.get().map(pair -> Pair.of(pair.getFirst().build(), ops.createMap(pair.getSecond().build())));
        });
    }

    @Override
    public <T> DataResult<T> encode(final List<Pair<K, V>> input, final DynamicOps<T> ops, final T prefix) {
        final Map<T, T> map = Maps.newHashMap();

        DataResult<Map<T, T>> result = DataResult.success(map);

        for (final Pair<K, V> pair : input) {
            result = result.flatMap(m -> {
                final DataResult<T> element = elementCodec.encodeStart(ops, pair.getSecond());
                final DataResult<Pair<T, T>> entry = element.flatMap(e -> keyCodec.encodeStart(ops, pair.getFirst()).map(k -> Pair.of(k, e)));
                return entry.flatMap(e -> {
                    final T key = e.getFirst();
                    if (m.containsKey(key)) {
                        return DataResult.error("Duplicate key: " + key, m);
                    }
                    m.put(key, e.getSecond());
                    return DataResult.success(m);
                });
            });
        }

        return result.flatMap(m -> ops.mergeToMap(prefix, m));
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
