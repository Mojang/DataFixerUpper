// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.Functor;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.kinds.Representable;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.Procompose;
import com.mojang.datafixers.optics.Wander;
import com.mojang.datafixers.optics.profunctors.Mapping;
import com.mojang.datafixers.optics.profunctors.MonoidProfunctor;
import com.mojang.datafixers.optics.profunctors.Monoidal;
import com.mojang.datafixers.optics.profunctors.TraversalP;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

public interface FunctionType<A, B> extends Function<A, B>, App2<FunctionType.Mu, A, B>, App<FunctionType.ReaderMu<A>, B> {
    final class Mu implements K2 {}

    final class ReaderMu<A> implements K1 {}

    static <A, B> FunctionType<A, B> create(final Function<? super A, ? extends B> function) {
        return function::apply;
    }

    static <A, B> Function<A, B> unbox(final App2<Mu, A, B> box) {
        return (FunctionType<A, B>) box;
    }

    static <A, B> Function<A, B> unbox(final App<ReaderMu<A>, B> box) {
        return (FunctionType<A, B>) box;
    }

    @Override
    @Nonnull
    B apply(@Nonnull A a);

    final class ReaderInstance<R> implements Representable<ReaderMu<R>, R, ReaderInstance.Mu<R>> {
        public static final class Mu<A> implements Representable.Mu {}

        @Override
        public <T, R2> App<ReaderMu<R>, R2> map(final Function<? super T, ? extends R2> func, final App<ReaderMu<R>, T> ts) {
            return FunctionType.create(func.compose(FunctionType.unbox(ts)));
        }

        @Override
        public <B> App<ReaderMu<R>, B> to(final App<ReaderMu<R>, B> input) {
            return input;
        }

        @Override
        public <B> App<ReaderMu<R>, B> from(final App<ReaderMu<R>, B> input) {
            return input;
        }
    }

    enum Instance implements TraversalP<Mu, Instance.Mu>, MonoidProfunctor<Mu, Instance.Mu>, Mapping<Mu, Instance.Mu>, Monoidal<Mu, Instance.Mu>, App<Instance.Mu, Mu> {
        INSTANCE;

        public static final class Mu implements TraversalP.Mu, MonoidProfunctor.Mu, Mapping.Mu, Monoidal.Mu {
            public static final TypeToken<Mu> TYPE_TOKEN = new TypeToken<Mu>() {};
        }

        @Override
        public <A, B, C, D> FunctionType<App2<FunctionType.Mu, A, B>, App2<FunctionType.Mu, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return f -> create(h.compose(Optics.getFunc(f)).compose(g));
        }

        @Override
        public <A, B, C> App2<FunctionType.Mu, Pair<A, C>, Pair<B, C>> first(final App2<FunctionType.Mu, A, B> input) {
            return create(p -> Pair.of(Optics.getFunc(input).apply(p.getFirst()), p.getSecond()));
        }

        @Override
        public <A, B, C> App2<FunctionType.Mu, Pair<C, A>, Pair<C, B>> second(final App2<FunctionType.Mu, A, B> input) {
            return create(p -> Pair.of(p.getFirst(), Optics.getFunc(input).apply(p.getSecond())));
        }

        @Override
        public <S, T, A, B> App2<FunctionType.Mu, S, T> wander(final Wander<S, T, A, B> wander, final App2<FunctionType.Mu, A, B> input) {
            return create(s -> IdF.get(wander.wander(
                IdF.Instance.INSTANCE,
                a -> IdF.create(Optics.getFunc(input).apply(a))
            ).apply(s)));
        }

        @Override
        public <A, B, C> App2<FunctionType.Mu, Either<A, C>, Either<B, C>> left(final App2<FunctionType.Mu, A, B> input) {
            return create(either -> either.mapLeft(Optics.getFunc(input)));
        }

        @Override
        public <A, B, C> App2<FunctionType.Mu, Either<C, A>, Either<C, B>> right(final App2<FunctionType.Mu, A, B> input) {
            return create(either -> either.mapRight(Optics.getFunc(input)));
        }

        @Override
        public <A, B, C, D> App2<FunctionType.Mu, Pair<A, C>, Pair<B, D>> par(final App2<FunctionType.Mu, A, B> first, final Supplier<App2<FunctionType.Mu, C, D>> second) {
            return create(pair -> Pair.of(Optics.getFunc(first).apply(pair.getFirst()), Optics.getFunc(second.get()).apply(pair.getSecond())));
        }

        @Override
        public App2<FunctionType.Mu, Void, Void> empty() {
            return create(Function.identity());
        }

        @Override
        public <A, B> App2<FunctionType.Mu, A, B> zero(final App2<FunctionType.Mu, A, B> func) {
            return func;
        }

        @Override
        public <A, B> App2<FunctionType.Mu, A, B> plus(final App2<Procompose.Mu<FunctionType.Mu, FunctionType.Mu>, A, B> input) {
            final Procompose<FunctionType.Mu, FunctionType.Mu, A, B, ?> cmp = Procompose.unbox(input);
            return cap(cmp);
        }

        private <A, B, C> App2<FunctionType.Mu, A, B> cap(final Procompose<FunctionType.Mu, FunctionType.Mu, A, B, C> cmp) {
            return create(Optics.getFunc(cmp.second()).compose(Optics.getFunc(cmp.first().get())));
        }

        @Override
        public <A, B, F extends K1> App2<FunctionType.Mu, App<F, A>, App<F, B>> mapping(final Functor<F, ?> functor, final App2<FunctionType.Mu, A, B> input) {
            return create(fa -> functor.map(Optics.getFunc(input), fa));
        }
    }
}
