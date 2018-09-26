// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.TraversalP;

import java.util.function.Function;

public interface Traversal<S, T, A, B> extends Wander<S, T, A, B>, App2<Traversal.Mu<A, B>, S, T>, Optic<TraversalP.Mu, S, T, A, B> {
    final class Mu<A, B> implements K2 {}

    static <S, T, A, B> Traversal<S, T, A, B> unbox(final App2<Mu<A, B>, S, T> box) {
        return (Traversal<S, T, A, B>) box;
    }

    @Override
    default <P extends K2> FunctionType<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends TraversalP.Mu, P> proof) {
        final TraversalP<P, ? extends TraversalP.Mu> proof1 = TraversalP.unbox(proof);
        return input -> proof1.wander(this, input);
    }

    final class Instance<A2, B2> implements TraversalP<Mu<A2, B2>, TraversalP.Mu> {
        @Override
        public <A, B, C, D> FunctionType<App2<Traversal.Mu<A2, B2>, A, B>, App2<Traversal.Mu<A2, B2>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return tr -> new Traversal<C, D, A2, B2>() {
                @Override
                public <F extends K1> FunctionType<C, App<F, D>> wander(final Applicative<F, ?> applicative, final FunctionType<A2, App<F, B2>> input) {
                    return c -> applicative.map(h, Traversal.unbox(tr).wander(applicative, input).apply(g.apply(c)));
                }
            };
        }

        @Override
        public <S, T, A, B> App2<Traversal.Mu<A2, B2>, S, T> wander(final Wander<S, T, A, B> wander, final App2<Traversal.Mu<A2, B2>, A, B> input) {
            return new Traversal<S, T, A2, B2>() {
                @Override
                public <F extends K1> FunctionType<S, App<F, T>> wander(final Applicative<F, ?> applicative, final FunctionType<A2, App<F, B2>> function) {
                    return wander.wander(applicative, unbox(input).wander(applicative, function));
                }
            };
        }
    }
}
