// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ListBox<T> implements App<ListBox.Mu, T> {
    public static final class Mu implements K1 {}

    public static <T> List<T> unbox(final App<Mu, T> box) {
        return ((ListBox<T>) box).value;
    }

    public static <T> ListBox<T> create(final List<T> value) {
        return new ListBox<>(value);
    }

    private final List<T> value;

    private ListBox(final List<T> value) {
        this.value = value;
    }

    public static <F extends K1, A, B> App<F, List<B>> traverse(final Applicative<F, ?> applicative, final Function<A, App<F, B>> function, final List<A> input) {
        return applicative.map(ListBox::unbox, Instance.INSTANCE.traverse(applicative, function, create(input)));
    }

    public static <F extends K1, A> App<F, List<A>> flip(final Applicative<F, ?> applicative, final List<App<F, A>> input) {
        return applicative.map(ListBox::unbox, Instance.INSTANCE.flip(applicative, create(input)));
    }

    public enum Instance implements Traversable<Mu, Instance.Mu> {
        INSTANCE;

        public static final class Mu implements Traversable.Mu {}

        @Override
        public <T, R> App<ListBox.Mu, R> map(final Function<? super T, ? extends R> func, final App<ListBox.Mu, T> ts) {
            return create(ListBox.unbox(ts).stream().map(func).collect(Collectors.toList()));
        }

        @Override
        public <F extends K1, A, B> App<F, App<ListBox.Mu, B>> traverse(final Applicative<F, ?> applicative, final Function<A, App<F, B>> function, final App<ListBox.Mu, A> input) {
            final List<? extends A> list = unbox(input);

            App<F, ImmutableList.Builder<B>> result = applicative.point(ImmutableList.builder());

            for (final A a : list) {
                final App<F, B> fb = function.apply(a);
                result = applicative.ap2(applicative.point(ImmutableList.Builder::add), result, fb);
            }

            return applicative.map(b -> create(b.build()), result);
        }
    }
}
