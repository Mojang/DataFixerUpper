package com.mojang.datafixers.functions;

import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.templates.RecursivePoint;

import java.util.Objects;

final class In<A> extends PointFreeFunction<A, A> {
    protected final RecursivePoint.RecursivePointType<A> type;

    public In(final RecursivePoint.RecursivePointType<A> type) {
        this.type = type;
    }

    @Override
    A eval(final DynamicOps<?> ops, final A input) {
        return input;
    }

    @Override
    public String toString(final int level) {
        return "In[" + type + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof In<?> && Objects.equals(type, ((In<?>) obj).type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
