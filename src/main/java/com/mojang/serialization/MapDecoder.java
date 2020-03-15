// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface MapDecoder<A> extends Decoder<A> {
    <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input);

    default <T> DataResult<A> compressedDecode(final DynamicOps<T> ops, final T input) {
        if (ops.compressMaps()) {
            return decode(ops, new MapLike<T>() {
                private final MapCompressor<T> compressor = compressor(ops);
                private final List<T> entries = ops.getStream(input).result().get().collect(Collectors.toList());

                @Nullable
                @Override
                public T get(final T key) {
                    return entries.get(compressor.compress(key));
                }

                @Nullable
                @Override
                public T get(final String key) {
                    return entries.get(compressor.compress(key));
                }

                @Override
                public Stream<Pair<T, T>> entries() {
                    return IntStream.range(0, entries.size()).mapToObj(i -> Pair.of(compressor.decompress(i), entries.get(i)));
                }
            });
        }
        return ops.getMap(input).flatMap(map -> decode(ops, map));
    }

    <T> MapCompressor<T> compressor(DynamicOps<T> ops);

    <T> Stream<T> keys(final DynamicOps<T> ops);

    @Override
    default <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
        return compressedDecode(ops, input).map(r -> Pair.of(r, input));
    }

    @Override
    default <B> MapDecoder<B> map(final Function<? super A, ? extends B> function) {
        final MapDecoder<A> self = this;
        return new Implementation<B>() {
            @Override
            public <T> DataResult<B> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return self.decode(ops, input).map(function);
            }

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return self.keys(ops);
            }
        };
    }

    abstract class Implementation<A> implements MapDecoder<A> {
        private final Map<DynamicOps<?>, MapCompressor<?>> compressors = new Object2ObjectArrayMap<>();

        @SuppressWarnings("unchecked")
        @Override
        public <T> MapCompressor<T> compressor(final DynamicOps<T> ops) {
            return (MapCompressor<T>) compressors.computeIfAbsent(ops, k -> new MapCompressor<>(ops, keys(ops)));
        }
    }
}
