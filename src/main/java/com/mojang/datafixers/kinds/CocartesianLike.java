// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import com.mojang.datafixers.util.Either;

import java.util.function.Function;

public interface CocartesianLike<T extends K1, C, Mu extends CocartesianLike.Mu> extends Functor<T, Mu>, Traversable<T, Mu> {
    static <F extends K1, C, Mu extends CocartesianLike.Mu> CocartesianLike<F, C, Mu> unbox(final App<Mu, F> proofBox) {
        return (CocartesianLike<F, C, Mu>) proofBox;
    }

    interface Mu extends Functor.Mu, Traversable.Mu {}

    <A> App<Either.Mu<C>, A> to(final App<T, A> input);

    <A> App<T, A> from(final App<Either.Mu<C>, A> input);

    @Override
    default <F extends K1, A, B> App<F, App<T, B>> traverse(final Applicative<F, ?> applicative, final Function<A, App<F, B>> function, final App<T, A> input) {
        return applicative.map(this::from, new Either.Instance<C>().traverse(applicative, function, to(input)));
    }
}
