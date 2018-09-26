// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.kinds.Kind2;

public interface FunctorProfunctor<T extends K1, P extends K2, Mu extends FunctorProfunctor.Mu<T>> extends Kind2<P, Mu> {
    static <T extends K1, P extends K2, Mu extends FunctorProfunctor.Mu<T>> FunctorProfunctor<T, P, Mu> unbox(final App<Mu, P> proofBox) {
        return (FunctorProfunctor<T, P, Mu>) proofBox;
    }

    interface Mu<T extends K1> extends Kind2.Mu {}

    <A, B, F extends K1> App2<P, App<F, A>, App<F, B>> distribute(final App<? extends T, F> proof, final App2<P, A, B> input);
}
