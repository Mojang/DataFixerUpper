// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.Functor;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;

import java.util.function.Function;

public class ProfunctorFunctorWrapper<P extends K2, F extends K1, G extends K1, A, B> implements App2<ProfunctorFunctorWrapper.Mu<P, F, G>, A, B> {
    public static final class Mu<P extends K2, F extends K1, G extends K1> implements K2 {}

    public static <P extends K2, F extends K1, G extends K1, A, B> ProfunctorFunctorWrapper<P, F, G, A, B> unbox(final App2<Mu<P, F, G>, A, B> box) {
        return (ProfunctorFunctorWrapper<P, F, G, A, B>) box;
    }

    private final App2<P, App<F, A>, App<G, B>> value;

    public ProfunctorFunctorWrapper(final App2<P, App<F, A>, App<G, B>> value) {
        this.value = value;
    }

    public App2<P, App<F, A>, App<G, B>> value() {
        return value;
    }

    public static final class Instance<P extends K2, F extends K1, G extends K1> implements Profunctor<Mu<P, F, G>, Instance.Mu>, App<Instance.Mu, Mu<P, F, G>> {
        public static final class Mu implements Profunctor.Mu {}

        private final Profunctor<P, ? extends Profunctor.Mu> profunctor;
        private final Functor<F, ?> fFunctor;
        private final Functor<G, ?> gFunctor;

        public Instance(final App<? extends Profunctor.Mu, P> proof, final Functor<F, ?> fFunctor, final Functor<G, ?> gFunctor) {
            profunctor = Profunctor.unbox(proof);
            this.fFunctor = fFunctor;
            this.gFunctor = gFunctor;
        }

        @Override
        public <A, B, C, D> FunctionType<App2<ProfunctorFunctorWrapper.Mu<P, F, G>, A, B>, App2<ProfunctorFunctorWrapper.Mu<P, F, G>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return input -> {
                final App2<P, App<F, A>, App<G, B>> value = ProfunctorFunctorWrapper.unbox(input).value();
                final App2<P, App<F, C>, App<G, D>> newValue = profunctor.dimap(value, c -> fFunctor.map(g, c), b -> gFunctor.map(h, b));
                return new ProfunctorFunctorWrapper<>(newValue);
            };
        }
    }
}
