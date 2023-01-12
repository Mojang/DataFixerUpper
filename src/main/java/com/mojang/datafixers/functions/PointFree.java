// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.functions;

import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DynamicOps;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

public abstract class PointFree<T> {
    private volatile boolean initialized;
    @Nullable
    private Function<DynamicOps<?>, T> value;

    @SuppressWarnings("ConstantConditions")
    public Function<DynamicOps<?>, T> evalCached() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    value = eval();
                    initialized = true;
                }
            }
        }
        return value;
    }

    public abstract Type<T> type();

    public abstract Function<DynamicOps<?>, T> eval();

    Optional<? extends PointFree<T>> all(final PointFreeRule rule) {
        return Optional.of(this);
    }

    Optional<? extends PointFree<T>> one(final PointFreeRule rule) {
        return Optional.empty();
    }

    @Override
    public final String toString() {
        return toString(0);
    }

    public static String indent(final int level) {
        return StringUtils.repeat("  ", level);
    }

    public abstract String toString(int level);
}
