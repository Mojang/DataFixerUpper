// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.util.Pair;

public interface Codec<A> extends Encoder<A>, Decoder<A> {
    static <A extends Serializable> Codec<A> of(final Decoder<A> decoder) {
        return of(Encoder.of(), decoder);
    }

    static <A> Codec<A> of(final Encoder<A> encoder, final Decoder<A> decoder) {
        return new Codec<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return decoder.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(final DynamicOps<T> ops, final T prefix, final A input) {
                return encoder.encode(ops, prefix, input);
            }

            @Override
            public String toString() {
                return "Codec[" + encoder + " " + decoder + "]";
            }
        };
    }
}
