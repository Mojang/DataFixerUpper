// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization.codecs;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import java.util.Objects;
import java.util.stream.Stream;

public final class EitherMapCodec<F, S> extends MapCodec<Either<F, S>> {
    private final MapCodec<F> first;
    private final MapCodec<S> second;

    public EitherMapCodec(final MapCodec<F> first, final MapCodec<S> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public <T> DataResult<Either<F, S>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final DataResult<Either<F, S>> firstRead = first.decode(ops, input).map(Either::left);
        if (firstRead.result().isPresent()) {
            return firstRead;
        }
        return second.decode(ops, input).map(Either::right);
    }

    @Override
    public <T> RecordBuilder<T> encode(final Either<F, S> input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
        return input.map(
            value1 -> first.encode(value1, ops, prefix),
            value2 -> second.encode(value2, ops, prefix)
        );
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EitherMapCodec<?, ?> eitherCodec = ((EitherMapCodec<?, ?>) o);
        return Objects.equals(first, eitherCodec.first) && Objects.equals(second, eitherCodec.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "EitherMapCodec[" + first + ", " + second + ']';
    }

    @Override
    public <T> Stream<T> keys(final DynamicOps<T> ops) {
        return Stream.concat(first.keys(ops), second.keys(ops));
    }
}
