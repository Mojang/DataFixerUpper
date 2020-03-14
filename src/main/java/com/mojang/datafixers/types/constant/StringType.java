// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.types.templates.Const;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public final class StringType extends Const.PrimitiveType<String> {
    @Override
    public <T> DataResult<String> read(final DynamicOps<T> ops, final T input) {
        return ops
            .getStringValue(input);
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
