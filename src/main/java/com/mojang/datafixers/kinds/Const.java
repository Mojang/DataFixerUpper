// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class Const<C, T> implements App<Const.Mu<C>, T> {
    public static final class Mu<C> implements K1 {}

    public static <C, T> C unbox(final App<Mu<C>, T> box) {
        return ((Const<C, T>) box).value;
    }

    public static <C, T> Const<C, T> create(final C value) {
        return new Const<>(value);
    }

    private final C value;

    Const(final C value) {
        this.value = value;
    }

    public static final class Instance<C> implements Applicative<Mu<C>, Instance.Mu<C>> {
        public static final class Mu<C> implements Applicative.Mu {}

        private final Monoid<C> monoid;

        public Instance(final Monoid<C> monoid) {
            this.monoid = monoid;
        }

        @Override
        public <T, R> App<Const.Mu<C>, R> map(final Function<? super T, ? extends R> func, final App<Const.Mu<C>, T> ts) {
            return create(Const.unbox(ts));
        }

        @Override
        public <A> App<Const.Mu<C>, A> point(final A a) {
            return create(monoid.point());
        }

        @Override
        public <A, R> Function<App<Const.Mu<C>, A>, App<Const.Mu<C>, R>> lift1(final App<Const.Mu<C>, Function<A, R>> function) {
            return a -> create(monoid.add(Const.unbox(function), Const.unbox(a)));
        }

        @Override
        public <A, B, R> BiFunction<App<Const.Mu<C>, A>, App<Const.Mu<C>, B>, App<Const.Mu<C>, R>> lift2(final App<Const.Mu<C>, BiFunction<A, B, R>> function) {
            return (a, b) -> create(monoid.add(Const.unbox(function), monoid.add(Const.unbox(a), Const.unbox(b))));
        }
    }
}
