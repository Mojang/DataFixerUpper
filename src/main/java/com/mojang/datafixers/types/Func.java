// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types;

import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;

import java.util.Objects;
import java.util.function.Function;

public final class Func<A, B> extends Type<Function<A, B>> {
    protected final Type<A> first;
    protected final Type<B> second;

    public Func(final Type<A> first, final Type<B> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public TypeTemplate buildTemplate() {
        throw new UnsupportedOperationException("No template for function types.");
    }

    @Override
    protected Codec<Function<A, B>> buildCodec() {
        return Codec.of(
            Encoder.error("Cannot save a function"),
            Decoder.error("Cannot read a function")
        );
    }

    @Override
    public String toString() {
        return "(" + first + " -> " + second + ")";
    }

    @Override
    public boolean equals(final Object obj, final boolean ignoreRecursionPoints, final boolean checkIndex) {
        if (!(obj instanceof Func<?, ?>)) {
            return false;
        }
        final Func<?, ?> that = (Func<?, ?>) obj;
        return first.equals(that.first, ignoreRecursionPoints, checkIndex) && second.equals(that.second, ignoreRecursionPoints, checkIndex);
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        return result;
    }

    public Type<A> first() {
        return first;
    }

    public Type<B> second() {
        return second;
    }
}
