// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.Functor;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;

public interface Mapping<P extends K2, Mu extends Mapping.Mu> extends TraversalP<P, Mu> {
    static <P extends K2, Proof extends Mapping.Mu> Mapping<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (Mapping<P, Proof>) proofBox;
    }

    interface Mu extends TraversalP.Mu {}

    <A, B, F extends K1> App2<P, App<F, A>, App<F, B>> mapping(final Functor<F, ?> functor, final App2<P, A, B> input);
}
