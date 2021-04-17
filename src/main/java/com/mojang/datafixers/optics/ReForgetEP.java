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

interface ReForgetEP<R, A, B> extends App2<ReForgetEP.Mu<R>, A, B> {
    final class Mu<R> implements K2 {}

    static <R, A, B> ReForgetEP<R, A, B> unbox(final App2<Mu<R>, A, B> box) {
        return (ReForgetEP<R, A, B>) box;
    }

    B run(final Either<A, Pair<A, R>> e);

    final class Instance<R> implements AffineP<Mu<R>, Instance.Mu<R>>, App<Instance.Mu<R>, Mu<R>> {
        static final class Mu<R> implements AffineP.Mu {}

        @Override
        public <A, B, C, D> FunctionType<App2<ReForgetEP.Mu<R>, A, B>, App2<ReForgetEP.Mu<R>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return input -> Optics.reForgetEP("dimap", e -> {
                final Either<A, Pair<A, R>> either = e.mapBoth(g, p -> Pair.of(g.apply(p.getFirst()), p.getSecond()));
                final B b = ReForgetEP.unbox(input).run(either);
                final D d = h.apply(b);
                return d;
            });
        }

        @Override
        public <A, B, C> App2<ReForgetEP.Mu<R>, Either<A, C>, Either<B, C>> left(final App2<ReForgetEP.Mu<R>, A, B> input) {
            final ReForgetEP<R, A, B> reForgetEP = ReForgetEP.unbox(input);
            return Optics.reForgetEP("left",
                e -> e.map(
                    e2 -> e2.mapLeft(
                        a -> reForgetEP.run(Either.left(a))
                    ),
                    (Pair<Either<A, C>, R> p) -> p.getFirst().mapLeft(
                        a -> reForgetEP.run(Either.right(Pair.of(a, p.getSecond())))
                    )
                )
            );
        }

        @Override
        public <A, B, C> App2<ReForgetEP.Mu<R>, Either<C, A>, Either<C, B>> right(final App2<ReForgetEP.Mu<R>, A, B> input) {
            final ReForgetEP<R, A, B> reForgetEP = ReForgetEP.unbox(input);
            return Optics.reForgetEP("right",
                e -> e.map(
                    e2 -> e2.mapRight(
                        a -> reForgetEP.run(Either.left(a))
                    ),
                    (Pair<Either<C, A>, R> p) -> p.getFirst().mapRight(
                        a -> reForgetEP.run(Either.right(Pair.of(a, p.getSecond())))
                    )
                )
            );
        }

        @Override
        public <A, B, C> App2<ReForgetEP.Mu<R>, Pair<A, C>, Pair<B, C>> first(final App2<ReForgetEP.Mu<R>, A, B> input) {
            final ReForgetEP<R, A, B> reForgetEP = ReForgetEP.unbox(input);
            return Optics.reForgetEP("first",
                e -> e.map(
                    p -> Pair.of(
                        reForgetEP.run(Either.left(p.getFirst())),
                        p.getSecond()
                    ),
                    (Pair<Pair<A, C>, R> p) -> Pair.of(
                        reForgetEP.run(Either.right(Pair.of(p.getFirst().getFirst(), p.getSecond()))),
                        p.getFirst().getSecond()
                    )
                )
            );
        }

        @Override
        public <A, B, C> App2<ReForgetEP.Mu<R>, Pair<C, A>, Pair<C, B>> second(final App2<ReForgetEP.Mu<R>, A, B> input) {
            final ReForgetEP<R, A, B> reForgetEP = ReForgetEP.unbox(input);
            return Optics.reForgetEP("second",
                e -> e.map(
                    p -> Pair.of(
                        p.getFirst(),
                        reForgetEP.run(Either.left(p.getSecond()))
                    ),
                    (Pair<Pair<C, A>, R> p) -> Pair.of(
                        p.getFirst().getFirst(),
                        reForgetEP.run(Either.right(Pair.of(p.getFirst().getSecond(), p.getSecond())))
                    )
                )
            );
        }
    }
}
