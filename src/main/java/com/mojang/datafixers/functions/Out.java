package com.mojang.datafixers.functions;

import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.templates.RecursivePoint;

import java.util.Objects;

final class Out<A> extends PointFreeFunction<A, A> {
    private final RecursivePoint.RecursivePointType<A> type;

    public Out(final RecursivePoint.RecursivePointType<A> type) {
        this.type = type;
    }

    @Override
    A eval(final DynamicOps<?> ops, final A input) {
        return input;
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
}
