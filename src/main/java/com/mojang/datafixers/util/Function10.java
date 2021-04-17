package com.mojang.datafixers.util;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> {
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10);

    default Function<T1, Function9<T2, T3, T4, T5, T6, T7, T8, T9, T10, R>> curry() {
        return t1 -> (t2, t3, t4, t5, t6, t7, t8, t9, t10) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }

    default BiFunction<T1, T2, Function8<T3, T4, T5, T6, T7, T8, T9, T10, R>> curry2() {
        return (t1, t2) -> (t3, t4, t5, t6, t7, t8, t9, t10) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }

    default Function3<T1, T2, T3, Function7<T4, T5, T6, T7, T8, T9, T10, R>> curry3() {
        return (t1, t2, t3) -> (t4, t5, t6, t7, t8, t9, t10) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }

    default Function4<T1, T2, T3, T4, Function6<T5, T6, T7, T8, T9, T10, R>> curry4() {
        return (t1, t2, t3, t4) -> (t5, t6, t7, t8, t9, t10) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }

    default Function5<T1, T2, T3, T4, T5, Function5<T6, T7, T8, T9, T10, R>> curry5() {
        return (t1, t2, t3, t4, t5) -> (t6, t7, t8, t9, t10) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }

    default Function6<T1, T2, T3, T4, T5, T6, Function4<T7, T8, T9, T10, R>> curry6() {
        return (t1, t2, t3, t4, t5, t6) -> (t7, t8, t9, t10) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }

    default Function7<T1, T2, T3, T4, T5, T6, T7, Function3<T8, T9, T10, R>> curry7() {
        return (t1, t2, t3, t4, t5, t6, t7) -> (t8, t9, t10) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }

    default Function8<T1, T2, T3, T4, T5, T6, T7, T8, BiFunction<T9, T10, R>> curry8() {
        return (t1, t2, t3, t4, t5, t6, t7, t8) -> (t9, t10) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }

    default Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, Function<T10, R>> curry9() {
        return (t1, t2, t3, t4, t5, t6, t7, t8, t9) -> t10 -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }
}
