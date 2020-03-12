// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.util.Pair;

public interface Codec<A> extends Encoder<A>, Decoder<A> {
    static <A extends Serializable> Codec<A> of(final Decoder<A> decoder) {
        return of(Encoder.of(), decoder);
    }

    static <A extends Serializable> Codec<A> of(final Terminal<A> terminal) {
        return of(Encoder.of(), terminal.decoder());
    }

    static <A extends Serializable> Codec<A> of(final Boxed<A> boxed) {
        return of(Encoder.of(), boxed.decoder());
    }

    static <A extends Serializable> Codec<A> of(final Simple<A> simple) {
        return of(Encoder.of(), simple.decoder());
    }

    static <A> Codec<A> of(final Encoder<A> encoder, final Terminal<A> terminal) {
        return of(encoder, terminal.decoder());
    }

    static <A> Codec<A> of(final Encoder<A> encoder, final Boxed<A> boxed) {
        return of(encoder, boxed.decoder());
    }

    static <A> Codec<A> of(final Encoder<A> encoder, final Simple<A> terminal) {
        return of(encoder, terminal.decoder());
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
