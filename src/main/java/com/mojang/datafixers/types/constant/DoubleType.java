// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.templates.Const;

import java.util.Optional;

public final class DoubleType extends Const.ConstType<Double> {
    @Override
    public <T> Pair<T, Optional<Double>> read(final DynamicOps<T> ops, final T input) {
        return ops
            .getNumberValue(input)
            .map(v -> Pair.of(ops.empty(), Optional.of(v.doubleValue())))
            .orElseGet(() -> Pair.of(input, Optional.empty()));
    }

    @Override
    public <T> T write(final DynamicOps<T> ops, final T rest, final Double value) {
        return ops.createDouble(value);
    }

    @Override
    public String toString() {
        return "Double";
    }
}
