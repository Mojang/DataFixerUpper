// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.kinds.Kind2;

import java.util.function.Function;
import java.util.function.Supplier;

interface Bicontravariant<P extends K2, Mu extends Bicontravariant.Mu> extends Kind2<P, Mu> {
    static <P extends K2, Proof extends Bicontravariant.Mu> Bicontravariant<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (Bicontravariant<P, Proof>) proofBox;
    }

    interface Mu extends Kind2.Mu {}

    <A, B, C, D> FunctionType<Supplier<App2<P, A, B>>, App2<P, C, D>> cimap(final Function<C, A> g, final Function<D, B> h);

    default <A, B, C, D> App2<P, C, D> cimap(final Supplier<App2<P, A, B>> arg, final Function<C, A> g, final Function<D, B> h) {
        return cimap(g, h).apply(arg);
    }
}
