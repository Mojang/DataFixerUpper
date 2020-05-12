// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.optics.profunctors.ReCocartesian;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import java.util.function.Function;

public interface Forget<R, A, B> extends App2<Forget.Mu<R>, A, B> {
    final class Mu<R> implements K2 {}

    static <R, A, B> Forget<R, A, B> unbox(final App2<Mu<R>, A, B> box) {
        return (Forget<R, A, B>) box;
    }

    R run(final A a);

    final class Instance<R> implements Cartesian<Mu<R>, Instance.Mu<R>>, ReCocartesian<Mu<R>, Instance.Mu<R>>, App<Instance.Mu<R>, Mu<R>> {
        public static final class Mu<R> implements Cartesian.Mu, ReCocartesian.Mu {}

        @Override
        public <A, B, C, D> FunctionType<App2<Forget.Mu<R>, A, B>, App2<Forget.Mu<R>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return input -> Optics.forget(c -> Forget.unbox(input).run(g.apply(c)));
        }

        @Override
        public <A, B, C> App2<Forget.Mu<R>, Pair<A, C>, Pair<B, C>> first(final App2<Forget.Mu<R>, A, B> input) {
            return Optics.forget(p -> Forget.unbox(input).run(p.getFirst()));
        }

        @Override
        public <A, B, C> App2<Forget.Mu<R>, Pair<C, A>, Pair<C, B>> second(final App2<Forget.Mu<R>, A, B> input) {
            return Optics.forget(p -> Forget.unbox(input).run(p.getSecond()));
        }

        @Override
        public <A, B, C> App2<Forget.Mu<R>, A, B> unleft(final App2<Forget.Mu<R>, Either<A, C>, Either<B, C>> input) {
            return Optics.forget(a -> Forget.unbox(input).run(Either.left(a)));
        }

        @Override
        public <A, B, C> App2<Forget.Mu<R>, A, B> unright(final App2<Forget.Mu<R>, Either<C, A>, Either<C, B>> input) {
            return Optics.forget(a -> Forget.unbox(input).run(Either.right(a)));
        }
    }
}
