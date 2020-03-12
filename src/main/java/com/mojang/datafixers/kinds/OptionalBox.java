// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class OptionalBox<T> implements App<OptionalBox.Mu, T> {
    public static final class Mu implements K1 {}

    public static <T> Optional<T> unbox(final App<Mu, T> box) {
        return ((OptionalBox<T>) box).value;
    }

    public static <T> OptionalBox<T> create(final Optional<T> value) {
        return new OptionalBox<>(value);
    }

    private final Optional<T> value;

    private OptionalBox(final Optional<T> value) {
        this.value = value;
    }

    public enum Instance implements Applicative<Mu, Instance.Mu>, Traversable<Mu, Instance.Mu> {
        INSTANCE;

        public static final class Mu implements Applicative.Mu, Traversable.Mu {}

        @Override
        public <T, R> App<OptionalBox.Mu, R> map(final Function<? super T, ? extends R> func, final App<OptionalBox.Mu, T> ts) {
            return create(OptionalBox.unbox(ts).map(func));
        }

        @Override
        public <A> App<OptionalBox.Mu, A> point(final A a) {
            return create(Optional.of(a));
        }

        @Override
        public <A, R> Function<App<OptionalBox.Mu, A>, App<OptionalBox.Mu, R>> lift1(final App<OptionalBox.Mu, Function<A, R>> function) {
            return a -> create(OptionalBox.unbox(function).flatMap(f -> OptionalBox.unbox(a).map(f)));
        }

        @Override
        public <A, B, R> BiFunction<App<OptionalBox.Mu, A>, App<OptionalBox.Mu, B>, App<OptionalBox.Mu, R>> lift2(final App<OptionalBox.Mu, BiFunction<A, B, R>> function) {
            return (a, b) -> create(OptionalBox.unbox(function).flatMap(f -> OptionalBox.unbox(a).flatMap(av -> OptionalBox.unbox(b).map(bv -> f.apply(av, bv)))));
        }

        @Override
        public <F extends K1, A, B> App<F, App<OptionalBox.Mu, B>> traverse(final Applicative<F, ?> applicative, final Function<A, App<F, B>> function, final App<OptionalBox.Mu, A> input) {
            final Optional<App<F, B>> traversed = unbox(input).map(function);
            if (traversed.isPresent()) {
                return applicative.map(b -> OptionalBox.create(Optional.of(b)), traversed.get());
            }
            return applicative.point(OptionalBox.create(Optional.empty()));
        }
    }
}
