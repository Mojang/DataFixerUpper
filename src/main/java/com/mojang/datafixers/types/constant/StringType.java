// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.types.templates.Const;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;

import java.util.Optional;

public final class StringType extends Const.PrimitiveType<String> {
    @Override
    public <T> Pair<T, Optional<String>> read(final DynamicOps<T> ops, final T input) {
        return ops
            .getStringValue(input)
            .map(v -> Pair.of(ops.empty(), Optional.of(v)))
            .orElseGet(() -> Pair.of(input, Optional.empty()));
    }

    @Override
    public <T> T doWrite(final DynamicOps<T> ops, final String value) {
        return ops.createString(value);
    }

    @Override
    public String toString() {
        return "String";
    }
}
