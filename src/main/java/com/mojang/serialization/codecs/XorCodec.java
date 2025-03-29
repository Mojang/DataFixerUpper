// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization.codecs;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.Optional;

public record XorCodec<F, S>(Codec<F> first, Codec<S> second) implements Codec<Either<F, S>> {
    @Override
    public <T> DataResult<Pair<Either<F, S>, T>> decode(final DynamicOps<T> ops, final T input) {
        final DataResult<Pair<Either<F, S>, T>> firstRead = first.decode(ops, input).map(vo -> vo.mapFirst(Either::left));
        final DataResult<Pair<Either<F, S>, T>> secondRead = second.decode(ops, input).map(vo -> vo.mapFirst(Either::right));
        final Optional<Pair<Either<F, S>, T>> firstResult = firstRead.result();
        final Optional<Pair<Either<F, S>, T>> secondResult = secondRead.result();
        if (firstResult.isPresent() && secondResult.isPresent()) {
            return DataResult.error(() -> "Both alternatives read successfully, can not pick the correct one; first: " + firstResult.get() + " second: " + secondResult.get(), firstResult.get());
        }
        if (firstResult.isPresent()) {
            return firstRead;
        }
        if (secondResult.isPresent()) {
            return secondRead;
        }
        return firstRead.apply2((f, s) -> s, secondRead);
    }

    @Override
    public <T> DataResult<T> encode(final Either<F, S> input, final DynamicOps<T> ops, final T prefix) {
        return input.map(
            value1 -> first.encode(value1, ops, prefix),
            value2 -> second.encode(value2, ops, prefix)
        );
    }
}
