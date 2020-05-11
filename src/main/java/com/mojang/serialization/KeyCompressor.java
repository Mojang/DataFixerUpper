// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.stream.Stream;

public final class KeyCompressor<T> {
    private final Int2ObjectMap<T> decompress = new Int2ObjectArrayMap<>();
    private final Object2IntMap<T> compress = new Object2IntArrayMap<>();
    private final Object2IntMap<String> compressString = new Object2IntArrayMap<>();
    private final int size;
    private final DynamicOps<T> ops;

    public KeyCompressor(final DynamicOps<T> ops, final Stream<T> keyStream) {
        this.ops = ops;

        compressString.defaultReturnValue(-1);

        keyStream.forEach(key -> {
            if (compress.containsKey(key)) {
                return;
            }
            final int next = compress.size();
            compress.put(key, next);
            ops.getStringValue(key).result().ifPresent(k ->
                compressString.put(k, next)
            );
            decompress.put(next, key);
        });

        size = compress.size();
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
