// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.util.Pair;

import java.util.function.Function;

public interface MapDecoder<A> extends Decoder<A> {
    <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input);

    @Override
    default <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getMap(input).flatMap(map -> decode(ops, map).map(r -> Pair.of(r, input)));
    }

    @Override
    default <B> MapDecoder<B> map(final Function<? super A, ? extends B> function) {
        final MapDecoder<A> self = this;
        return new MapDecoder<B>() {
            @Override
            public <T> DataResult<B> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return self.decode(ops, input).map(function);
            }
        };
    }
}
