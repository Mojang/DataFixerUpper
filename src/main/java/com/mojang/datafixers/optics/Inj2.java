// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.util.Either;

public final class Inj2<F, G, G2> implements Prism<Either<F, G>, Either<F, G2>, G, G2> {
    public static final Inj2<?, ?, ?> INSTANCE = new Inj2<>();

    private Inj2() {
    }

    @Override
    public Either<Either<F, G2>, G> match(final Either<F, G> either) {
        return either.map(f -> Either.left(Either.left(f)), Either::right);
    }

    @Override
    public Either<F, G2> build(final G2 g2) {
        return Either.right(g2);
    }

    @Override
    public String toString() {
        return "inj2";
    }
}
