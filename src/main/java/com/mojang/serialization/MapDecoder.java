// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.util.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface MapDecoder<A> extends Keyable {
    <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input);

    default <T> DataResult<A> compressedDecode(final DynamicOps<T> ops, final T input) {
        if (ops.compressMaps()) {
            final Optional<Consumer<Consumer<T>>> inputList = ops.getList(input).result();

            if (!inputList.isPresent()) {
                return DataResult.error(() -> "Input is not a list");
            }

            final KeyCompressor<T> compressor = compressor(ops);
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
        // will use the lifecycle of decode
        return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> decode(ops, map));
    }

    <T> KeyCompressor<T> compressor(DynamicOps<T> ops);

    default Decoder<A> decoder() {
        return new Decoder<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return compressedDecode(ops, input).map(r -> Pair.of(r, input));
            }

            @Override
            public String toString() {
                return MapDecoder.this.toString();
            }
        };
    }

    default <B> MapDecoder<B> flatMap(final Function<? super A, ? extends DataResult<? extends B>> function) {
        return new Implementation<B>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapDecoder.this.keys(ops);
            }

            @Override
            public <T> DataResult<B> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return MapDecoder.this.decode(ops, input).flatMap(b -> function.apply(b).map(Function.identity()));
            }

            @Override
            public String toString() {
                return MapDecoder.this.toString() + "[flatMapped]";
            }
        };
    }

    default <B> MapDecoder<B> map(final Function<? super A, ? extends B> function) {
        return new Implementation<B>() {
            @Override
            public <T> DataResult<B> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return MapDecoder.this.decode(ops, input).map(function);
            }

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapDecoder.this.keys(ops);
            }

            @Override
            public String toString() {
                return MapDecoder.this.toString() + "[mapped]";
            }
        };
    }

    default <E> MapDecoder<E> ap(final MapDecoder<Function<? super A, ? extends E>> decoder) {
        return new Implementation<E>() {
            @Override
            public <T> DataResult<E> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return MapDecoder.this.decode(ops, input).flatMap(f ->
                    decoder.decode(ops, input).map(e -> e.apply(f))
                );
            }

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return Stream.concat(MapDecoder.this.keys(ops), decoder.keys(ops));
            }

            @Override
            public String toString() {
                return decoder.toString() + " * " + MapDecoder.this.toString();
            }
        };
    }

    default MapDecoder<A> withLifecycle(final Lifecycle lifecycle) {
        return new Implementation<A>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapDecoder.this.keys(ops);
            }

            @Override
            public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return MapDecoder.this.decode(ops, input).setLifecycle(lifecycle);
            }

            @Override
            public String toString() {
                return MapDecoder.this.toString();
            }
        };
    }

    abstract class Implementation<A> extends CompressorHolder implements MapDecoder<A> {
    }
}
