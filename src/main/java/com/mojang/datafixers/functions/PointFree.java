// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.functions;

import com.google.common.base.Strings;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

public abstract class PointFree<T> {
    private boolean initialized;
    @Nullable
    private Function<DynamicOps<?>, T> value;

    @SuppressWarnings("ConstantConditions")
    public Function<DynamicOps<?>, T> evalCached() {
        if (!initialized) {
            initialized = true;
            value = eval();
        }
        return value;
    }

    public abstract Function<DynamicOps<?>, T> eval();

    Optional<? extends PointFree<T>> all(final PointFreeRule rule, final Type<T> type) {
        return Optional.of(this);
    }

    Optional<? extends PointFree<T>> one(final PointFreeRule rule, final Type<T> type) {
        return Optional.empty();
    }

    @Override
    public final String toString() {
        return toString(0);
    }

    public static String indent(final int level) {
        return Strings.repeat("  ", level);
    }

    public abstract String toString(int level);
}
