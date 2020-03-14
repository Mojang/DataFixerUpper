// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

public interface Encoder<A> {
    <T> DataResult<T> encode(final DynamicOps<T> ops, final T prefix, final A input);

    default <T> DataResult<T> encodeStart(final DynamicOps<T> ops, final A input) {
        return encode(ops, ops.empty(), input);
    }

    static <A extends Serializable> Encoder<A> of() {
        return new Encoder<A>() {
            @Override
            public <T> DataResult<T> encode(final DynamicOps<T> ops, final T prefix, final A input) {
                return input.serialize(ops, prefix);
            }

            @Override
            public String toString() {
                return "SerializableEncoder";
            }
        };
    }
}
