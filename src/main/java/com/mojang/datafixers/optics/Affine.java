// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.AffineP;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.optics.profunctors.Cocartesian;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import java.util.function.Function;

public interface Affine<S, T, A, B> extends App2<Affine.Mu<A, B>, S, T>, Optic<AffineP.Mu, S, T, A, B> {
    final class Mu<A, B> implements K2 {}

    static <S, T, A, B> Affine<S, T, A, B> unbox(final App2<Mu<A, B>, S, T> box) {
        return (Affine<S, T, A, B>) box;
    }

    Either<T, A> preview(final S s);

    T set(final B b, final S s);

    @Override
    default <P extends K2> FunctionType<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends AffineP.Mu, P> proof) {
        final Cartesian<P, ? extends AffineP.Mu> cartesian = Cartesian.unbox(proof);
        final Cocartesian<P, ? extends AffineP.Mu> cocartesian = Cocartesian.unbox(proof);
        return input -> cartesian.dimap(
            cocartesian.left(cartesian.rmap(cartesian.<A, B, S>first(input), p -> set(p.getFirst(), p.getSecond()))),
            (S s) -> preview(s).map(Either::right, a -> Either.left(Pair.of(a, s))),
            (Either<T, T> e) -> {
                return e.map(Function.identity(), Function.identity());
            }
        );
    }

    final class Instance<A2, B2> implements AffineP<Mu<A2, B2>, AffineP.Mu> {
        @Override
        public <A, B, C, D> FunctionType<App2<Affine.Mu<A2, B2>, A, B>, App2<Affine.Mu<A2, B2>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return affineBox -> Optics.affine(
                (C c) -> Affine.unbox(affineBox).preview(g.apply(c)).mapLeft(h),
                (b2, c) -> h.apply(Affine.unbox(affineBox).set(b2, g.apply(c)))
            );
        }

        @Override
        public <A, B, C> App2<Affine.Mu<A2, B2>, Pair<A, C>, Pair<B, C>> first(final App2<Affine.Mu<A2, B2>, A, B> input) {
            final Affine<A, B, A2, B2> affine = Affine.unbox(input);
            return Optics.affine(
                pair -> affine.preview(pair.getFirst()).mapBoth(b -> Pair.of(b, pair.getSecond()), Function.identity()),
                (b2, pair) -> Pair.of(affine.set(b2, pair.getFirst()), pair.getSecond())
            );
        }

        @Override
        public <A, B, C> App2<Affine.Mu<A2, B2>, Pair<C, A>, Pair<C, B>> second(final App2<Affine.Mu<A2, B2>, A, B> input) {
            final Affine<A, B, A2, B2> affine = Affine.unbox(input);
            return Optics.affine(
                pair -> affine.preview(pair.getSecond()).mapBoth(b -> Pair.of(pair.getFirst(), b), Function.identity()),
                (b2, pair) -> Pair.of(pair.getFirst(), affine.set(b2, pair.getSecond()))
            );
        }

        @Override
        public <A, B, C> App2<Affine.Mu<A2, B2>, Either<A, C>, Either<B, C>> left(final App2<Affine.Mu<A2, B2>, A, B> input) {
            final Affine<A, B, A2, B2> affine = Affine.unbox(input);
            return Optics.affine(
                either -> either.map(
                    a -> affine.preview(a).mapLeft(Either::left),
                    c -> Either.left(Either.right(c))
                ),
                (b, either) -> either.map(l -> Either.left(affine.set(b, l)), Either::right)
            );
        }

        @Override
        public <A, B, C> App2<Affine.Mu<A2, B2>, Either<C, A>, Either<C, B>> right(final App2<Affine.Mu<A2, B2>, A, B> input) {
            final Affine<A, B, A2, B2> affine = Affine.unbox(input);
            return Optics.affine(
                either -> either.map(
                    c -> Either.left(Either.left(c)),
                    a -> affine.preview(a).mapLeft(Either::right)
                ),
                (b, either) -> either.map(Either::left, r -> Either.right(affine.set(b, r)))
            );
        }
    }
}
