// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class IdF<A> implements App<IdF.Mu, A> {
    public static final class Mu implements K1 {}

    protected final A value;

    IdF(final A value) {
        this.value = value;
    }

    public A value() {
        return value;
    }

    public static <A> A get(final App<Mu, A> box) {
        return ((IdF<A>) box).value;
    }

    public static <A> IdF<A> create(final A a) {
        return new IdF<>(a);
    }

    public enum Instance implements Functor<Mu, Instance.Mu>, Applicative<Mu, Instance.Mu> {
        INSTANCE;

        public static final class Mu implements Functor.Mu, Applicative.Mu {}

        @Override
        public <T, R> App<IdF.Mu, R> map(final Function<? super T, ? extends R> func, final App<IdF.Mu, T> ts) {
            final IdF<T> idF = (IdF<T>) ts;
            return new IdF<>(func.apply(idF.value));
        }

        @Override
        public <A> App<IdF.Mu, A> point(final A a) {
            return create(a);
        }

        @Override
        public <A, R> Function<App<IdF.Mu, A>, App<IdF.Mu, R>> lift1(final App<IdF.Mu, Function<A, R>> function) {
            return a -> create(get(function).apply(get(a)));
        }

        @Override
        public <A, B, R> BiFunction<App<IdF.Mu, A>, App<IdF.Mu, B>, App<IdF.Mu, R>> lift2(final App<IdF.Mu, BiFunction<A, B, R>> function) {
            return (a, b) -> create(get(function).apply(get(a), get(b)));
        }
    }
}
