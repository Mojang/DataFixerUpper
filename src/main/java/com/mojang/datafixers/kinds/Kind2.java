// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

public interface Kind2<F extends K2, Mu extends Kind2.Mu> extends App<Mu, F> {
    static <F extends K2, Proof extends Kind2.Mu> Kind2<F, Proof> unbox(final App<Proof, F> proofBox) {
        return (Kind2<F, Proof>) proofBox;
    }

    interface Mu extends K1 {}
}
