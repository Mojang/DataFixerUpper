// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

public interface Encoder<A> {
    <T> DataResult<T> encode(final DynamicOps<T> ops, final T prefix, final A input);
}
