// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.Function;
import java.util.stream.Stream;

public abstract class MapCodec<A> extends MapDecoder.Implementation<A> implements MapEncoder<A>, Codec<A> {
    public final <O> RecordCodecBuilder<O, A> forGetter(final Function<O, A> getter) {
        return RecordCodecBuilder.of(getter, this);
    }

    @Override
    public abstract <T> Stream<T> keys(final DynamicOps<T> ops);

    @Override
    public MapCodec<A> withDefault(final A value) {
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
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return self.keys(ops);
            }

            @Override
            public String toString() {
                return "WithDefault[" + self + " " + value + "]";
            }
        };
    }

}
