// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.google.common.base.Function;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.templates.Const;
import com.mojang.datafixers.util.Pair;

import java.util.Optional;

public final class NamespacedStringType extends Const.ConstType<String> {
    public static Function<String, String> ENSURE_NAMESPACE = s -> s;

    @Override
    public <T> Pair<T, Optional<String>> read(final DynamicOps<T> ops, final T input) {
        return ops
            .getStringValue(input)
            .map(v -> Pair.of(ops.empty(), Optional.of(ENSURE_NAMESPACE.apply(v))))
            .orElseGet(() -> Pair.of(input, Optional.empty()));
    }

    @Override
    public <T> T write(final DynamicOps<T> ops, final T rest, final String value) {
        return ops.createString(value);
    }

    @Override
    public String toString() {
        return "NamespacedString";
    }
}
