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

import java.util.function.BiFunction;
import java.util.function.Function;

public interface ReForgetC<R, A, B> extends App2<ReForgetC.Mu<R>, A, B> {
    final class Mu<R> implements K2 {}

    static <R, A, B> ReForgetC<R, A, B> unbox(final App2<Mu<R>, A, B> box) {
        return (ReForgetC<R, A, B>) box;
    }

    Either<Function<R, B>, BiFunction<A, R, B>> impl();

    default B run(final A a, final R r) {
        return impl().map(f -> f.apply(r), f -> f.apply(a, r));
    }

    final class Instance<R> implements AffineP<Mu<R>, Instance.Mu<R>>, App<Instance.Mu<R>, Mu<R>> {
        public static final class Mu<R> implements AffineP.Mu {}

        @Override
        public <A, B, C, D> FunctionType<App2<ReForgetC.Mu<R>, A, B>, App2<ReForgetC.Mu<R>, C, D>> dimap(final Function<C, A> g, final Function<B, D> h) {
            return input -> Optics.reForgetC("dimap", ReForgetC.unbox(input).impl().map(
                (Function<R, B> f) -> Either.left((R r) -> h.apply(f.apply(r))),
                (BiFunction<A, R, B> f) -> Either.right((C c, R r) -> h.apply(f.apply(g.apply(c), r)))
            ));
        }

        @Override
        public <A, B, C> App2<ReForgetC.Mu<R>, Pair<A, C>, Pair<B, C>> first(final App2<ReForgetC.Mu<R>, A, B> input) {
            return Optics.reForgetC("first", ReForgetC.unbox(input).impl().map(
                // stop ignoring the A here - switching from left to right
                (Function<R, B> f) -> Either.right((Pair<A, C> p, R r) -> Pair.of(f.apply(r), p.getSecond())),
                (BiFunction<A, R, B> f) -> Either.right((Pair<A, C> p, R r) -> Pair.of(f.apply(p.getFirst(), r), p.getSecond()))
            ));
        }

        @Override
        public <A, B, C> App2<ReForgetC.Mu<R>, Pair<C, A>, Pair<C, B>> second(final App2<ReForgetC.Mu<R>, A, B> input) {
            return Optics.reForgetC("second", ReForgetC.unbox(input).impl().map(
                // stop ignoring the A here - switching from left to right
                (Function<R, B> f) -> Either.right((Pair<C, A> p, R r) -> Pair.of(p.getFirst(), f.apply(r))),
                (BiFunction<A, R, B> f) -> Either.right((Pair<C, A> p, R r) -> Pair.of(p.getFirst(), f.apply(p.getSecond(), r)))
            ));
        }

        @Override
        public <A, B, C> App2<ReForgetC.Mu<R>, Either<A, C>, Either<B, C>> left(final App2<ReForgetC.Mu<R>, A, B> input) {
            return Optics.reForgetC("left", ReForgetC.unbox(input).impl().map(
                // keep ignoring A, and continue the prism path
                (Function<R, B> f) -> Either.left((R r) -> Either.left(f.apply(r))),
                // FIXME: R will be ingored in the C case, augment with the error instead?
                (BiFunction<A, R, B> f) -> Either.right((Either<A, C> p, R r) -> p.mapLeft(a -> f.apply(a, r)))
            ));
        }

        @Override
        public <A, B, C> App2<ReForgetC.Mu<R>, Either<C, A>, Either<C, B>> right(final App2<ReForgetC.Mu<R>, A, B> input) {
            return Optics.reForgetC("right", ReForgetC.unbox(input).impl().map(
                // keep ignoring A, and continue the prism path
                (Function<R, B> f) -> Either.left((R r) -> Either.right(f.apply(r))),
                (BiFunction<A, R, B> f) -> Either.right((Either<C, A> p, R r) -> p.mapRight(a -> f.apply(a, r)))
            ));
        }
    }
}
