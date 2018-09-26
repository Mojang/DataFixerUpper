// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.functions;

import com.mojang.datafixers.types.DynamicOps;

import java.util.function.Function;

final class Bang<A> extends PointFree<Function<A, Void>> {
    Bang() {
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

    @Override
    public Function<DynamicOps<?>, Function<A, Void>> eval() {
        return ops -> a -> null;
    }
}
