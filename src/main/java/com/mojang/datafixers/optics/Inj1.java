// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.util.Either;

public final class Inj1<F, G, F2> implements Prism<Either<F, G>, Either<F2, G>, F, F2> {
    public static final Inj1<?, ?, ?> INSTANCE = new Inj1<>();

    private Inj1() {
    }

    @Override
    public Either<Either<F2, G>, F> match(final Either<F, G> either) {
        return either.map(Either::right, g -> Either.left(Either.right(g)));
    }

    @Override
    public Either<F2, G> build(final F2 f2) {
        return Either.left(f2);
    }

    @Override
    public String toString() {
        return "inj1";
    }
}
