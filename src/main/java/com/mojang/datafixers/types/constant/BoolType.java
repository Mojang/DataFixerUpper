// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.types.templates.Const;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public final class BoolType extends Const.PrimitiveType<Boolean> {
    @Override
    public <T> DataResult<Boolean> read(final DynamicOps<T> ops, final T input) {
        return ops
            .getNumberValue(input)
            .map(v -> v.intValue() != 0);
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
