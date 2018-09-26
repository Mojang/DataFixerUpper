// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.functions;

import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.templates.RecursivePoint;

import java.util.Objects;
import java.util.function.Function;

final class Out<A> extends PointFree<Function<A, A>> {
    private final RecursivePoint.RecursivePointType<A> type;

    public Out(final RecursivePoint.RecursivePointType<A> type) {
        this.type = type;
    }

    @Override
    public String toString(final int level) {
        return "Out[" + type + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof Out<?> && Objects.equals(type, ((Out<?>) obj).type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public Function<DynamicOps<?>, Function<A, A>> eval() {
        return ops -> Function.identity();
    }
}
