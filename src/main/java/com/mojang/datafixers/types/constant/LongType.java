// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.types.templates.Const;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public final class LongType extends Const.PrimitiveType<Long> {
    @Override
    public <T> DataResult<Pair<Long, T>> read(final DynamicOps<T> ops, final T input) {
        return ops
            .getNumberValue(input)
            .map(v -> Pair.of(v.longValue(), ops.empty()));
    }

    @Override
    public <T> T doWrite(final DynamicOps<T> ops, final Long value) {
        return ops.createLong(value);
    }

    @Override
    public String toString() {
        return "Long";
    }
}
