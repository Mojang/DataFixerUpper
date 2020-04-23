// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MapCompressor<T> {
    private final Int2ObjectMap<T> decompress = new Int2ObjectArrayMap<>();
    private final Object2IntMap<T> compress = new Object2IntArrayMap<>();
    private final Object2IntMap<String> compressString = new Object2IntArrayMap<>();
    private final int size;
    private final DynamicOps<T> ops;

    public MapCompressor(final DynamicOps<T> ops, final Stream<T> keyStream) {
        this.ops = ops;
        final List<T> keys = keyStream.distinct().collect(Collectors.toList());

        compressString.defaultReturnValue(-1);

        for (int i = 0; i < keys.size(); i++) {
            final T key = keys.get(i);
            compress.put(key, i);
            final int finalI = i;
            ops.getStringValue(key).result().ifPresent(k ->
                compressString.put(k, finalI)
            );
            decompress.put(i, key);
        }

        size = keys.size();
    }

    public T decompress(final int key) {
        return decompress.get(key);
    }

    public int compress(final String key) {
        final int id = compressString.getInt(key);
        return id == -1 ? compress(ops.createString(key)) : id;
    }

    public int compress(final T key) {
        return compress.get(key);
    }

    public int size() {
        return size;
    }
}
