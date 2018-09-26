// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Applicative<F extends K1, Mu extends Applicative.Mu> extends Functor<F, Mu> {
    static <F extends K1, Mu extends Applicative.Mu> Applicative<F, Mu> unbox(final App<Mu, F> proofBox) {
        return (Applicative<F, Mu>) proofBox;
    }

    interface Mu extends Functor.Mu {}

    <A> App<F, A> point(final A a);

    <A, R> Function<App<F, A>, App<F, R>> lift1(final App<F, Function<A, R>> function);

    <A, B, R> BiFunction<App<F, A>, App<F, B>, App<F, R>> lift2(final App<F, BiFunction<A, B, R>> function);

    default <A, R> App<F, R> ap(final App<F, Function<A, R>> func, final App<F, A> arg) {
        return lift1(func).apply(arg);
    }

    default <A, R> App<F, R> ap(final Function<A, R> func, final App<F, A> arg) {
        return lift1(point(func)).apply(arg);
    }

    default <A, B, R> App<F, R> ap2(final App<F, BiFunction<A, B, R>> func, final App<F, A> a, final App<F, B> b) {
        return lift2(func).apply(a, b);
    }

    default <A, B, R> App<F, R> ap2(final BiFunction<A, B, R> func, final App<F, A> a, final App<F, B> b) {
        return lift2(point(func)).apply(a, b);
    }
}
