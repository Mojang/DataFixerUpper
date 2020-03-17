// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface MapEncoder<A> extends Encoder<A> {
    <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix);

    default <T> RecordBuilder<T> compressedBuilder(final DynamicOps<T> ops) {
        if (ops.compressMaps()) {
            return new MapCodec.CompressedRecordBuilder<>(compressor(ops), ops);
        }
        return ops.mapBuilder();
    }

    <T> Stream<T> keys(final DynamicOps<T> ops);

    <T> MapCompressor<T> compressor(final DynamicOps<T> ops);

    @Override
    default <B> MapEncoder<B> comap(final Function<? super B, ? extends A> function) {
        final MapEncoder<A> self = this;
        return new MapEncoder.Implementation<B>() {
            @Override
            public <T> RecordBuilder<T> encode(final B input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                return self.encode(function.apply(input), ops, prefix);
            }

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return self.keys(ops);
            }

            @Override
            public String toString() {
                return self.toString() + "[comapped]";
            }
        };
    }

    @Override
    default <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
        return encode(input, ops, compressedBuilder(ops)).build(prefix);
    }

    abstract class Implementation<A> implements MapEncoder<A> {
        private final Map<DynamicOps<?>, MapCompressor<?>> compressors = new Object2ObjectArrayMap<>();

        @SuppressWarnings("unchecked")
        @Override
        public <T> MapCompressor<T> compressor(final DynamicOps<T> ops) {
            return (MapCompressor<T>) compressors.computeIfAbsent(ops, k -> new MapCompressor<>(ops, keys(ops)));
        }
    }

    class CompressedRecordBuilder<T> implements RecordBuilder<T> {
        private final MapCompressor<T> compressor;
        private final DynamicOps<T> ops;
        private DataResult<List<T>> builder;

        public CompressedRecordBuilder(final MapCompressor<T> compressor, final DynamicOps<T> ops) {
            this.compressor = compressor;
            this.ops = ops;
            resetBuilder(compressor);
        }

        @Override
        public DynamicOps<T> ops() {
            return ops;
        }

        @Override
        public RecordBuilder<T> add(final String key, final T value) {
            builder = builder.map(b -> {
                b.set(compressor.compress(key), value);
                return b;
            });
            return this;
        }

        @Override
        public RecordBuilder<T> add(final String key, final DataResult<T> value) {
            builder = builder.ap2(value, (b, v) -> {
                b.set(compressor.compress(key), v);
                return b;
            });
            return this;
        }

        @Override
        public RecordBuilder<T> add(final T key, final T value) {
            builder = builder.map(b -> {
                b.set(compressor.compress(key), value);
                return b;
            });
            return this;
        }

        @Override
        public RecordBuilder<T> add(final T key, final DataResult<T> value) {
            builder = builder.ap2(value, (b, v) -> {
                b.set(compressor.compress(key), v);
                return b;
            });
            return this;
        }

        @Override
        public RecordBuilder<T> add(final DataResult<T> key, final DataResult<T> value) {
            builder = builder.ap(key.ap2(value, (k, v) -> b -> {
                b.set(compressor.compress(k), v);
                return b;
            }));
            return this;
        }

        @Override
        public DataResult<T> build(final T prefix) {
            final DataResult<T> result = builder.flatMap(l -> ops.mergeToList(prefix, l));
            resetBuilder(compressor);
            return result;
        }

        private void resetBuilder(final MapCompressor<T> compressor) {
            builder = DataResult.success(IntStream.range(0, compressor.size()).<T>mapToObj(i -> null).collect(Collectors.toList()));
        }
    }
}
