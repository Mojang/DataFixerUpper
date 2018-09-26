// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.Profunctor;

import java.util.function.Function;
import java.util.function.Supplier;

public final class Procompose<F extends K2, G extends K2, A, B, C> implements App2<Procompose.Mu<F, G>, A, B> {
    public Procompose(final Supplier<App2<F, A, C>> first, final App2<G, C, B> second) {
        this.first = first;
        this.second = second;
    }

    public static final class Mu<F extends K2, G extends K2> implements K2 {}

    public static <F extends K2, G extends K2, A, B> Procompose<F, G, A, B, ?> unbox(final App2<Mu<F, G>, A, B> box) {
        return (Procompose<F, G, A, B, ?>) box;
    }

    private final Supplier<App2<F, A, C>> first;
    private final App2<G, C, B> second;

    static final class ProfunctorInstance<F extends K2, G extends K2> implements Profunctor<Mu<F, G>, Profunctor.Mu> {
        private final Profunctor<F, Mu> p1;
        private final Profunctor<G, Mu> p2;

        ProfunctorInstance(final Profunctor<F, Mu> p1, final Profunctor<G, Mu> p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        @Override
        public <A, B, C, D> FunctionType<App2<Procompose.Mu<F, G>, A, B>, App2<Procompose.Mu<F, G>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return cmp -> cap(Procompose.unbox(cmp), g, h);
        }

        private <A, B, C, D, E> App2<Procompose.Mu<F, G>, C, D> cap(final Procompose<F, G, A, B, E> cmp, final Function<C, A> g, final Function<B, D> h) {
            return new Procompose<>(() -> p1.dimap(g, Function.<E>identity()).apply(cmp.first.get()), p2.dimap(Function.<E>identity(), h).apply(cmp.second));
        }
    }

    public Supplier<App2<F, A, C>> first() {
        return first;
    }

    public App2<G, C, B> second() {
        return second;
    }
}
