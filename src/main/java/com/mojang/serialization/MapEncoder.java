// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public interface MapEncoder<A> extends Encoder<A> {
    <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix);

    default <T> RecordBuilder<T> compressedBuilder(final DynamicOps<T> ops) {
        if (ops.compressMaps()) {
            return makeCompressedBuilder(ops, compressor(ops));
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

    static <T> RecordBuilder<T> makeCompressedBuilder(final DynamicOps<T> ops, final MapCompressor<T> compressor) {
        class CompressedRecordBuilder extends RecordBuilder.AbstractBuilder<T, List<T>> {
            private CompressedRecordBuilder() {
                super(ops);
            }

            @Override
            protected List<T> initBuilder() {
                final ArrayList<T> list = new ArrayList<>(compressor.size());
                for (int i = 0; i < compressor.size(); i++) {
                    list.add(null);
                }
                return list;
            }

            @Override
            protected List<T> append(final T key, final T value, final List<T> builder) {
                builder.set(compressor.compress(key), value);
                return builder;
            }

            @Override
            protected List<T> append(final String key, final T value, final List<T> builder) {
                builder.set(compressor.compress(key), value);
                return builder;
            }

            @Override
            protected DataResult<T> build(final List<T> builder, final T prefix) {
                return ops().mergeToList(prefix, builder);
            }
        }

        return new CompressedRecordBuilder();
    }
}
