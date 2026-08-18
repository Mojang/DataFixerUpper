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
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface BaseMapCodec<K, V> {
    Codec<K> keyCodec();

    Codec<V> elementCodec();

    default <T> DataResult<Map<K, V>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final List<Pair<T, T>> pairs = input.entries().toList();

        final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        boolean anyDecodeFailure = false;
        for (final Pair<T, T> pair : pairs) {
            final Optional<K> k = keyCodec().parse(ops, pair.getFirst()).result();
            final Optional<V> v = elementCodec().parse(ops, pair.getSecond()).result();
            if (k.isPresent() && v.isPresent()) {
                builder.put(k.get(), v.get());
            } else {
                anyDecodeFailure = true;
                break;
            }
        }

        // failures and duplicates are rare, so it is okay to fall back to HashMap
        // no failures: best performance
        // failure:     acceptable performance
        if (!anyDecodeFailure) {
            try {
                return DataResult.success(builder.buildOrThrow());
            } catch (IllegalArgumentException duplicateKey) {
                return fallbackDecode(ops, pairs);
            }
        }

        return fallbackDecode(ops, pairs);
    }

    // HashMap fallback to check for failures and duplicates
    private <T> DataResult<Map<K, V>> fallbackDecode(final DynamicOps<T> ops, final List<Pair<T, T>> pairs) {
        final Object2ObjectMap<K, V> read = new Object2ObjectLinkedOpenHashMap<>();
        final Stream.Builder<Pair<T, T>> failed = Stream.builder();

        final DataResult<Unit> result = pairs.stream().reduce(
            DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
            (r, pair) -> {
                final DataResult<K> key = keyCodec().parse(ops, pair.getFirst());
                final DataResult<V> value = elementCodec().parse(ops, pair.getSecond());

                final DataResult<Pair<K, V>> entryResult = key.apply2stable(Pair::of, value);
                final Optional<Pair<K, V>> entry = entryResult.resultOrPartial();
                if (entry.isPresent()) {
                    final V existingValue = read.putIfAbsent(entry.get().getFirst(), entry.get().getSecond());
                    if (existingValue != null) {
                        failed.add(pair);
                        return r.apply2stable((u, p) -> u, DataResult.error(() -> "Duplicate entry for key: '" + entry.get().getFirst() + "'"));
                    }
                }
                if (entryResult.isError()) {
                    failed.add(pair);
                }

                return r.apply2stable((u, p) -> u, entryResult);
            },
            (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2)
        );

        final Map<K, V> elements = ImmutableMap.copyOf(read);
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
