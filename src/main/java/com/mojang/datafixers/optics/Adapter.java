// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.Profunctor;

import java.util.function.Function;

public interface Adapter<S, T, A, B> extends App2<Adapter.Mu<A, B>, S, T>, Optic<Profunctor.Mu, S, T, A, B> {
    final class Mu<A, B> implements K2 {}

    static <S, T, A, B> Adapter<S, T, A, B> unbox(final App2<Mu<A, B>, S, T> box) {
        return (Adapter<S, T, A, B>) box;
    }

    A from(final S s);

    T to(final B b);

    @Override
    default <P extends K2> FunctionType<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends Profunctor.Mu, P> proofBox) {
        final Profunctor<P, ? extends Profunctor.Mu> proof = Profunctor.unbox(proofBox);
        return a -> proof.dimap(
            a,
            this::from,
            this::to
        );
    }

    final class Instance<A2, B2> implements Profunctor<Mu<A2, B2>, Profunctor.Mu> {
        @Override
        public <A, B, C, D> FunctionType<App2<Adapter.Mu<A2, B2>, A, B>, App2<Adapter.Mu<A2, B2>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return a -> Optics.adapter(
                c -> Adapter.unbox(a).from(g.apply(c)),
                b2 -> h.apply(Adapter.unbox(a).to(b2))
            );
        }
    }
}
