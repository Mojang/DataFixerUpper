// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

public interface Compressable extends Keyable {
    <T> MapCompressor<T> compressor(final DynamicOps<T> ops);
}
