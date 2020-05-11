// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.Map;

public abstract class CompressorHolder implements Compressable {
    private final Map<DynamicOps<?>, MapCompressor<?>> compressors = new Object2ObjectArrayMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> MapCompressor<T> compressor(final DynamicOps<T> ops) {
        return (MapCompressor<T>) compressors.computeIfAbsent(ops, k -> new MapCompressor<>(ops, keys(ops)));
    }
}
