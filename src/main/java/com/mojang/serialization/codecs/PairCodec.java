// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization.codecs;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.Objects;

public final class PairCodec<F, S> implements Codec<Pair<F, S>> {
    private final Codec<F> first;
    private final Codec<S> second;

    public PairCodec(final Codec<F> first, final Codec<S> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public <T> DataResult<Pair<Pair<F, S>, T>> decode(final DynamicOps<T> ops, final T input) {
        return first.decode(ops, input).flatMap(p1 ->
            second.decode(ops, p1.getSecond()).map(p2 ->
                Pair.of(Pair.of(p1.getFirst(), p2.getFirst()), p2.getSecond())
            )
        );
    }

    @Override
    public <T> DataResult<T> encode(final Pair<F, S> value, final DynamicOps<T> ops, final T rest) {
        return second.encode(value.getSecond(), ops, rest).flatMap(f -> first.encode(value.getFirst(), ops, f));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PairCodec<?, ?> pairCodec = (PairCodec<?, ?>) o;
        return Objects.equals(first, pairCodec.first) && Objects.equals(second, pairCodec.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "PairCodec[" + first + ", " + second + ']';
    }
}
