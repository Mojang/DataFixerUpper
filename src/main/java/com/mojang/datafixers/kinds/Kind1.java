// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.kinds;

import com.mojang.datafixers.Products;

public interface Kind1<F extends K1, Mu extends Kind1.Mu> extends App<Mu, F> {
    static <F extends K1, Proof extends Kind1.Mu> Kind1<F, Proof> unbox(final App<Proof, F> proofBox) {
        return (Kind1<F, Proof>) proofBox;
    }

    interface Mu extends K1 {}

    default <T1> Products.P1<F, T1> group(final App<F, T1> t1) {
        return new Products.P1<>(t1);
    }

    default <T1, T2> Products.P2<F, T1, T2> group(final App<F, T1> t1, final App<F, T2> t2) {
        return new Products.P2<>(t1, t2);
    }

    default <T1, T2, T3> Products.P3<F, T1, T2, T3> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3) {
        return new Products.P3<>(t1, t2, t3);
    }

    default <T1, T2, T3, T4> Products.P4<F, T1, T2, T3, T4> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4) {
        return new Products.P4<>(t1, t2, t3, t4);
    }

    default <T1, T2, T3, T4, T5> Products.P5<F, T1, T2, T3, T4, T5> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5) {
        return new Products.P5<>(t1, t2, t3, t4, t5);
    }

    default <T1, T2, T3, T4, T5, T6> Products.P6<F, T1, T2, T3, T4, T5, T6> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6) {
        return new Products.P6<>(t1, t2, t3, t4, t5, t6);
    }

    default <T1, T2, T3, T4, T5, T6, T7> Products.P7<F, T1, T2, T3, T4, T5, T6, T7> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7) {
        return new Products.P7<>(t1, t2, t3, t4, t5, t6, t7);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8> Products.P8<F, T1, T2, T3, T4, T5, T6, T7, T8> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8) {
        return new Products.P8<>(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9> Products.P9<F, T1, T2, T3, T4, T5, T6, T7, T8, T9> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9) {
        return new Products.P9<>(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Products.P10<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10) {
        return new Products.P10<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Products.P11<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11) {
        return new Products.P11<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Products.P12<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12) {
        return new Products.P12<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Products.P13<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13) {
        return new Products.P13<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Products.P14<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13, final App<F, T14> t14) {
        return new Products.P14<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Products.P15<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13, final App<F, T14> t14, final App<F, T15> t15) {
        return new Products.P15<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Products.P16<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> group(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13, final App<F, T14> t14, final App<F, T15> t15, final App<F, T16> t16) {
        return new Products.P16<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16);
    }
}
