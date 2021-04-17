// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.CocartesianLike;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.util.Either;

public interface Cocartesian<P extends K2, Mu extends Cocartesian.Mu> extends Profunctor<P, Mu> {
    static <P extends K2, Proof extends Cocartesian.Mu> Cocartesian<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (Cocartesian<P, Proof>) proofBox;
    }

    interface Mu extends Profunctor.Mu {
        TypeToken<Mu> TYPE_TOKEN = new TypeToken<Mu>() {};
    }

    <A, B, C> App2<P, Either<A, C>, Either<B, C>> left(final App2<P, A, B> input);

    default <A, B, C> App2<P, Either<C, A>, Either<C, B>> right(final App2<P, A, B> input) {
        return dimap(left(input), Either::swap, Either::swap);
    }

    default FunctorProfunctor<CocartesianLike.Mu, P, FunctorProfunctor.Mu<CocartesianLike.Mu>> toFP() {
        return new FunctorProfunctor<CocartesianLike.Mu, P, FunctorProfunctor.Mu<CocartesianLike.Mu>>() {
            @Override
            public <A, B, F extends K1> App2<P, App<F, A>, App<F, B>> distribute(final App<? extends CocartesianLike.Mu, F> proof, final App2<P, A, B> input) {
                return cap(CocartesianLike.unbox(proof), input);
            }

            private <A, B, F extends K1, C> App2<P, App<F, A>, App<F, B>> cap(final CocartesianLike<F, C, ?> cLike, final App2<P, A, B> input) {
                return dimap(left(input), e -> Either.unbox(cLike.to(e)), cLike::from);
            }
        };
    }
}
