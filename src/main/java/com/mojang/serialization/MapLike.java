// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.util.Pair;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Stream;

public interface MapLike<T> {
    @Nullable
    T get(final T key);

    Stream<Pair<T, T>> entries();

    static <T> MapLike<T> forMap(final Map<T, T> map) {
        return new MapLike<T>() {
            @Nullable
            @Override
            public T get(final T key) {
                return map.get(key);
            }

            @Override
            public Stream<Pair<T, T>> entries() {
                return map.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue()));
            }

            @Override
            public String toString() {
                return "MapLike[" + map + "]";
            }
        };
    }
}
