package com.mojang.datafixers.util;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Function3<T1, T2, T3, R> {
    R apply(T1 t1, T2 t2, T3 t3);

    default Function<T1, BiFunction<T2, T3, R>> curry() {
        return t1 -> (t2, t3) -> apply(t1, t2, t3);
    }

    default BiFunction<T1, T2, Function<T3, R>> curry2() {
        return (t1, t2) -> t3 -> apply(t1, t2, t3);
    }
}
