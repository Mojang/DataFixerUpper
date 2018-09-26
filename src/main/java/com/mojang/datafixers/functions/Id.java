// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.functions;

import com.mojang.datafixers.types.DynamicOps;

import java.util.function.Function;

final class Id<A> extends PointFree<Function<A, A>> {
    public Id() {
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof com.mojang.datafixers.functions.Id<?>;
    }

    @Override
    public int hashCode() {
        return Id.class.hashCode();
    }

    @Override
    public String toString(final int level) {
        return "id";
    }

    @Override
    public Function<DynamicOps<?>, Function<A, A>> eval() {
        return ops -> Function.identity();
    }
}
