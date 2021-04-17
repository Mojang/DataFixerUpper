// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.AffineP;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import java.util.function.Function;

interface ReForgetP<R, A, B> extends App2<ReForgetP.Mu<R>, A, B> {
    final class Mu<R> implements K2 {}

    static <R, A, B> ReForgetP<R, A, B> unbox(final App2<Mu<R>, A, B> box) {
        return (ReForgetP<R, A, B>) box;
    }

    B run(final A a, final R r);

    final class Instance<R> implements AffineP<Mu<R>, Instance.Mu<R>>, App<Instance.Mu<R>, Mu<R>> {
        static final class Mu<R> implements AffineP.Mu {}

        @Override
        public <A, B, C, D> FunctionType<App2<ReForgetP.Mu<R>, A, B>, App2<ReForgetP.Mu<R>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return input -> Optics.reForgetP("dimap", (c, r) -> {
                final A a = g.apply(c);
                final B b = ReForgetP.unbox(input).run(a, r);
                final D d = h.apply(b);
                return d;
            });
        }

        @Override
        public <A, B, C> App2<ReForgetP.Mu<R>, Either<A, C>, Either<B, C>> left(final App2<ReForgetP.Mu<R>, A, B> input) {
            return Optics.reForgetP("left", (e, r) -> e.mapLeft(a -> ReForgetP.unbox(input).run(a, r)));
        }

        @Override
        public <A, B, C> App2<ReForgetP.Mu<R>, Either<C, A>, Either<C, B>> right(final App2<ReForgetP.Mu<R>, A, B> input) {
            return Optics.reForgetP("right", (e, r) -> e.mapRight(a -> ReForgetP.unbox(input).run(a, r)));
        }

        @Override
        public <A, B, C> App2<ReForgetP.Mu<R>, Pair<A, C>, Pair<B, C>> first(final App2<ReForgetP.Mu<R>, A, B> input) {
            return Optics.reForgetP("first", (p, r) -> Pair.of(ReForgetP.unbox(input).run(p.getFirst(), r), p.getSecond()));
        }

        @Override
        public <A, B, C> App2<ReForgetP.Mu<R>, Pair<C, A>, Pair<C, B>> second(final App2<ReForgetP.Mu<R>, A, B> input) {
            return Optics.reForgetP("second", (p, r) -> Pair.of(p.getFirst(), ReForgetP.unbox(input).run(p.getSecond(), r)));
        }
    }
}
