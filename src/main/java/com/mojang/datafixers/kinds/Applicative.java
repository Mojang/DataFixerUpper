// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import com.mojang.datafixers.util.Function10;
import com.mojang.datafixers.util.Function11;
import com.mojang.datafixers.util.Function12;
import com.mojang.datafixers.util.Function13;
import com.mojang.datafixers.util.Function14;
import com.mojang.datafixers.util.Function15;
import com.mojang.datafixers.util.Function16;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;
import com.mojang.datafixers.util.Function9;

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
        return (fa, fb) -> ap2(function, fa, fb);
    }

    default <T1, T2, T3, R> Function3<App<F, T1>, App<F, T2>, App<F, T3>, App<F, R>> lift3(final App<F, Function3<T1, T2, T3, R>> function) {
        return (ft1, ft2, ft3) -> ap3(function, ft1, ft2, ft3);
    }

    default <T1, T2, T3, T4, R> Function4<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, R>> lift4(final App<F, Function4<T1, T2, T3, T4, R>> function) {
        return (ft1, ft2, ft3, ft4) -> ap4(function, ft1, ft2, ft3, ft4);
    }

    default <T1, T2, T3, T4, T5, R> Function5<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, T5>, App<F, R>> lift5(final App<F, Function5<T1, T2, T3, T4, T5, R>> function) {
        return (ft1, ft2, ft3, ft4, ft5) -> ap5(function, ft1, ft2, ft3, ft4, ft5);
    }

    default <T1, T2, T3, T4, T5, T6, R> Function6<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, T5>, App<F, T6>, App<F, R>> lift6(final App<F, Function6<T1, T2, T3, T4, T5, T6, R>> function) {
        return (ft1, ft2, ft3, ft4, ft5, ft6) -> ap6(function, ft1, ft2, ft3, ft4, ft5, ft6);
    }

    default <T1, T2, T3, T4, T5, T6, T7, R> Function7<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, T5>, App<F, T6>, App<F, T7>, App<F, R>> lift7(final App<F, Function7<T1, T2, T3, T4, T5, T6, T7, R>> function) {
        return (ft1, ft2, ft3, ft4, ft5, ft6, ft7) -> ap7(function, ft1, ft2, ft3, ft4, ft5, ft6, ft7);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, R> Function8<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, T5>, App<F, T6>, App<F, T7>, App<F, T8>, App<F, R>> lift8(final App<F, Function8<T1, T2, T3, T4, T5, T6, T7, T8, R>> function) {
        return (ft1, ft2, ft3, ft4, ft5, ft6, ft7, ft8) -> ap8(function, ft1, ft2, ft3, ft4, ft5, ft6, ft7, ft8);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> Function9<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, T5>, App<F, T6>, App<F, T7>, App<F, T8>, App<F, T9>, App<F, R>> lift9(final App<F, Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R>> function) {
        return (ft1, ft2, ft3, ft4, ft5, ft6, ft7, ft8, ft9) -> ap9(function, ft1, ft2, ft3, ft4, ft5, ft6, ft7, ft8, ft9);
    }

    default <A, R> App<F, R> ap(final App<F, Function<A, R>> func, final App<F, A> arg) {
        return lift1(func).apply(arg);
    }

    default <A, R> App<F, R> ap(final Function<A, R> func, final App<F, A> arg) {
        return map(func, arg);
    }

    default <A, B, R> App<F, R> ap2(final App<F, BiFunction<A, B, R>> func, final App<F, A> a, final App<F, B> b) {
        final Function<BiFunction<A, B, R>, Function<A, Function<B, R>>> curry = f -> a1 -> b1 -> f.apply(a1, b1);
        return ap(ap(map(curry, func), a), b);
    }

    default <T1, T2, T3, R> App<F, R> ap3(final App<F, Function3<T1, T2, T3, R>> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3) {
        return ap2(ap(map(Function3::curry, func), t1), t2, t3);
    }

    default <T1, T2, T3, T4, R> App<F, R> ap4(final App<F, Function4<T1, T2, T3, T4, R>> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4) {
        return ap2(ap2(map(Function4::curry2, func), t1, t2), t3, t4);
    }

    default <T1, T2, T3, T4, T5, R> App<F, R> ap5(final App<F, Function5<T1, T2, T3, T4, T5, R>> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5) {
        return ap3(ap2(map(Function5::curry2, func), t1, t2), t3, t4, t5);
    }

    default <T1, T2, T3, T4, T5, T6, R> App<F, R> ap6(final App<F, Function6<T1, T2, T3, T4, T5, T6, R>> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6) {
        return ap3(ap3(map(Function6::curry3, func), t1, t2, t3), t4, t5, t6);
    }

    default <T1, T2, T3, T4, T5, T6, T7, R> App<F, R> ap7(final App<F, Function7<T1, T2, T3, T4, T5, T6, T7, R>> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7) {
        return ap4(ap3(map(Function7::curry3, func), t1, t2, t3), t4, t5, t6, t7);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, R> App<F, R> ap8(final App<F, Function8<T1, T2, T3, T4, T5, T6, T7, T8, R>> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8) {
        return ap4(ap4(map(Function8::curry4, func), t1, t2, t3, t4), t5, t6, t7, t8);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> App<F, R> ap9(final App<F, Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R>> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9) {
        return ap5(ap4(map(Function9::curry4, func), t1, t2, t3, t4), t5, t6, t7, t8, t9);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> App<F, R> ap10(final App<F, Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R>> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10) {
        return ap5(ap5(map(Function10::curry5, func), t1, t2, t3, t4, t5), t6, t7, t8, t9, t10);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> App<F, R> ap11(final App<F, Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R>> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11) {
        return ap6(ap5(map(Function11::curry5, func), t1, t2, t3, t4, t5), t6, t7, t8, t9, t10, t11);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> App<F, R> ap12(final App<F, Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R>> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12) {
        return ap6(ap6(map(Function12::curry6, func), t1, t2, t3, t4, t5, t6), t7, t8, t9, t10, t11, t12);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> App<F, R> ap13(final App<F, Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R>> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13) {
        return ap7(ap6(map(Function13::curry6, func), t1, t2, t3, t4, t5, t6), t7, t8, t9, t10, t11, t12, t13);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> App<F, R> ap14(final App<F, Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R>> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13, final App<F, T14> t14) {
        return ap7(ap7(map(Function14::curry7, func), t1, t2, t3, t4, t5, t6, t7), t8, t9, t10, t11, t12, t13, t14);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> App<F, R> ap15(final App<F, Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R>> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13, final App<F, T14> t14, final App<F, T15> t15) {
        return ap8(ap7(map(Function15::curry7, func), t1, t2, t3, t4, t5, t6, t7), t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R> App<F, R> ap16(final App<F, Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R>> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13, final App<F, T14> t14, final App<F, T15> t15, final App<F, T16> t16) {
        return ap8(ap8(map(Function16::curry8, func), t1, t2, t3, t4, t5, t6, t7, t8), t9, t10, t11, t12, t13, t14, t15, t16);
    }

    default <A, B, R> App<F, R> apply2(final BiFunction<A, B, R> func, final App<F, A> a, final App<F, B> b) {
        return ap2(point(func), a, b);
    }

    default <T1, T2, T3, R> App<F, R> apply3(final Function3<T1, T2, T3, R> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3) {
        return ap3(point(func), t1, t2, t3);
    }

    default <T1, T2, T3, T4, R> App<F, R> apply4(final Function4<T1, T2, T3, T4, R> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4) {
        return ap4(point(func), t1, t2, t3, t4);
    }

    default <T1, T2, T3, T4, T5, R> App<F, R> apply5(final Function5<T1, T2, T3, T4, T5, R> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5) {
        return ap5(point(func), t1, t2, t3, t4, t5);
    }

    default <T1, T2, T3, T4, T5, T6, R> App<F, R> apply6(final Function6<T1, T2, T3, T4, T5, T6, R> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6) {
        return ap6(point(func), t1, t2, t3, t4, t5, t6);
    }

    default <T1, T2, T3, T4, T5, T6, T7, R> App<F, R> apply7(final Function7<T1, T2, T3, T4, T5, T6, T7, R> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7) {
        return ap7(point(func), t1, t2, t3, t4, t5, t6, t7);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, R> App<F, R> apply8(final Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8) {
        return ap8(point(func), t1, t2, t3, t4, t5, t6, t7, t8);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> App<F, R> apply9(final Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9) {
        return ap9(point(func), t1, t2, t3, t4, t5, t6, t7, t8, t9);
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

    default <T1, T2, T3, T4, T5, T6, T7, T8> P8<F, T1, T2, T3, T4, T5, T6, T7, T8> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8) {
        return new P8<>(this, t1, t2, t3, t4, t5, t6, t7, t8);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9> P9<F, T1, T2, T3, T4, T5, T6, T7, T8, T9> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9) {
        return new P9<>(this, t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> P10<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10) {
        return new P10<>(this, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> P11<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11) {
        return new P11<>(this, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> P12<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12) {
        return new P12<>(this, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> P13<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13) {
        return new P13<>(this, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> P14<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13, final App<F, T14> t14) {
        return new P14<>(this, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> P15<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13, final App<F, T14> t14, final App<F, T15> t15) {
        return new P15<>(this, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> P16<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13, final App<F, T14> t14, final App<F, T15> t15, final App<F, T16> t16) {
        return new P16<>(this, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16);
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

        public <T2, T3> P3<F, T1, T2, T3> and(final P2<F, T2, T3> p) {
            return new P3<>(instance, t1, p.t1, p.t2);
        }

        public <T2, T3, T4> P4<F, T1, T2, T3, T4> and(final P3<F, T2, T3, T4> p) {
            return new P4<>(instance, t1, p.t1, p.t2, p.t3);
        }

        public <T2, T3, T4, T5> P5<F, T1, T2, T3, T4, T5> and(final P4<F, T2, T3, T4, T5> p) {
            return new P5<>(instance, t1, p.t1, p.t2, p.t3, p.t4);
        }

        public <T2, T3, T4, T5, T6> P6<F, T1, T2, T3, T4, T5, T6> and(final P5<F, T2, T3, T4, T5, T6> p) {
            return new P6<>(instance, t1, p.t1, p.t2, p.t3, p.t4, p.t5);
        }

        public <T2, T3, T4, T5, T6, T7> P7<F, T1, T2, T3, T4, T5, T6, T7> and(final P6<F, T2, T3, T4, T5, T6, T7> p) {
            return new P7<>(instance, t1, p.t1, p.t2, p.t3, p.t4, p.t5, p.t6);
        }

        public <T2, T3, T4, T5, T6, T7, T8> P8<F, T1, T2, T3, T4, T5, T6, T7, T8> and(final P7<F, T2, T3, T4, T5, T6, T7, T8> p) {
            return new P8<>(instance, t1, p.t1, p.t2, p.t3, p.t4, p.t5, p.t6, p.t7);
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

        public <T3, T4> P4<F, T1, T2, T3, T4> and(final P2<F, T3, T4> p) {
            return new P4<>(instance, t1, t2, p.t1, p.t2);
        }

        public <T3, T4, T5> P5<F, T1, T2, T3, T4, T5> and(final P3<F, T3, T4, T5> p) {
            return new P5<>(instance, t1, t2, p.t1, p.t2, p.t3);
        }

        public <T3, T4, T5, T6> P6<F, T1, T2, T3, T4, T5, T6> and(final P4<F, T3, T4, T5, T6> p) {
            return new P6<>(instance, t1, t2, p.t1, p.t2, p.t3, p.t4);
        }

        public <T3, T4, T5, T6, T7> P7<F, T1, T2, T3, T4, T5, T6, T7> and(final P5<F, T3, T4, T5, T6, T7> p) {
            return new P7<>(instance, t1, t2, p.t1, p.t2, p.t3, p.t4, p.t5);
        }

        public <T3, T4, T5, T6, T7, T8> P8<F, T1, T2, T3, T4, T5, T6, T7, T8> and(final P6<F, T3, T4, T5, T6, T7, T8> p) {
            return new P8<>(instance, t1, t2, p.t1, p.t2, p.t3, p.t4, p.t5, p.t6);
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

        public <T4, T5> P5<F, T1, T2, T3, T4, T5> and(final P2<F, T4, T5> p) {
            return new P5<>(instance, t1, t2, t3, p.t1, p.t2);
        }

        public <T4, T5, T6> P6<F, T1, T2, T3, T4, T5, T6> and(final P3<F, T4, T5, T6> p) {
            return new P6<>(instance, t1, t2, t3, p.t1, p.t2, p.t3);
        }

        public <T4, T5, T6, T7> P7<F, T1, T2, T3, T4, T5, T6, T7> and(final P4<F, T4, T5, T6, T7> p) {
            return new P7<>(instance, t1, t2, t3, p.t1, p.t2, p.t3, p.t4);
        }

        public <T4, T5, T6, T7, T8> P8<F, T1, T2, T3, T4, T5, T6, T7, T8> and(final P5<F, T4, T5, T6, T7, T8> p) {
            return new P8<>(instance, t1, t2, t3, p.t1, p.t2, p.t3, p.t4, p.t5);
        }

        public <R> App<F, R> apply(final Function3<T1, T2, T3, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function3<T1, T2, T3, R>> function) {
            return instance.ap3(function, t1, t2, t3);
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

        public <T5, T6> P6<F, T1, T2, T3, T4, T5, T6> and(final P2<F, T5, T6> p) {
            return new P6<>(instance, t1, t2, t3, t4, p.t1, p.t2);
        }

        public <T5, T6, T7> P7<F, T1, T2, T3, T4, T5, T6, T7> and(final P3<F, T5, T6, T7> p) {
            return new P7<>(instance, t1, t2, t3, t4, p.t1, p.t2, p.t3);
        }

        public <T5, T6, T7, T8> P8<F, T1, T2, T3, T4, T5, T6, T7, T8> and(final P4<F, T5, T6, T7, T8> p) {
            return new P8<>(instance, t1, t2, t3, t4, p.t1, p.t2, p.t3, p.t4);
        }

        public <R> App<F, R> apply(final Function4<T1, T2, T3, T4, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function4<T1, T2, T3, T4, R>> function) {
            return instance.ap4(function, t1, t2, t3, t4);
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

        public <T6, T7> P7<F, T1, T2, T3, T4, T5, T6, T7> and(final P2<F, T6, T7> p) {
            return new P7<>(instance, t1, t2, t3, t4, t5, p.t1, p.t2);
        }

        public <T6, T7, T8> P8<F, T1, T2, T3, T4, T5, T6, T7, T8> and(final P3<F, T6, T7, T8> p) {
            return new P8<>(instance, t1, t2, t3, t4, t5, p.t1, p.t2, p.t3);
        }

        public <R> App<F, R> apply(final Function5<T1, T2, T3, T4, T5, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function5<T1, T2, T3, T4, T5, R>> function) {
            return instance.ap5(function, t1, t2, t3, t4, t5);
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

        public <T7, T8> P8<F, T1, T2, T3, T4, T5, T6, T7, T8> and(final P2<F, T7, T8> p) {
            return new P8<>(instance, t1, t2, t3, t4, t5, t6, p.t1, p.t2);
        }

        public <R> App<F, R> apply(final Function6<T1, T2, T3, T4, T5, T6, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function6<T1, T2, T3, T4, T5, T6, R>> function) {
            return instance.ap6(function, t1, t2, t3, t4, t5, t6);
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

        public <T8> P8<F, T1, T2, T3, T4, T5, T6, T7, T8> and(final App<F, T8> t8) {
            return new P8<>(instance, t1, t2, t3, t4, t5, t6, t7, t8);
        }

        public <R> App<F, R> apply(final Function7<T1, T2, T3, T4, T5, T6, T7, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function7<T1, T2, T3, T4, T5, T6, T7, R>> function) {
            return instance.ap7(function, t1, t2, t3, t4, t5, t6, t7);
        }
    }

    final class P8<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;
        private final App<F, T2> t2;
        private final App<F, T3> t3;
        private final App<F, T4> t4;
        private final App<F, T5> t5;
        private final App<F, T6> t6;
        private final App<F, T7> t7;
        private final App<F, T8> t8;

        private P8(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8) {
            this.instance = instance;
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
            this.t7 = t7;
            this.t8 = t8;
        }

        public <R> App<F, R> apply(final Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function8<T1, T2, T3, T4, T5, T6, T7, T8, R>> function) {
            return instance.ap8(function, t1, t2, t3, t4, t5, t6, t7, t8);
        }
    }

    final class P9<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;
        private final App<F, T2> t2;
        private final App<F, T3> t3;
        private final App<F, T4> t4;
        private final App<F, T5> t5;
        private final App<F, T6> t6;
        private final App<F, T7> t7;
        private final App<F, T8> t8;
        private final App<F, T9> t9;

        private P9(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9) {
            this.instance = instance;
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
            this.t7 = t7;
            this.t8 = t8;
            this.t9 = t9;
        }

        public <R> App<F, R> apply(final Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R>> function) {
            return instance.ap9(function, t1, t2, t3, t4, t5, t6, t7, t8, t9);
        }
    }

    final class P10<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;
        private final App<F, T2> t2;
        private final App<F, T3> t3;
        private final App<F, T4> t4;
        private final App<F, T5> t5;
        private final App<F, T6> t6;
        private final App<F, T7> t7;
        private final App<F, T8> t8;
        private final App<F, T9> t9;
        private final App<F, T10> t10;

        private P10(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10) {
            this.instance = instance;
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
            this.t7 = t7;
            this.t8 = t8;
            this.t9 = t9;
            this.t10 = t10;
        }

        public <R> App<F, R> apply(final Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R>> function) {
            return instance.ap10(function, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
        }
    }

    final class P11<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;
        private final App<F, T2> t2;
        private final App<F, T3> t3;
        private final App<F, T4> t4;
        private final App<F, T5> t5;
        private final App<F, T6> t6;
        private final App<F, T7> t7;
        private final App<F, T8> t8;
        private final App<F, T9> t9;
        private final App<F, T10> t10;
        private final App<F, T11> t11;

        private P11(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11) {
            this.instance = instance;
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
            this.t7 = t7;
            this.t8 = t8;
            this.t9 = t9;
            this.t10 = t10;
            this.t11 = t11;
        }

        public <R> App<F, R> apply(final Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R>> function) {
            return instance.ap11(function, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
        }
    }

    final class P12<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;
        private final App<F, T2> t2;
        private final App<F, T3> t3;
        private final App<F, T4> t4;
        private final App<F, T5> t5;
        private final App<F, T6> t6;
        private final App<F, T7> t7;
        private final App<F, T8> t8;
        private final App<F, T9> t9;
        private final App<F, T10> t10;
        private final App<F, T11> t11;
        private final App<F, T12> t12;

        private P12(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12) {
            this.instance = instance;
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
            this.t7 = t7;
            this.t8 = t8;
            this.t9 = t9;
            this.t10 = t10;
            this.t11 = t11;
            this.t12 = t12;
        }

        public <R> App<F, R> apply(final Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R>> function) {
            return instance.ap12(function, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);
        }
    }

    final class P13<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;
        private final App<F, T2> t2;
        private final App<F, T3> t3;
        private final App<F, T4> t4;
        private final App<F, T5> t5;
        private final App<F, T6> t6;
        private final App<F, T7> t7;
        private final App<F, T8> t8;
        private final App<F, T9> t9;
        private final App<F, T10> t10;
        private final App<F, T11> t11;
        private final App<F, T12> t12;
        private final App<F, T13> t13;

        private P13(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13) {
            this.instance = instance;
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
            this.t7 = t7;
            this.t8 = t8;
            this.t9 = t9;
            this.t10 = t10;
            this.t11 = t11;
            this.t12 = t12;
            this.t13 = t13;
        }

        public <R> App<F, R> apply(final Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R>> function) {
            return instance.ap13(function, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
        }
    }

    final class P14<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;
        private final App<F, T2> t2;
        private final App<F, T3> t3;
        private final App<F, T4> t4;
        private final App<F, T5> t5;
        private final App<F, T6> t6;
        private final App<F, T7> t7;
        private final App<F, T8> t8;
        private final App<F, T9> t9;
        private final App<F, T10> t10;
        private final App<F, T11> t11;
        private final App<F, T12> t12;
        private final App<F, T13> t13;
        private final App<F, T14> t14;

        private P14(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13, final App<F, T14> t14) {
            this.instance = instance;
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
            this.t7 = t7;
            this.t8 = t8;
            this.t9 = t9;
            this.t10 = t10;
            this.t11 = t11;
            this.t12 = t12;
            this.t13 = t13;
            this.t14 = t14;
        }

        public <R> App<F, R> apply(final Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R>> function) {
            return instance.ap14(function, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14);
        }
    }

    final class P15<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;
        private final App<F, T2> t2;
        private final App<F, T3> t3;
        private final App<F, T4> t4;
        private final App<F, T5> t5;
        private final App<F, T6> t6;
        private final App<F, T7> t7;
        private final App<F, T8> t8;
        private final App<F, T9> t9;
        private final App<F, T10> t10;
        private final App<F, T11> t11;
        private final App<F, T12> t12;
        private final App<F, T13> t13;
        private final App<F, T14> t14;
        private final App<F, T15> t15;

        private P15(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13, final App<F, T14> t14, final App<F, T15> t15) {
            this.instance = instance;
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
            this.t7 = t7;
            this.t8 = t8;
            this.t9 = t9;
            this.t10 = t10;
            this.t11 = t11;
            this.t12 = t12;
            this.t13 = t13;
            this.t14 = t14;
            this.t15 = t15;
        }

        public <R> App<F, R> apply(final Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R>> function) {
            return instance.ap15(function, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
        }
    }

    final class P16<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> {
        private final Applicative<F, ?> instance;
        private final App<F, T1> t1;
        private final App<F, T2> t2;
        private final App<F, T3> t3;
        private final App<F, T4> t4;
        private final App<F, T5> t5;
        private final App<F, T6> t6;
        private final App<F, T7> t7;
        private final App<F, T8> t8;
        private final App<F, T9> t9;
        private final App<F, T10> t10;
        private final App<F, T11> t11;
        private final App<F, T12> t12;
        private final App<F, T13> t13;
        private final App<F, T14> t14;
        private final App<F, T15> t15;
        private final App<F, T16> t16;

        private P16(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13, final App<F, T14> t14, final App<F, T15> t15, final App<F, T16> t16) {
            this.instance = instance;
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
            this.t7 = t7;
            this.t8 = t8;
            this.t9 = t9;
            this.t10 = t10;
            this.t11 = t11;
            this.t12 = t12;
            this.t13 = t13;
            this.t14 = t14;
            this.t15 = t15;
            this.t16 = t16;
        }

        public <R> App<F, R> apply(final Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R> function) {
            return apply(instance.point(function));
        }

        public <R> App<F, R> apply(final App<F, Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R>> function) {
            return instance.ap16(function, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16);
        }
    }
}
