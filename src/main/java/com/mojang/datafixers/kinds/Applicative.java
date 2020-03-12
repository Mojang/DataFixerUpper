// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Applicative<F extends K1, Mu extends Applicative.Mu> extends Functor<F, Mu> {
    static <F extends K1, Mu extends Applicative.Mu> Applicative<F, Mu> unbox(final App<Mu, F> proofBox) {
        return (Applicative<F, Mu>) proofBox;
    }

    interface Mu extends Functor.Mu {}

    <A> App<F, A> point(final A a);

    <A, R> Function<App<F, A>, App<F, R>> lift1(final App<F, Function<A, R>> function);

    default <A, B, R> BiFunction<App<F, A>, App<F, B>, App<F, R>> lift2(final App<F, BiFunction<A, B, R>> function) {
        final Function<BiFunction<A, B, R>, Function<A, Function<B, R>>> curry = f -> a -> b -> f.apply(a, b);
        return (fa, fb) -> ap(lift1(map(curry, function)).apply(fa), fb);
    }

    default <T1, T2, T3, R> Function3<App<F, T1>, App<F, T2>, App<F, T3>, App<F, R>> lift3(final App<F, Function3<T1, T2, T3, R>> function) {
        return (ft1, ft2, ft3) -> lift2(lift1(map(Function3::curry, function)).apply(ft1)).apply(ft2, ft3);
    }

    default <T1, T2, T3, T4, R> Function4<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, R>> lift4(final App<F, Function4<T1, T2, T3, T4, R>> function) {
        return (ft1, ft2, ft3, ft4) -> lift2(lift2(map(Function4::curry2, function)).apply(ft1, ft2)).apply(ft3, ft4);
    }

    default <T1, T2, T3, T4, T5, R> Function5<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, T5>, App<F, R>> lift5(final App<F, Function5<T1, T2, T3, T4, T5, R>> function) {
        return (ft1, ft2, ft3, ft4, ft5) -> lift3(lift2(map(Function5::curry2, function)).apply(ft1, ft2)).apply(ft3, ft4, ft5);
    }

    default <T1, T2, T3, T4, T5, T6, R> Function6<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, T5>, App<F, T6>, App<F, R>> lift6(final App<F, Function6<T1, T2, T3, T4, T5, T6, R>> function) {
        return (ft1, ft2, ft3, ft4, ft5, ft6) -> lift3(lift3(map(Function6::curry3, function)).apply(ft1, ft2, ft3)).apply(ft4, ft5, ft6);
    }

    default <T1, T2, T3, T4, T5, T6, T7, R> Function7<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, T5>, App<F, T6>, App<F, T7>, App<F, R>> lift7(final App<F, Function7<T1, T2, T3, T4, T5, T6, T7, R>> function) {
        return (ft1, ft2, ft3, ft4, ft5, ft6, ft7) -> lift4(lift3(map(Function7::curry3, function)).apply(ft1, ft2, ft3)).apply(ft4, ft5, ft6, ft7);
    }

    default <A, R> App<F, R> ap(final App<F, Function<A, R>> func, final App<F, A> arg) {
        return lift1(func).apply(arg);
    }

    default <A, R> App<F, R> ap(final Function<A, R> func, final App<F, A> arg) {
        return ap(point(func), arg);
    }

    default <A, B, R> App<F, R> ap2(final App<F, BiFunction<A, B, R>> func, final App<F, A> a, final App<F, B> b) {
        return lift2(func).apply(a, b);
    }

    default <A, B, R> App<F, R> ap2(final BiFunction<A, B, R> func, final App<F, A> a, final App<F, B> b) {
        return ap2(point(func), a, b);
    }

    default <T1> P1<F, T1> group(final App<F, T1> t1) {
        return new P1<>(this, t1);
    }

    default <T1, T2> P2<F, T1, T2> group(final App<F, T1> t1, final App<F, T2> t2) {
        return new P2<>(this, t1, t2);
    }

    default <T1, T2, T3> P3<F, T1, T2, T3> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3) {
        return new P3<>(this, t1, t2, t3);
    }

    default <T1, T2, T3, T4> P4<F, T1, T2, T3, T4> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4) {
        return new P4<>(this, t1, t2, t3, t4);
    }

    default <T1, T2, T3, T4, T5> P5<F, T1, T2, T3, T4, T5> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5) {
        return new P5<>(this, t1, t2, t3, t4, t5);
    }

    default <T1, T2, T3, T4, T5, T6> P6<F, T1, T2, T3, T4, T5, T6> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6) {
        return new P6<>(this, t1, t2, t3, t4, t5, t6);
    }

    default <T1, T2, T3, T4, T5, T6, T7> P7<F, T1, T2, T3, T4, T5, T6, T7> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7) {
        return new P7<>(this, t1, t2, t3, t4, t5, t6, t7);
    }

    final class P1<F extends K1, T1> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;

        private P1(final Applicative<F, ?> instance, final App<F, T1> t1) {
            this.instance = instance;
            this.t1 = t1;
        }

        public <T2> P2<F, T1, T2> and(final App<F, T2> t2) {
            return new P2<>(instance, t1, t2);
        }

        public <R> App<F, R> apply(final Function<T1, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function<T1, R>> function) {
            return instance.ap(function, t1);
        }
    }

    final class P2<F extends K1, T1, T2> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;
        private final App<F, T2> t2;

        private P2(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2) {
            this.instance = instance;
            this.t1 = t1;
            this.t2 = t2;
        }

        public <T3> P3<F, T1, T2, T3> and(final App<F, T3> t3) {
            return new P3<>(instance, t1, t2, t3);
        }

        public <R> App<F, R> apply(final BiFunction<T1, T2, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, BiFunction<T1, T2, R>> function) {
            return instance.ap2(function, t1, t2);
        }
    }

    final class P3<F extends K1, T1, T2, T3> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;
        private final App<F, T2> t2;
        private final App<F, T3> t3;

        private P3(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3) {
            this.instance = instance;
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
        }

        public <T4> P4<F, T1, T2, T3, T4> and(final App<F, T4> t4) {
            return new P4<>(instance, t1, t2, t3, t4);
        }

        public <R> App<F, R> apply(final Function3<T1, T2, T3, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function3<T1, T2, T3, R>> function) {
            return instance.lift3(function).apply(t1, t2, t3);
        }
    }

    final class P4<F extends K1, T1, T2, T3, T4> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;
        private final App<F, T2> t2;
        private final App<F, T3> t3;
        private final App<F, T4> t4;

        private P4(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4) {
            this.instance = instance;
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
        }

        public <T5> P5<F, T1, T2, T3, T4, T5> and(final App<F, T5> t5) {
            return new P5<>(instance, t1, t2, t3, t4, t5);
        }

        public <R> App<F, R> apply(final Function4<T1, T2, T3, T4, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function4<T1, T2, T3, T4, R>> function) {
            return instance.lift4(function).apply(t1, t2, t3, t4);
        }
    }

    final class P5<F extends K1, T1, T2, T3, T4, T5> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;
        private final App<F, T2> t2;
        private final App<F, T3> t3;
        private final App<F, T4> t4;
        private final App<F, T5> t5;

        private P5(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5) {
            this.instance = instance;
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
        }

        public <T6> P6<F, T1, T2, T3, T4, T5, T6> and(final App<F, T6> t6) {
            return new P6<>(instance, t1, t2, t3, t4, t5, t6);
        }

        public <R> App<F, R> apply(final Function5<T1, T2, T3, T4, T5, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function5<T1, T2, T3, T4, T5, R>> function) {
            return instance.lift5(function).apply(t1, t2, t3, t4, t5);
        }
    }

    final class P6<F extends K1, T1, T2, T3, T4, T5, T6> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;
        private final App<F, T2> t2;
        private final App<F, T3> t3;
        private final App<F, T4> t4;
        private final App<F, T5> t5;
        private final App<F, T6> t6;

        private P6(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6) {
            this.instance = instance;
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
        }

        public <T7> P7<F, T1, T2, T3, T4, T5, T6, T7> and(final App<F, T7> t7) {
            return new P7<>(instance, t1, t2, t3, t4, t5, t6, t7);
        }

        public <R> App<F, R> apply(final Function6<T1, T2, T3, T4, T5, T6, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function6<T1, T2, T3, T4, T5, T6, R>> function) {
            return instance.lift6(function).apply(t1, t2, t3, t4, t5, t6);
        }
    }

    final class P7<F extends K1, T1, T2, T3, T4, T5, T6, T7> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;
        private final App<F, T2> t2;
        private final App<F, T3> t3;
        private final App<F, T4> t4;
        private final App<F, T5> t5;
        private final App<F, T6> t6;
        private final App<F, T7> t7;

        private P7(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7) {
            this.instance = instance;
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
            this.t7 = t7;
        }

        public <R> App<F, R> apply(final Function7<T1, T2, T3, T4, T5, T6, T7, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function7<T1, T2, T3, T4, T5, T6, T7, R>> function) {
            return instance.lift7(function).apply(t1, t2, t3, t4, t5, t6, t7);
        }
    }
}
