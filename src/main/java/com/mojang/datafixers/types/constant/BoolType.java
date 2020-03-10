// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.types.templates.Const;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public final class BoolType extends Const.PrimitiveType<Boolean> {
    @Override
    public <T> DataResult<Pair<Boolean, T>> read(final DynamicOps<T> ops, final T input) {
        return ops
            .getNumberValue(input)
            .map(v -> DataResult.success(Pair.of(v.intValue() != 0, ops.empty())))
            .orElseGet(() -> DataResult.error("Input is not a number: " + input));
    }

    @Override
    public <T> T doWrite(final DynamicOps<T> ops, final Boolean value) {
        return ops.createBoolean(value);
    }

    @Override
    public String toString() {
        return "Bool";
    }
}
