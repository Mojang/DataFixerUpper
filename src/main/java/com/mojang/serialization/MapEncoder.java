// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public interface MapEncoder<A> extends Keyable {
    <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix);

    default <T> RecordBuilder<T> compressedBuilder(final DynamicOps<T> ops) {
        if (ops.compressMaps()) {
            return makeCompressedBuilder(ops, compressor(ops));
        }
        return ops.mapBuilder();
    }

    <T> KeyCompressor<T> compressor(final DynamicOps<T> ops);

    default <B> MapEncoder<B> comap(final Function<? super B, ? extends A> function) {
        return new Implementation<B>() {
            @Override
            public <T> RecordBuilder<T> encode(final B input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                return MapEncoder.this.encode(function.apply(input), ops, prefix);
            }

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapEncoder.this.keys(ops);
            }

            @Override
            public String toString() {
                return MapEncoder.this.toString() + "[comapped]";
            }
        };
    }

    default <B> MapEncoder<B> flatComap(final Function<? super B, ? extends DataResult<? extends A>> function) {
        return new Implementation<B>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapEncoder.this.keys(ops);
            }

            @Override
            public <T> RecordBuilder<T> encode(final B input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                final DataResult<? extends A> aResult = function.apply(input);
                final RecordBuilder<T> builder = prefix.withErrorsFrom(aResult);
                return aResult.map(r -> MapEncoder.this.encode(r, ops, builder)).result().orElse(builder);
            }

            @Override
            public String toString() {
                return MapEncoder.this.toString() + "[flatComapped]";
            }
        };
    }

    default Encoder<A> encoder() {
        return new Encoder<A>() {
            @Override
            public <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
                return MapEncoder.this.encode(input, ops, compressedBuilder(ops)).build(prefix);
            }

            @Override
            public String toString() {
                return MapEncoder.this.toString();
            }
        };
    }

    default MapEncoder<A> withLifecycle(final Lifecycle lifecycle) {
        return new Implementation<A>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapEncoder.this.keys(ops);
            }

            @Override
            public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                return MapEncoder.this.encode(input, ops, prefix).setLifecycle(lifecycle);
            }

            @Override
            public String toString() {
                return MapEncoder.this.toString();
            }
        };
    }

    abstract class Implementation<A> extends CompressorHolder implements MapEncoder<A> {
    }

    static <T> RecordBuilder<T> makeCompressedBuilder(final DynamicOps<T> ops, final KeyCompressor<T> compressor) {
        class CompressedRecordBuilder extends RecordBuilder.AbstractUniversalBuilder<T, List<T>> {
            private CompressedRecordBuilder() {
                super(ops);
            }

            @Override
            protected List<T> initBuilder() {
                final List<T> list = new ArrayList<>(compressor.size());
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
            protected DataResult<T> build(final List<T> builder, final T prefix) {
                return ops().mergeToList(prefix, builder);
            }
        }

        return new CompressedRecordBuilder();
    }
}
