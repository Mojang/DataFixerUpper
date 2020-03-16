// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MapCompressor<T> {
    private final Int2ObjectMap<T> decompress = new Int2ObjectArrayMap<>();
    private final Object2IntMap<T> compress = new Object2IntArrayMap<>();
    private final Object2IntMap<String> compressString = new Object2IntArrayMap<>();
    private final int size;

    public MapCompressor(final DynamicOps<T> ops, final Stream<T> keyStream) {
        final List<String> keys = keyStream.map(k -> ops.getStringValue(k).result().get()).distinct().sorted(Comparator.naturalOrder()).collect(Collectors.toList());

        for (int i = 0; i < keys.size(); i++) {
            final String string = keys.get(i);
            final T key = ops.createString(string);
            compress.put(key, i);
            compressString.put(string, i);
            decompress.put(i, key);
        }

        size = keys.size();
    }

    public T decompress(final int key) {
        return decompress.get(key);
    }

    public int compress(final String key) {
        return compressString.getInt(key);
    }

    public int compress(final T key) {
        return compress.get(key);
    }

    public int size() {
        return size;
    }
}
