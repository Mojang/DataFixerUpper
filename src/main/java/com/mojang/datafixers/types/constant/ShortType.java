// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.types.templates.Const;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public final class ShortType extends Const.PrimitiveType<Short> {
    @Override
    public <T> DataResult<Short> read(final DynamicOps<T> ops, final T input) {
        return ops
            .getNumberValue(input)
            .map(Number::shortValue);
    }

    @Override
    public <T> T doWrite(final DynamicOps<T> ops, final Short value) {
        return ops.createShort(value);
    }

    @Override
    public String toString() {
        return "Short";
    }
}
