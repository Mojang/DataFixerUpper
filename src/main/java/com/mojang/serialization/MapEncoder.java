// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import java.util.stream.Stream;

public interface MapEncoder<A> extends Encoder<A> {
    <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix);

    <T> Stream<T> keys(final DynamicOps<T> ops);

    @Override
    default <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
        return encode(input, ops, ops.mapBuilder()).build(prefix);
    }
}
