// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.types.templates.Const;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public final class DoubleType extends Const.PrimitiveType<Double> {
    @Override
    public <T> DataResult<Pair<Double, T>> read(final DynamicOps<T> ops, final T input) {
        return ops
            .getNumberValue(input)
            .map(v -> DataResult.success(Pair.of(v.doubleValue(), ops.empty())))
            .orElseGet(() -> DataResult.error("Input is not a number: " + input));
    }

    @Override
    public <T> T doWrite(final DynamicOps<T> ops, final Double value) {
        return ops.createDouble(value);
    }

    @Override
    public String toString() {
        return "Double";
    }
}
