// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.google.common.base.Function;
import com.mojang.datafixers.types.templates.Const;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

// FIXME: move out of this project?
public final class NamespacedStringType extends Const.PrimitiveType<String> {
    public static Function<String, String> ENSURE_NAMESPACE = s -> s;

    @Override
    public <T> DataResult<Pair<String, T>> read(final DynamicOps<T> ops, final T input) {
        return ops
            .getStringValue(input)
            .map(v -> Pair.of(ENSURE_NAMESPACE.apply(v), ops.empty()));
    }

    @Override
    public <T> T doWrite(final DynamicOps<T> ops, final String value) {
        return ops.createString(value);
    }

    @Override
    public String toString() {
        return "NamespacedString";
    }
}
