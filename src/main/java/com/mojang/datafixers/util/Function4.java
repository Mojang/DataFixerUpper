package com.mojang.datafixers.util;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Function4<T1, T2, T3, T4, R> {
    R apply(T1 t1, T2 t2, T3 t3, T4 t4);

    default Function<T1, Function3<T2, T3, T4, R>> curry() {
        return t1 -> (t2, t3, t4) -> apply(t1, t2, t3, t4);
    }

    default BiFunction<T1, T2, BiFunction<T3, T4, R>> curry2() {
        return (t1, t2) -> (t3, t4) -> apply(t1, t2, t3, t4);
    }

    default Function3<T1, T2, T3, Function<T4, R>> curry3() {
        return (t1, t2, t3) -> t4 -> apply(t1, t2, t3, t4);
    }
}
