// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization.codecs;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class ListCodec<A> implements Codec<List<A>> {
    private final Codec<A> elementCodec;

    public ListCodec(final Codec<A> elementCodec) {
        this.elementCodec = elementCodec;
    }

    @Override
    public <T> DataResult<T> encode(final DynamicOps<T> ops, final T prefix, final java.util.List<A> input) {
        final java.util.List<T> list = new ArrayList<>(input.size());
        DataResult<java.util.List<T>> result = DataResult.success(list);

        for (final A a : input) {
            result = result.flatMap(t -> {
                final DataResult<T> written = elementCodec.encode(ops, ops.empty(), a);
                return written.map(e -> {
                    list.add(e);
                    return list;
                });
            });
        }

        return result.flatMap(l -> ops.mergeToList(prefix, l));
    }

    @Override
    public <T> DataResult<Pair<List<A>, T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getStream(input).flatMap(stream -> {
            final AtomicReference<DataResult<Pair<ImmutableList.Builder<A>, ImmutableList.Builder<T>>>> result =
                new AtomicReference<>(DataResult.success(Pair.of(ImmutableList.builder(), ImmutableList.builder())));

            stream.forEach(t ->
                result.set(result.get().flatMap(pair -> {
                    final DataResult<Pair<A, T>> read = elementCodec.decode(ops, t);

                    read.error().ifPresent(e -> pair.getSecond().add(t));

                    return read.map(value -> {
                        pair.getFirst().add(value.getFirst());
                        return pair;
                    });
                }))
            );

            return result.get().map(pair -> Pair.of(pair.getFirst().build(), ops.createList(pair.getSecond().build().stream())));
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
