// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import com.google.common.collect.ImmutableList;

import java.util.List;

public interface Monoid<T> {
    T point();

    T add(final T first, final T second);

    static <T> Monoid<List<T>> listMonoid() {
        // TODO: immutable list with structural sharing
        return new Monoid<List<T>>() {
            @Override
            public List<T> point() {
                return ImmutableList.of();
            }

            @Override
            public List<T> add(final List<T> first, final List<T> second) {
                final ImmutableList.Builder<T> builder = ImmutableList.builder();
                builder.addAll(first);
                builder.addAll(second);
                return builder.build();
            }
        };
    }
}
