// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.util;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.CartesianLike;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.Traversable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Pair<F, S> implements App<Pair.Mu<S>, F> {
    public static final class Mu<S> implements K1 {}

    public static <F, S> Pair<F, S> unbox(final App<Mu<S>, F> box) {
        return (Pair<F, S>) box;
    }

    private final F first;
    private final S second;

    public Pair(final F first, final S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    public Pair<S, F> swap() {
        return of(second, first);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Pair<?, ?>)) {
            return false;
        }
        final Pair<?, ?> other = (Pair<?, ?>) obj;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(first, second);
    }

    public <F2> Pair<F2, S> mapFirst(final Function<? super F, ? extends F2> function) {
        return of(function.apply(first), second);
    }

    public <S2> Pair<F, S2> mapSecond(final Function<? super S, ? extends S2> function) {
        return of(first, function.apply(second));
    }

    public static <F, S> Pair<F, S> of(final F first, final S second) {
        return new Pair<>(first, second);
    }

    public static <F, S> Collector<Pair<F, S>, ?, Map<F, S>> toMap() {
        return Collectors.toMap(Pair::getFirst, Pair::getSecond);
    }

    public static final class Instance<S2> implements Traversable<Mu<S2>, Instance.Mu<S2>>, CartesianLike<Mu<S2>, S2, Instance.Mu<S2>> {
        public static final class Mu<S2> implements Traversable.Mu, CartesianLike.Mu {}

        @Override
        public <T, R> App<Pair.Mu<S2>, R> map(final Function<? super T, ? extends R> func, final App<Pair.Mu<S2>, T> ts) {
            return Pair.unbox(ts).mapFirst(func);
        }

        @Override
        public <F extends K1, A, B> App<F, App<Pair.Mu<S2>, B>> traverse(final Applicative<F, ?> applicative, final Function<A, App<F, B>> function, final App<Pair.Mu<S2>, A> input) {
            final Pair<A, S2> pair = Pair.unbox(input);
            return applicative.ap(b -> of(b, pair.second), function.apply(pair.first));
        }

        @Override
        public <A> App<Pair.Mu<S2>, A> to(final App<Pair.Mu<S2>, A> input) {
            return input;
        }

        @Override
        public <A> App<Pair.Mu<S2>, A> from(final App<Pair.Mu<S2>, A> input) {
            return input;
        }
    }
}
