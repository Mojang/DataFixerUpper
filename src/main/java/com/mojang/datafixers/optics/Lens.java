// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.util.Pair;

import java.util.function.Function;

public interface Lens<S, T, A, B> extends App2<Lens.Mu<A, B>, S, T>, Optic<Cartesian.Mu, S, T, A, B> {
    final class Mu<A, B> implements K2 {}

    final class Mu2<S, T> implements K2 {}

    static <S, T, A, B> Lens<S, T, A, B> unbox(final App2<Mu<A, B>, S, T> box) {
        return (Lens<S, T, A, B>) box;
    }

    static <S, T, A, B> Lens<S, T, A, B> unbox2(final App2<Mu2<S, T>, B, A> box) {
        return ((Box<S, T, A, B>) box).lens;
    }

    static <S, T, A, B> App2<Mu2<S, T>, B, A> box(final Lens<S, T, A, B> lens) {
        return new Box<>(lens);
    }

    final class Box<S, T, A, B> implements App2<Mu2<S, T>, B, A> {
        private final Lens<S, T, A, B> lens;

        public Box(final Lens<S, T, A, B> lens) {
            this.lens = lens;
        }
    }

    A view(final S s);

    T update(final B b, final S s);

    @Override
    default <P extends K2> FunctionType<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends Cartesian.Mu, P> proofBox) {
        final Cartesian<P, ? extends Cartesian.Mu> proof = Cartesian.unbox(proofBox);
        return a -> proof.dimap(
            proof.<A, B, S>first(a),
            s -> Pair.<A, S>of(view(s), s),
            pair -> update(pair.getFirst(), pair.getSecond())
        );
    }

    final class Instance<A2, B2> implements Cartesian<Mu<A2, B2>, Cartesian.Mu> {
        @Override
        public <A, B, C, D> FunctionType<App2<Lens.Mu<A2, B2>, A, B>, App2<Lens.Mu<A2, B2>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return l -> Optics.lens(
                c -> Lens.unbox(l).view(g.apply(c)),
                (b2, c) -> h.apply(Lens.unbox(l).update(b2, g.apply(c)))
            );
        }

        @Override
        public <A, B, C> App2<Lens.Mu<A2, B2>, Pair<A, C>, Pair<B, C>> first(final App2<Lens.Mu<A2, B2>, A, B> input) {
            return Optics.lens(
                pair -> Lens.unbox(input).view(pair.getFirst()),
                (b2, pair) -> Pair.of(Lens.unbox(input).update(b2, pair.getFirst()), pair.getSecond())
            );
        }

        @Override
        public <A, B, C> App2<Lens.Mu<A2, B2>, Pair<C, A>, Pair<C, B>> second(final App2<Lens.Mu<A2, B2>, A, B> input) {
            return Optics.lens(
                pair -> Lens.unbox(input).view(pair.getSecond()),
                (b2, pair) -> Pair.of(pair.getFirst(), Lens.unbox(input).update(b2, pair.getSecond()))
            );
        }
    }
}
