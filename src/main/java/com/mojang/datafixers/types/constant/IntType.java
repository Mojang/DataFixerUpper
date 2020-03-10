// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.types.templates.Const;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public final class IntType extends Const.PrimitiveType<Integer> {
    @Override
    public <T> DataResult<Pair<Integer, T>> read(final DynamicOps<T> ops, final T input) {
        return ops
            .getNumberValue(input)
            .map(v -> DataResult.success(Pair.of(v.intValue(), ops.empty())))
            .orElseGet(() -> DataResult.error("Input is not a number: " + input));
    }

    @Override
    public <T> T doWrite(final DynamicOps<T> ops, final Integer value) {
        return ops.createInt(value);
    }

    @Override
    public String toString() {
        return "Int";
    }
}
