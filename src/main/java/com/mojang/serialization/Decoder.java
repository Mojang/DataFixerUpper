// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.util.Pair;

public interface Decoder<A> {
    <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input);
}
