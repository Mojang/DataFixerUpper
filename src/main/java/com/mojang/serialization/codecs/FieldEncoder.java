// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization.codecs;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.RecordBuilder;

import java.util.Objects;
import java.util.stream.Stream;

public class FieldEncoder<A> extends MapEncoder.Implementation<A> {
    private final String name;
    private final Encoder<A> elementCodec;

    public FieldEncoder(final String name, final Encoder<A> elementCodec) {
        this.name = name;
        this.elementCodec = elementCodec;
    }

    @Override
    public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
        return prefix.add(name, elementCodec.encodeStart(ops, input));
    }

    @Override
    public <T> Stream<T> keys(final DynamicOps<T> ops) {
        return Stream.of(ops.createString(name));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FieldEncoder<?> that = (FieldEncoder<?>) o;
        return Objects.equals(name, that.name) && Objects.equals(elementCodec, that.elementCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, elementCodec);
    }

    @Override
    public String toString() {
        return "FieldEncoder[" + name + ": " + elementCodec + ']';
    }
}
