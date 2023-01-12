// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.util.Pair;

public final class Proj2<F, G, G2> implements Lens<Pair<F, G>, Pair<F, G2>, G, G2> {
    @Override
    public G view(final Pair<F, G> pair) {
        return pair.getSecond();
    }

    @Override
    public Pair<F, G2> update(final G2 newValue, final Pair<F, G> pair) {
        return Pair.of(pair.getFirst(), newValue);
    }

    @Override
    public String toString() {
        return "\u03C02";
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Proj2<?, ?, ?>;
    }
}
