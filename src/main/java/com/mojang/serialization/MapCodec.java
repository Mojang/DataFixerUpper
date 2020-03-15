// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.Function;

public interface MapCodec<A> extends MapDecoder<A>, MapEncoder<A>, Codec<A> {
    default <O> RecordCodecBuilder<O, A> forGetter(final Function<O, A> getter) {
        return RecordCodecBuilder.of(getter, this);
    }

    @Override
    default MapCodec<A> withDefault(final A value) {
        final MapCodec<A> self = this;
        return new MapCodec<A>() {
            @Override
            public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return DataResult.success(self.decode(ops, input).result().orElse(value));
            }

            @Override
            public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                return self.encode(input, ops, prefix);
            }

            @Override
            public String toString() {
                return "WithDefault[" + self + " " + value + "]";
            }
        };
    }
}
