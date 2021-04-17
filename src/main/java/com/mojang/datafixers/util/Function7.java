package com.mojang.datafixers.util;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Function7<T1, T2, T3, T4, T5, T6, T7, R> {
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7);

    default Function<T1, Function6<T2, T3, T4, T5, T6, T7, R>> curry() {
        return t1 -> (t2, t3, t4, t5, t6, t7) -> apply(t1, t2, t3, t4, t5, t6, t7);
    }

    default BiFunction<T1, T2, Function5<T3, T4, T5, T6, T7, R>> curry2() {
        return (t1, t2) -> (t3, t4, t5, t6, t7) -> apply(t1, t2, t3, t4, t5, t6, t7);
    }

    default Function3<T1, T2, T3, Function4<T4, T5, T6, T7, R>> curry3() {
        return (t1, t2, t3) -> (t4, t5, t6, t7) -> apply(t1, t2, t3, t4, t5, t6, t7);
    }

    default Function4<T1, T2, T3, T4, Function3<T5, T6, T7, R>> curry4() {
        return (t1, t2, t3, t4) -> (t5, t6, t7) -> apply(t1, t2, t3, t4, t5, t6, t7);
    }

    default Function5<T1, T2, T3, T4, T5, BiFunction<T6, T7, R>> curry5() {
        return (t1, t2, t3, t4, t5) -> (t6, t7) -> apply(t1, t2, t3, t4, t5, t6, t7);
    }

    default Function6<T1, T2, T3, T4, T5, T6, Function<T7, R>> curry6() {
        return (t1, t2, t3, t4, t5, t6) -> t7 -> apply(t1, t2, t3, t4, t5, t6, t7);
    }
}
