// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.util.Pair;

import java.util.function.Supplier;

public interface Monoidal<P extends K2, Mu extends Monoidal.Mu> extends Profunctor<P, Mu> {
    static <P extends K2, Proof extends Monoidal.Mu> Monoidal<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (Monoidal<P, Proof>) proofBox;
    }

    interface Mu extends Profunctor.Mu {}

    <A, B, C, D> App2<P, Pair<A, C>, Pair<B, D>> par(final App2<P, A, B> first, final Supplier<App2<P, C, D>> second);

    App2<P, Void, Void> empty();

    /*default <R extends K1, I, A extends KK1, B extends KK1, C extends KK1, D extends KK1> App2<P, App<HApp<TypeFamilyContext.PairMu<A, C>, R>, I>, App<HApp<TypeFamilyContext.PairMu<B, D>, R>, I>> parF(final App2<P, App<HApp<A, R>, I>, App<HApp<B, R>, I>> first, final App2<P, App<HApp<C, R>, I>, App<HApp<D, R>, I>> second) {
        return dimap(
            par(first, second),
            TypeFamilyContext::unLift,
            TypeFamilyContext::lift
        );
    }

    default <R extends K1, I> App2<P, App<HApp<TypeFamilyContext.ConstMu<Void>, R>, I>, App<HApp<TypeFamilyContext.ConstMu<Void>, R>, I>> emptyF() {
        return dimap(empty(), TypeFamilyContext::getConst, TypeFamilyContext::constant);
    }*/
}
