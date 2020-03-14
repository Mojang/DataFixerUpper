// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.types.templates.Const;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public final class FloatType extends Const.PrimitiveType<Float> {
    @Override
    public <T> DataResult<Float> read(final DynamicOps<T> ops, final T input) {
        return ops
            .getNumberValue(input)
            .map(Number::floatValue);
    }

    @Override
    public <T> T doWrite(final DynamicOps<T> ops, final Float value) {
        return ops.createFloat(value);
    }

    @Override
    public String toString() {
        return "Float";
    }
}
