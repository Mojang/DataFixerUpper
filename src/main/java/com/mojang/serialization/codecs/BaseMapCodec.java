// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization.codecs;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface BaseMapCodec<K, V> {
    Codec<K> keyCodec();

    Codec<V> elementCodec();

    default <T> DataResult<Map<K, V>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final ImmutableMap.Builder<K, V> read = ImmutableMap.builder();
        final Stream.Builder<Pair<T, T>> failed = Stream.builder();

        final DataResult<Unit> result = input.entries().reduce(
            DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
            (r, pair) -> {
                final DataResult<K> key = keyCodec().parse(ops, pair.getFirst());
                final DataResult<V> value = elementCodec().parse(ops, pair.getSecond());

                final DataResult<Pair<K, V>> entryResult = key.apply2stable(Pair::of, value);
                entryResult.resultOrPartial().ifPresent(entry -> read.put(entry.getFirst(), entry.getSecond()));
                if (entryResult.error().isPresent()) {
                    failed.add(pair);
                }

                return r.apply2stable((u, p) -> u, entryResult);
            },
            (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2)
        );

        final Map<K, V> elements = read.build();
        final T errors = ops.createMap(failed.build());

        return result.map(unit -> elements).setPartial(elements).mapError(e -> e + " missed input: " + errors);
    }

    default <T> RecordBuilder<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
        for (final Map.Entry<K, V> entry : input.entrySet()) {
            prefix.add(keyCodec().encodeStart(ops, entry.getKey()), elementCodec().encodeStart(ops, entry.getValue()));
        }
        return prefix;
    }
}
