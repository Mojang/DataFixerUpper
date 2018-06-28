package com.mojang.datafixers.functions;

import com.mojang.datafixers.types.DynamicOps;

final class Bang<A> extends PointFreeFunction<A, Void> {
    Bang() {
    }

    @Override
    public Void eval(final DynamicOps<?> ops, final A input) {
        return null;
    }

    @Override
    public String toString(final int level) {
        return "!";
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Bang<?>;
    }

    @Override
    public int hashCode() {
        return Bang.class.hashCode();
    }
}
