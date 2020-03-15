// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization.codecs;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import java.util.Objects;

public class FieldCodec<A> implements MapCodec<A> {
    public static boolean REMOVE_FIELD_WHEN_PARSING = false;

    private final String name;
    private final Codec<A> elementCodec;

    public FieldCodec(final String name, final Codec<A> elementCodec) {
        this.name = name;
        this.elementCodec = elementCodec;
    }

    @Override
    public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
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
    public <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
        return elementCodec.encodeStart(ops, input).flatMap(result -> ops.mergeToMap(prefix, ops.createString(name), result));
    }

    @Override
    public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final T value = input.get(ops.createString(name));
        if (value == null) {
            return DataResult.error("No key " + name + " in " + input);
        }
        return elementCodec.parse(ops, value);
    }

    @Override
    public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
        return prefix.add(name, elementCodec.encodeStart(ops, input));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FieldCodec<?> that = (FieldCodec<?>) o;
        return Objects.equals(name, that.name) && Objects.equals(elementCodec, that.elementCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, elementCodec);
    }

    @Override
    public String toString() {
        return "FieldCodec[" + name + ": " + elementCodec + ']';
    }
}
