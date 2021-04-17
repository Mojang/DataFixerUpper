// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.util.Either;

public interface ReCocartesian<P extends K2, Mu extends ReCocartesian.Mu> extends Profunctor<P, Mu> {
    static <P extends K2, Proof extends ReCocartesian.Mu> ReCocartesian<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (ReCocartesian<P, Proof>) proofBox;
    }

    interface Mu extends Profunctor.Mu {}

    <A, B, C> App2<P, A, B> unleft(final App2<P, Either<A, C>, Either<B, C>> input);

    <A, B, C> App2<P, A, B> unright(final App2<P, Either<C, A>, Either<C, B>> input);
}
