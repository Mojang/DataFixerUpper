// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import com.mojang.datafixers.FunctionType;

public interface Representable<T extends K1, C, Mu extends Representable.Mu> extends Functor<T, Mu> {
    static <F extends K1, C, Mu extends Representable.Mu> Representable<F, C, Mu> unbox(final App<Mu, F> proofBox) {
        return (Representable<F, C, Mu>) proofBox;
    }

    interface Mu extends Functor.Mu {}

    <A> App<FunctionType.ReaderMu<C>, A> to(final App<T, A> input);

    <A> App<T, A> from(final App<FunctionType.ReaderMu<C>, A> input);
}
