package com.mojang.datafixers;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.K1;
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

public interface Products {
    record P1<F extends K1, T1>(
        App<F, T1> t1
    ) {
        public <T2> P2<F, T1, T2> and(final App<F, T2> t2) {
            return new P2<>(t1, t2);
        }

        public <T2, T3> P3<F, T1, T2, T3> and(final P2<F, T2, T3> p) {
            return new P3<>(t1, p.t1, p.t2);
        }

        public <T2, T3, T4> P4<F, T1, T2, T3, T4> and(final P3<F, T2, T3, T4> p) {
            return new P4<>(t1, p.t1, p.t2, p.t3);
        }

        public <T2, T3, T4, T5> P5<F, T1, T2, T3, T4, T5> and(final P4<F, T2, T3, T4, T5> p) {
            return new P5<>(t1, p.t1, p.t2, p.t3, p.t4);
        }

        public <T2, T3, T4, T5, T6> P6<F, T1, T2, T3, T4, T5, T6> and(final P5<F, T2, T3, T4, T5, T6> p) {
            return new P6<>(t1, p.t1, p.t2, p.t3, p.t4, p.t5);
        }

        public <T2, T3, T4, T5, T6, T7> P7<F, T1, T2, T3, T4, T5, T6, T7> and(final P6<F, T2, T3, T4, T5, T6, T7> p) {
            return new P7<>(t1, p.t1, p.t2, p.t3, p.t4, p.t5, p.t6);
        }

        public <T2, T3, T4, T5, T6, T7, T8> P8<F, T1, T2, T3, T4, T5, T6, T7, T8> and(final P7<F, T2, T3, T4, T5, T6, T7, T8> p) {
            return new P8<>(t1, p.t1, p.t2, p.t3, p.t4, p.t5, p.t6, p.t7);
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function<T1, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function<T1, R>> function) {
            return instance.ap(function, t1);
        }
    }

    static <T1, T2> P2<IdF.Mu, T1, T2> of(final T1 t1, final T2 t2) {
        return new P2<>(IdF.create(t1), IdF.create(t2));
    }

    record P2<F extends K1, T1, T2>(
        App<F, T1> t1,
        App<F, T2> t2
    ) {
        public <T3> P3<F, T1, T2, T3> and(final App<F, T3> t3) {
            return new P3<>(t1, t2, t3);
        }

        public <T3, T4> P4<F, T1, T2, T3, T4> and(final P2<F, T3, T4> p) {
            return new P4<>(t1, t2, p.t1, p.t2);
        }

        public <T3, T4, T5> P5<F, T1, T2, T3, T4, T5> and(final P3<F, T3, T4, T5> p) {
            return new P5<>(t1, t2, p.t1, p.t2, p.t3);
        }

        public <T3, T4, T5, T6> P6<F, T1, T2, T3, T4, T5, T6> and(final P4<F, T3, T4, T5, T6> p) {
            return new P6<>(t1, t2, p.t1, p.t2, p.t3, p.t4);
        }

        public <T3, T4, T5, T6, T7> P7<F, T1, T2, T3, T4, T5, T6, T7> and(final P5<F, T3, T4, T5, T6, T7> p) {
            return new P7<>(t1, t2, p.t1, p.t2, p.t3, p.t4, p.t5);
        }

        public <T3, T4, T5, T6, T7, T8> P8<F, T1, T2, T3, T4, T5, T6, T7, T8> and(final P6<F, T3, T4, T5, T6, T7, T8> p) {
            return new P8<>(t1, t2, p.t1, p.t2, p.t3, p.t4, p.t5, p.t6);
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final BiFunction<T1, T2, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, BiFunction<T1, T2, R>> function) {
            return instance.ap2(function, t1, t2);
        }
    }

    record P3<F extends K1, T1, T2, T3>(
        App<F, T1> t1,
        App<F, T2> t2,
        App<F, T3> t3
    ) {
        public <T4> P4<F, T1, T2, T3, T4> and(final App<F, T4> t4) {
            return new P4<>(t1, t2, t3, t4);
        }

        public <T4, T5> P5<F, T1, T2, T3, T4, T5> and(final P2<F, T4, T5> p) {
            return new P5<>(t1, t2, t3, p.t1, p.t2);
        }

        public <T4, T5, T6> P6<F, T1, T2, T3, T4, T5, T6> and(final P3<F, T4, T5, T6> p) {
            return new P6<>(t1, t2, t3, p.t1, p.t2, p.t3);
        }

        public <T4, T5, T6, T7> P7<F, T1, T2, T3, T4, T5, T6, T7> and(final P4<F, T4, T5, T6, T7> p) {
            return new P7<>(t1, t2, t3, p.t1, p.t2, p.t3, p.t4);
        }

        public <T4, T5, T6, T7, T8> P8<F, T1, T2, T3, T4, T5, T6, T7, T8> and(final P5<F, T4, T5, T6, T7, T8> p) {
            return new P8<>(t1, t2, t3, p.t1, p.t2, p.t3, p.t4, p.t5);
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function3<T1, T2, T3, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function3<T1, T2, T3, R>> function) {
            return instance.ap3(function, t1, t2, t3);
        }
    }

    record P4<F extends K1, T1, T2, T3, T4>(
        App<F, T1> t1,
        App<F, T2> t2,
        App<F, T3> t3,
        App<F, T4> t4
    ) {
        public <T5> P5<F, T1, T2, T3, T4, T5> and(final App<F, T5> t5) {
            return new P5<>(t1, t2, t3, t4, t5);
        }

        public <T5, T6> P6<F, T1, T2, T3, T4, T5, T6> and(final P2<F, T5, T6> p) {
            return new P6<>(t1, t2, t3, t4, p.t1, p.t2);
        }

        public <T5, T6, T7> P7<F, T1, T2, T3, T4, T5, T6, T7> and(final P3<F, T5, T6, T7> p) {
            return new P7<>(t1, t2, t3, t4, p.t1, p.t2, p.t3);
        }

        public <T5, T6, T7, T8> P8<F, T1, T2, T3, T4, T5, T6, T7, T8> and(final P4<F, T5, T6, T7, T8> p) {
            return new P8<>(t1, t2, t3, t4, p.t1, p.t2, p.t3, p.t4);
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function4<T1, T2, T3, T4, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function4<T1, T2, T3, T4, R>> function) {
            return instance.ap4(function, t1, t2, t3, t4);
        }
    }

    record P5<F extends K1, T1, T2, T3, T4, T5>(
        App<F, T1> t1,
        App<F, T2> t2,
        App<F, T3> t3,
        App<F, T4> t4,
        App<F, T5> t5
    ) {
        public <T6> P6<F, T1, T2, T3, T4, T5, T6> and(final App<F, T6> t6) {
            return new P6<>(t1, t2, t3, t4, t5, t6);
        }

        public <T6, T7> P7<F, T1, T2, T3, T4, T5, T6, T7> and(final P2<F, T6, T7> p) {
            return new P7<>(t1, t2, t3, t4, t5, p.t1, p.t2);
        }

        public <T6, T7, T8> P8<F, T1, T2, T3, T4, T5, T6, T7, T8> and(final P3<F, T6, T7, T8> p) {
            return new P8<>(t1, t2, t3, t4, t5, p.t1, p.t2, p.t3);
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function5<T1, T2, T3, T4, T5, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function5<T1, T2, T3, T4, T5, R>> function) {
            return instance.ap5(function, t1, t2, t3, t4, t5);
        }
    }

    record P6<F extends K1, T1, T2, T3, T4, T5, T6>(
        App<F, T1> t1,
        App<F, T2> t2,
        App<F, T3> t3,
        App<F, T4> t4,
        App<F, T5> t5,
        App<F, T6> t6
    ) {
        public <T7> P7<F, T1, T2, T3, T4, T5, T6, T7> and(final App<F, T7> t7) {
            return new P7<>(t1, t2, t3, t4, t5, t6, t7);
        }

        public <T7, T8> P8<F, T1, T2, T3, T4, T5, T6, T7, T8> and(final P2<F, T7, T8> p) {
            return new P8<>(t1, t2, t3, t4, t5, t6, p.t1, p.t2);
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function6<T1, T2, T3, T4, T5, T6, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function6<T1, T2, T3, T4, T5, T6, R>> function) {
            return instance.ap6(function, t1, t2, t3, t4, t5, t6);
        }
    }

    record P7<F extends K1, T1, T2, T3, T4, T5, T6, T7>(
        App<F, T1> t1,
        App<F, T2> t2,
        App<F, T3> t3,
        App<F, T4> t4,
        App<F, T5> t5,
        App<F, T6> t6,
        App<F, T7> t7
    ) {
        public <T8> P8<F, T1, T2, T3, T4, T5, T6, T7, T8> and(final App<F, T8> t8) {
            return new P8<>(t1, t2, t3, t4, t5, t6, t7, t8);
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function7<T1, T2, T3, T4, T5, T6, T7, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function7<T1, T2, T3, T4, T5, T6, T7, R>> function) {
            return instance.ap7(function, t1, t2, t3, t4, t5, t6, t7);
        }
    }

    record P8<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8>(
        App<F, T1> t1,
        App<F, T2> t2,
        App<F, T3> t3,
        App<F, T4> t4,
        App<F, T5> t5,
        App<F, T6> t6,
        App<F, T7> t7,
        App<F, T8> t8
    ) {
        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function8<T1, T2, T3, T4, T5, T6, T7, T8, R>> function) {
            return instance.ap8(function, t1, t2, t3, t4, t5, t6, t7, t8);
        }
    }

    record P9<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9>(
        App<F, T1> t1,
        App<F, T2> t2,
        App<F, T3> t3,
        App<F, T4> t4,
        App<F, T5> t5,
        App<F, T6> t6,
        App<F, T7> t7,
        App<F, T8> t8,
        App<F, T9> t9
    ) {
        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R>> function) {
            return instance.ap9(function, t1, t2, t3, t4, t5, t6, t7, t8, t9);
        }
    }

    record P10<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>(
        App<F, T1> t1,
        App<F, T2> t2,
        App<F, T3> t3,
        App<F, T4> t4,
        App<F, T5> t5,
        App<F, T6> t6,
        App<F, T7> t7,
        App<F, T8> t8,
        App<F, T9> t9,
        App<F, T10> t10
    ) {
        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R>> function) {
            return instance.ap10(function, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
        }
    }

    record P11<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>(
        App<F, T1> t1,
        App<F, T2> t2,
        App<F, T3> t3,
        App<F, T4> t4,
        App<F, T5> t5,
        App<F, T6> t6,
        App<F, T7> t7,
        App<F, T8> t8,
        App<F, T9> t9,
        App<F, T10> t10,
        App<F, T11> t11
    ) {
        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R>> function) {
            return instance.ap11(function, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
        }
    }

    record P12<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>(
        App<F, T1> t1,
        App<F, T2> t2,
        App<F, T3> t3,
        App<F, T4> t4,
        App<F, T5> t5,
        App<F, T6> t6,
        App<F, T7> t7,
        App<F, T8> t8,
        App<F, T9> t9,
        App<F, T10> t10,
        App<F, T11> t11,
        App<F, T12> t12
    ) {
        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R>> function) {
            return instance.ap12(function, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);
        }
    }

    record P13<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>(
        App<F, T1> t1,
        App<F, T2> t2,
        App<F, T3> t3,
        App<F, T4> t4,
        App<F, T5> t5,
        App<F, T6> t6,
        App<F, T7> t7,
        App<F, T8> t8,
        App<F, T9> t9,
        App<F, T10> t10,
        App<F, T11> t11,
        App<F, T12> t12,
        App<F, T13> t13
    ) {
        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R>> function) {
            return instance.ap13(function, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
        }
    }

    record P14<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>(
        App<F, T1> t1,
        App<F, T2> t2,
        App<F, T3> t3,
        App<F, T4> t4,
        App<F, T5> t5,
        App<F, T6> t6,
        App<F, T7> t7,
        App<F, T8> t8,
        App<F, T9> t9,
        App<F, T10> t10,
        App<F, T11> t11,
        App<F, T12> t12,
        App<F, T13> t13,
        App<F, T14> t14
    ) {
        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R>> function) {
            return instance.ap14(function, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14);
        }
    }

    record P15<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>(
        App<F, T1> t1,
        App<F, T2> t2,
        App<F, T3> t3,
        App<F, T4> t4,
        App<F, T5> t5,
        App<F, T6> t6,
        App<F, T7> t7,
        App<F, T8> t8,
        App<F, T9> t9,
        App<F, T10> t10,
        App<F, T11> t11,
        App<F, T12> t12,
        App<F, T13> t13,
        App<F, T14> t14,
        App<F, T15> t15
    ) {
        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R>> function) {
            return instance.ap15(function, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
        }
    }

    record P16<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>(
        App<F, T1> t1,
        App<F, T2> t2,
        App<F, T3> t3,
        App<F, T4> t4,
        App<F, T5> t5,
        App<F, T6> t6,
        App<F, T7> t7,
        App<F, T8> t8,
        App<F, T9> t9,
        App<F, T10> t10,
        App<F, T11> t11,
        App<F, T12> t12,
        App<F, T13> t13,
        App<F, T14> t14,
        App<F, T15> t15,
        App<F, T16> t16
    ) {
        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R>> function) {
            return instance.ap16(function, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16);
        }
    }
}
