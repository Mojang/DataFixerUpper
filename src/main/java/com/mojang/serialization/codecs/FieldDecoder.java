// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization.codecs;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapLike;

import java.util.Objects;
import java.util.stream.Stream;

public class FieldDecoder<A> extends MapDecoder.Implementation<A> {
    public static boolean REMOVE_FIELD_WHEN_PARSING = false;

    protected final String name;
    private final Decoder<A> elementCodec;

    public FieldDecoder(final String name, final Decoder<A> elementCodec) {
        this.name = name;
        this.elementCodec = elementCodec;
    }

    @Override
    public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
        if (ops.compressMaps()) {
            return super.decode(ops, input);
        }
        return ops.getMap(input).flatMap(map -> decode(ops, map).map(r -> {
            final T output;
            if (REMOVE_FIELD_WHEN_PARSING) {
                final T nameObject = ops.createString(name);
                output = ops.createMap(map.entries().filter(e -> !Objects.equals(e.getFirst(), nameObject)));
            } else {
                output = input;
            }
            return Pair.of(r, output);
        }));
    }

    @Override
    public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final T value = input.get(name);
        if (value == null) {
            return DataResult.error("No key " + name + " in " + input);
        }
        return elementCodec.parse(ops, value);
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
        final FieldDecoder<?> that = (FieldDecoder<?>) o;
        return Objects.equals(name, that.name) && Objects.equals(elementCodec, that.elementCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, elementCodec);
    }

    @Override
    public String toString() {
        return "FieldDecoder[" + name + ": " + elementCodec + ']';
    }
}
