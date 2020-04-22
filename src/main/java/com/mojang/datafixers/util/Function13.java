package com.mojang.datafixers.util;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> {
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12, T13 t13);

    default Function<T1, Function12<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R>> curry() {
        return t1 -> (t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
    }

    default BiFunction<T1, T2, Function11<T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R>> curry2() {
        return (t1, t2) -> (t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
    }

    default Function3<T1, T2, T3, Function10<T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R>> curry3() {
        return (t1, t2, t3) -> (t4, t5, t6, t7, t8, t9, t10, t11, t12, t13) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
    }

    default Function4<T1, T2, T3, T4, Function9<T5, T6, T7, T8, T9, T10, T11, T12, T13, R>> curry4() {
        return (t1, t2, t3, t4) -> (t5, t6, t7, t8, t9, t10, t11, t12, t13) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
    }

    default Function5<T1, T2, T3, T4, T5, Function8<T6, T7, T8, T9, T10, T11, T12, T13, R>> curry5() {
        return (t1, t2, t3, t4, t5) -> (t6, t7, t8, t9, t10, t11, t12, t13) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
    }

    default Function6<T1, T2, T3, T4, T5, T6, Function7<T7, T8, T9, T10, T11, T12, T13, R>> curry6() {
        return (t1, t2, t3, t4, t5, t6) -> (t7, t8, t9, t10, t11, t12, t13) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
    }

    default Function7<T1, T2, T3, T4, T5, T6, T7, Function6<T8, T9, T10, T11, T12, T13, R>> curry7() {
        return (t1, t2, t3, t4, t5, t6, t7) -> (t8, t9, t10, t11, t12, t13) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
    }

    default Function8<T1, T2, T3, T4, T5, T6, T7, T8, Function5<T9, T10, T11, T12, T13, R>> curry8() {
        return (t1, t2, t3, t4, t5, t6, t7, t8) -> (t9, t10, t11, t12, t13) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
    }

    default Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, Function4<T10, T11, T12, T13, R>> curry9() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9) -> (t10, t11, t12, t13) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
    }

    default Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, Function3<T11, T12, T13, R>> curry10() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10) -> (t11, t12, t13) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
    }

    default Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, BiFunction<T12, T13, R>> curry11() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11) -> (t12, t13) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
    }

    default Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, Function<T13, R>> curry12() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12) -> t13 -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
    }
}
