// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

public interface Serializable {
    <T> DataResult<T> serialize(final DynamicOps<T> ops, final T prefix);
}
