// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.Cocartesian;
import com.mojang.datafixers.util.Either;

import java.util.function.Function;

public interface Prism<S, T, A, B> extends App2<Prism.Mu<A, B>, S, T>, Optic<Cocartesian.Mu, S, T, A, B> {
    final class Mu<A, B> implements K2 {}

    static <S, T, A, B> Prism<S, T, A, B> unbox(final App2<Mu<A, B>, S, T> box) {
        return (Prism<S, T, A, B>) box;
    }

    Either<T, A> match(final S s);

    T build(final B b);

    @Override
    default <P extends K2> FunctionType<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends Cocartesian.Mu, P> proof) {
        final Cocartesian<P, ? extends Cocartesian.Mu> cocartesian = Cocartesian.unbox(proof);
        return input -> cocartesian.dimap(
            cocartesian.right(input),
            this::match,
            (Either<T, B> a) -> {
                return a.map(Function.identity(), this::build);
            }
        );
    }

    final class Instance<A2, B2> implements Cocartesian<Mu<A2, B2>, Cocartesian.Mu> {
        @Override
        public <A, B, C, D> FunctionType<App2<Prism.Mu<A2, B2>, A, B>, App2<Prism.Mu<A2, B2>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return prismBox -> Optics.prism(
                (C c) -> Prism.unbox(prismBox).match(g.apply(c)).mapLeft(h),
                (B2 b) -> h.apply(Prism.unbox(prismBox).build(b))
            );
        }

        @Override
        public <A, B, C> App2<Prism.Mu<A2, B2>, Either<A, C>, Either<B, C>> left(final App2<Prism.Mu<A2, B2>, A, B> input) {
            final Prism<A, B, A2, B2> prism = Prism.unbox(input);
            return Optics.prism(
                (Either<A, C> either) -> either.map(
                    a -> prism.match(a).mapLeft(Either::left),
                    c -> Either.left(Either.right(c))
                ),
                (B2 b) -> Either.left(prism.build(b))
            );
        }

        @Override
        public <A, B, C> App2<Prism.Mu<A2, B2>, Either<C, A>, Either<C, B>> right(final App2<Prism.Mu<A2, B2>, A, B> input) {
            final Prism<A, B, A2, B2> prism = Prism.unbox(input);
            return Optics.prism(
                (Either<C, A> either) -> either.map(
                    c -> Either.left(Either.left(c)),
                    a -> prism.match(a).mapLeft(Either::right)
                ),
                (B2 b) -> Either.right(prism.build(b))
            );
        }
    }
}
