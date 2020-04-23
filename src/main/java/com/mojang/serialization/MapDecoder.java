// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface MapDecoder<A> extends Decoder<A>, Keyable {
    <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input);

    default <T> DataResult<A> compressedDecode(final DynamicOps<T> ops, final T input) {
        if (ops.compressMaps()) {
            final Optional<Consumer<Consumer<T>>> inputList = ops.getList(input).result();

            if (!inputList.isPresent()) {
                return DataResult.error("Input is not a list");
            }

            final MapCompressor<T> compressor = compressor(ops);
            final List<T> entries = new ArrayList<>();
            inputList.get().accept(entries::add);

            final MapLike<T> map = new MapLike<T>() {
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
                    return IntStream.range(0, entries.size()).mapToObj(i -> Pair.of(compressor.decompress(i), entries.get(i))).filter(p -> p.getSecond() != null);
                }
            };
            return decode(ops, map);
        }
        return ops.getMap(input).flatMap(map -> decode(ops, map));
    }

    <T> MapCompressor<T> compressor(DynamicOps<T> ops);

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

            @Override
            public String toString() {
                return self.toString() + "[mapped]";
            }
        };
    }

    default <E> MapDecoder<E> ap(final MapDecoder<Function<? super A, ? extends E>> decoder) {
        final MapDecoder<A> self = this;
        return new Implementation<E>() {
            @Override
            public <T> DataResult<E> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return self.decode(ops, input).flatMap(f ->
                    decoder.decode(ops, input).map(e -> e.apply(f))
                );
            }

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return Stream.concat(self.keys(ops), decoder.keys(ops));
            }

            @Override
            public String toString() {
                return decoder.toString() + " * " + self.toString();
            }
        };
    }

    @Override
    default MapDecoder<A> withDefault(final A value) {
        final MapDecoder<A> self = this;

        return new Implementation<A>() {
            @Override
            public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return DataResult.success(self.decode(ops, input).result().orElse(value));
            }

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return self.keys(ops);
            }

            @Override
            public String toString() {
                return "WithDefault[" + self + " " + value + "]";
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
