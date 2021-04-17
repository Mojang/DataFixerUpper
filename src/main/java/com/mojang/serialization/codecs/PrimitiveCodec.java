// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization.codecs;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public interface PrimitiveCodec<A> extends Codec<A> {
    <T> DataResult<A> read(final DynamicOps<T> ops, final T input);

    <T> T write(final DynamicOps<T> ops, final A value);

    @Override
    default <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
        return read(ops, input).map(r -> Pair.of(r, ops.empty()));
    }

    @Override
    default <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
        return ops.mergeToPrimitive(prefix, write(ops, input));
    }
}
