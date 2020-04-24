// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization.codecs;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class ListCodec<A> implements Codec<List<A>> {
    private final Codec<A> elementCodec;

    public ListCodec(final Codec<A> elementCodec) {
        this.elementCodec = elementCodec;
    }

    @Override
    public <T> DataResult<T> encode(final List<A> input, final DynamicOps<T> ops, final T prefix) {
        final ListBuilder<T> builder = ops.listBuilder();

        for (final A a : input) {
            builder.add(elementCodec.encodeStart(ops, a));
        }

        return builder.build(prefix);
    }

    @Override
    public <T> DataResult<Pair<List<A>, T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getList(input).flatMap(stream -> {
            final ImmutableList.Builder<A> read = ImmutableList.builder();
            final ImmutableList.Builder<T> failed = ImmutableList.builder();
            final AtomicReference<DataResult<Unit>> result =
                new AtomicReference<>(DataResult.success(Unit.INSTANCE));

            stream.accept(t -> {
                final DataResult<Pair<A, T>> element = elementCodec.decode(ops, t);
                element.error().ifPresent(e -> failed.add(t));
                result.set(result.get().apply2((r, v) -> {
                    read.add(v.getFirst());
                    return r;
                }, element));
            });

            final ImmutableList<A> elements = read.build();
            final T errors = ops.createList(failed.build().stream());

            final Pair<List<A>, T> pair = Pair.of(elements, errors);

            return result.get().map(unit -> pair).setPartial(pair);
        });
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ListCodec<?> listCodec = (ListCodec<?>) o;
        return Objects.equals(elementCodec, listCodec.elementCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementCodec);
    }

    @Override
    public String toString() {
        return "ListCodec[" + elementCodec + ']';
    }
}
