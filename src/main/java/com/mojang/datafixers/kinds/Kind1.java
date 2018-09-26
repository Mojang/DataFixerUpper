// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

public interface Kind1<F extends K1, Mu extends Kind1.Mu> extends App<Mu, F> {
    static <F extends K1, Proof extends Kind1.Mu> Kind1<F, Proof> unbox(final App<Proof, F> proofBox) {
        return (Kind1<F, Proof>) proofBox;
    }

    interface Mu extends K1 {}
}
