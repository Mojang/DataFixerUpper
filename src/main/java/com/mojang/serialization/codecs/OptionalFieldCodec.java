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
import java.util.Optional;
import java.util.stream.Stream;

/** Optimization of `Codec.either(someCodec.field(name), Codec.EMPTY)` */
public class OptionalFieldCodec<A> extends MapCodec<Optional<A>> {
    private final String name;
    private final Codec<A> elementCodec;

    public OptionalFieldCodec(final String name, final Codec<A> elementCodec) {
        this.name = name;
        this.elementCodec = elementCodec;
    }

    @Override
    public <T> DataResult<Pair<Optional<A>, T>> decode(final DynamicOps<T> ops, final T input) {
        if (ops.compressMaps()) {
            return super.decode(ops, input);
        }
        return ops.getMap(input).flatMap(map -> decode(ops, map).map(r -> {
            final T output;
            if (FieldDecoder.REMOVE_FIELD_WHEN_PARSING) {
                final T nameObject = ops.createString(name);
                output = ops.createMap(map.entries().filter(e -> !Objects.equals(e.getFirst(), nameObject)));
            } else {
                output = input;
            }
            return Pair.of(r, output);
        }));
    }

    @Override
    public <T> DataResult<T> encode(final Optional<A> input, final DynamicOps<T> ops, final T prefix) {
        if (ops.compressMaps()) {
            return super.encode(input, ops, prefix);
        }
        if (input.isPresent()) {
            return elementCodec.encodeStart(ops, input.get()).flatMap(result -> ops.mergeToMap(prefix, ops.createString(name), result));
        }
        return DataResult.success(prefix);
    }

    @Override
    public <T> DataResult<Optional<A>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final T value = input.get(name);
        if (value == null) {
            return DataResult.success(Optional.empty());
        }
        final DataResult<A> parsed = elementCodec.parse(ops, value);
        if (parsed.result().isPresent()) {
            return parsed.map(Optional::of);
        }
        return DataResult.success(Optional.empty());
    }

    @Override
    public <T> RecordBuilder<T> encode(final Optional<A> input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
        if (input.isPresent()) {
            return prefix.add(name, elementCodec.encodeStart(ops, input.get()));
        }
        return prefix;
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
        final OptionalFieldCodec<?> that = (OptionalFieldCodec<?>) o;
        return Objects.equals(name, that.name) && Objects.equals(elementCodec, that.elementCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, elementCodec);
    }

    @Override
    public String toString() {
        return "OptionalFieldCodec[" + name + ": " + elementCodec + ']';
    }
}
