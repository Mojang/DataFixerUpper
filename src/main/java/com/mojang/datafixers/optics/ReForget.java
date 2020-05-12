// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.Cocartesian;
import com.mojang.datafixers.optics.profunctors.ReCartesian;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import java.util.function.Function;

interface ReForget<R, A, B> extends App2<ReForget.Mu<R>, A, B> {
    final class Mu<R> implements K2 {}

    static <R, A, B> ReForget<R, A, B> unbox(final App2<Mu<R>, A, B> box) {
        return (ReForget<R, A, B>) box;
    }

    B run(final R r);

    final class Instance<R> implements ReCartesian<Mu<R>, Instance.Mu<R>>, Cocartesian<Mu<R>, Instance.Mu<R>>, App<Instance.Mu<R>, Mu<R>> {
        static final class Mu<R> implements ReCartesian.Mu, Cocartesian.Mu {}

        @Override
        public <A, B, C, D> FunctionType<App2<ReForget.Mu<R>, A, B>, App2<ReForget.Mu<R>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return input -> Optics.reForget(r -> h.apply(ReForget.unbox(input).run(r)));
        }

        @Override
        public <A, B, C> App2<ReForget.Mu<R>, A, B> unfirst(final App2<ReForget.Mu<R>, Pair<A, C>, Pair<B, C>> input) {
            return Optics.reForget(r -> ReForget.unbox(input).run(r).getFirst());
        }

        @Override
        public <A, B, C> App2<ReForget.Mu<R>, A, B> unsecond(final App2<ReForget.Mu<R>, Pair<C, A>, Pair<C, B>> input) {
            return Optics.reForget(r -> ReForget.unbox(input).run(r).getSecond());
        }

        @Override
        public <A, B, C> App2<ReForget.Mu<R>, Either<A, C>, Either<B, C>> left(final App2<ReForget.Mu<R>, A, B> input) {
            return Optics.reForget(r -> Either.left(ReForget.unbox(input).run(r)));
        }

        @Override
        public <A, B, C> App2<ReForget.Mu<R>, Either<C, A>, Either<C, B>> right(final App2<ReForget.Mu<R>, A, B> input) {
            return Optics.reForget(r -> Either.right(ReForget.unbox(input).run(r)));
        }
    }
}
