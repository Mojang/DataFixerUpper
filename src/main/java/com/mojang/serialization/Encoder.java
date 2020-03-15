// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import java.util.stream.Stream;

public interface Encoder<A> {
    <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix);

    default <T> DataResult<T> encodeStart(final DynamicOps<T> ops, final A input) {
        return encode(input, ops, ops.empty());
    }

    static <A extends Serializable> Encoder<A> of() {
        return new Encoder<A>() {
            @Override
            public <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
                return input.serialize(ops, prefix);
            }

            @Override
            public String toString() {
                return "SerializableEncoder";
            }
        };
    }

    static <A> MapEncoder<A> empty() {
        return new MapEncoder.Implementation<A>() {
            @Override
            public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                return prefix;
            }

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return Stream.empty();
            }

            @Override
            public <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
                return DataResult.success(prefix);
            }

            @Override
            public String toString() {
                return "EmptyEncoder";
            }
        };
    }

    static <A> Encoder<A> error(final String error) {
        return new Encoder<A>() {
            @Override
            public <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
                return DataResult.error(error + " " + input);
            }

            @Override
            public String toString() {
                return "EmptyEncoder[" + error + "]";
            }
        };
    }
}
