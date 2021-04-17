package com.mojang.datafixers.util;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Function5<T1, T2, T3, T4, T5, R> {
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);

    default Function<T1, Function4<T2, T3, T4, T5, R>> curry() {
        return t1 -> (t2, t3, t4, t5) -> apply(t1, t2, t3, t4, t5);
    }

    default BiFunction<T1, T2, Function3<T3, T4, T5, R>> curry2() {
        return (t1, t2) -> (t3, t4, t5) -> apply(t1, t2, t3, t4, t5);
    }

    default Function3<T1, T2, T3, BiFunction<T4, T5, R>> curry3() {
        return (t1, t2, t3) -> (t4, t5) -> apply(t1, t2, t3, t4, t5);
    }

    default Function4<T1, T2, T3, T4, Function<T5, R>> curry4() {
        return (t1, t2, t3, t4) -> (t5) -> apply(t1, t2, t3, t4, t5);
    }
}
