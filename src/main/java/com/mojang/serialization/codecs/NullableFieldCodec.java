// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization.codecs;

import com.mojang.serialization.*;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

/** Optional Nullable Field Codec */
public class NullableFieldCodec<A> extends MapCodec<@Nullable A> {
    private final String name;
    private final Codec<A> elementCodec;
    private final boolean lenient;

    public NullableFieldCodec(final String name, final Codec<A> elementCodec, final boolean lenient) {
        this.name = name;
        this.elementCodec = elementCodec;
        this.lenient = lenient;
    }

    @Override
    public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final T value = input.get(name);
        if (value == null) {
            return DataResult.success(null);
        }
        final DataResult<A> parsed = elementCodec.parse(ops, value);
        if (parsed.isError()) {
            if (lenient) {
                return DataResult.success(null, parsed.lifecycle());
            }
            return parsed.hasResultOrPartial() ? parsed : parsed.setPartial((A) null);
        }
        return parsed;
    }

    @Override
    public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
        if (input != null) {
            return prefix.add(name, elementCodec.encodeStart(ops, input));
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
        final NullableFieldCodec<?> that = (NullableFieldCodec<?>) o;
        return Objects.equals(name, that.name) && Objects.equals(elementCodec, that.elementCodec) && lenient == that.lenient;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, elementCodec, lenient);
    }

    @Override
    public String toString() {
        return "NullableField[" + name + ": " + elementCodec + "]";
    }
}
