// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import java.util.function.Function;

public interface Functor<F extends K1, Mu extends Functor.Mu> extends Kind1<F, Mu> {
    static <F extends K1, Mu extends Functor.Mu> Functor<F, Mu> unbox(final App<Mu, F> proofBox) {
        return (Functor<F, Mu>) proofBox;
    }

    interface Mu extends Kind1.Mu {}

    <T, R> App<F, R> map(final Function<? super T, ? extends R> func, final App<F, T> ts);
}
