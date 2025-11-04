package com.mojang.datafixers.util;

import java.util.List;

/**
 * The Tuple interface defines a finite-length tuple that allows for storing multiple values of different types.<br/>
 * It provides a series of static factory methods to create tuple instances of different lengths (from 2 to 16 elements).<br/>
 * Tuple instances can be converted to a list form using the asList method.<br/>
 * Example:
 * <pre>{@code
 *     Tuple.of(1, 2) // Tuple.T2<Integer,Integer>
 * }</pre>
 *
 * @author TT432
 */
public sealed interface Tuple permits Tuple.T10, Tuple.T11, Tuple.T12, Tuple.T13, Tuple.T14, Tuple.T15, Tuple.T16, Tuple.T2, Tuple.T3, Tuple.T4, Tuple.T5, Tuple.T6, Tuple.T7, Tuple.T8, Tuple.T9 {
    List<Object> asList();

    static <A, B> T2<A, B> of(A a, B b) {
        return new T2<>(a, b);
    }

    static <A, B, C> T3<A, B, C> of(A a, B b, C c) {
        return new T3<>(a, b, c);
    }

    static <A, B, C, D> T4<A, B, C, D> of(A a, B b, C c, D d) {
        return new T4<>(a, b, c, d);
    }

    static <A, B, C, D, E> T5<A, B, C, D, E> of(A a, B b, C c, D d, E e) {
        return new T5<>(a, b, c, d, e);
    }

    static <A, B, C, D, E, F> T6<A, B, C, D, E, F> of(A a, B b, C c, D d, E e, F f) {
        return new T6<>(a, b, c, d, e, f);
    }

    static <A, B, C, D, E, F, G> T7<A, B, C, D, E, F, G> of(A a, B b, C c, D d, E e, F f, G g) {
        return new T7<>(a, b, c, d, e, f, g);
    }

    static <A, B, C, D, E, F, G, H> T8<A, B, C, D, E, F, G, H> of(A a, B b, C c, D d, E e, F f, G g, H h) {
        return new T8<>(a, b, c, d, e, f, g, h);
    }

    static <A, B, C, D, E, F, G, H, I> T9<A, B, C, D, E, F, G, H, I> of(A a, B b, C c, D d, E e, F f, G g, H h, I i) {
        return new T9<>(a, b, c, d, e, f, g, h, i);
    }

    static <A, B, C, D, E, F, G, H, I, J> T10<A, B, C, D, E, F, G, H, I, J> of(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j) {
        return new T10<>(a, b, c, d, e, f, g, h, i, j);
    }

    static <A, B, C, D, E, F, G, H, I, J, K> T11<A, B, C, D, E, F, G, H, I, J, K> of(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j, K k) {
        return new T11<>(a, b, c, d, e, f, g, h, i, j, k);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L> T12<A, B, C, D, E, F, G, H, I, J, K, L> of(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j, K k, L l) {
        return new T12<>(a, b, c, d, e, f, g, h, i, j, k, l);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M> T13<A, B, C, D, E, F, G, H, I, J, K, L, M> of(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j, K k, L l, M m) {
        return new T13<>(a, b, c, d, e, f, g, h, i, j, k, l, m);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M, N> T14<A, B, C, D, E, F, G, H, I, J, K, L, M, N> of(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j, K k, L l, M m, N n) {
        return new T14<>(a, b, c, d, e, f, g, h, i, j, k, l, m, n);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O> T15<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O> of(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j, K k, L l, M m, N n, O o) {
        return new T15<>(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P> T16<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P> of(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j, K k, L l, M m, N n, O o, P p) {
        return new T16<>(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p);
    }

    record T2<A, B>(A a, B b) implements Tuple {
        @Override
        public List<Object> asList() {
            return List.of(a, b);
        }
    }

    record T3<A, B, C>(A a, B b, C c) implements Tuple {
        @Override
        public List<Object> asList() {
            return List.of(a, b, c);
        }
    }

    record T4<A, B, C, D>(A a, B b, C c, D d) implements Tuple {
        @Override
        public List<Object> asList() {
            return List.of(a, b, c, d);
        }
    }

    record T5<A, B, C, D, E>(A a, B b, C c, D d, E e) implements Tuple {
        @Override
        public List<Object> asList() {
            return List.of(a, b, c, d, e);
        }
    }

    record T6<A, B, C, D, E, F>(A a, B b, C c, D d, E e, F f) implements Tuple {
        @Override
        public List<Object> asList() {
            return List.of(a, b, c, d, e, f);
        }
    }

    record T7<A, B, C, D, E, F, G>(A a, B b, C c, D d, E e, F f, G g) implements Tuple {
        @Override
        public List<Object> asList() {
            return List.of(a, b, c, d, e, f, g);
        }
    }

    record T8<A, B, C, D, E, F, G, H>(A a, B b, C c, D d, E e, F f, G g, H h) implements Tuple {
        @Override
        public List<Object> asList() {
            return List.of(a, b, c, d, e, f, g, h);
        }
    }

    record T9<A, B, C, D, E, F, G, H, I>(A a, B b, C c, D d, E e, F f, G g, H h, I i) implements Tuple {
        @Override
        public List<Object> asList() {
            return List.of(a, b, c, d, e, f, g, h, i);
        }
    }

    record T10<A, B, C, D, E, F, G, H, I, J>(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j) implements Tuple {
        @Override
        public List<Object> asList() {
            return List.of(a, b, c, d, e, f, g, h, i, j);
        }
    }

    record T11<A, B, C, D, E, F, G, H, I, J, K>(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j,
                                                K k) implements Tuple {
        @Override
        public List<Object> asList() {
            return List.of(a, b, c, d, e, f, g, h, i, j, k);
        }
    }

    record T12<A, B, C, D, E, F, G, H, I, J, K, L>(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j, K k,
                                                   L l) implements Tuple {
        @Override
        public List<Object> asList() {
            return List.of(a, b, c, d, e, f, g, h, i, j, k, l);
        }
    }

    record T13<A, B, C, D, E, F, G, H, I, J, K, L, M>(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j, K k, L l,
                                                      M m) implements Tuple {
        @Override
        public List<Object> asList() {
            return List.of(a, b, c, d, e, f, g, h, i, j, k, l, m);
        }
    }

    record T14<A, B, C, D, E, F, G, H, I, J, K, L, M, N>(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j, K k, L l,
                                                         M m, N n) implements Tuple {
        @Override
        public List<Object> asList() {
            return List.of(a, b, c, d, e, f, g, h, i, j, k, l, m, n);
        }
    }

    record T15<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O>(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j, K k, L l,
                                                            M m, N n, O o) implements Tuple {
        @Override
        public List<Object> asList() {
            return List.of(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o);
        }
    }

    record T16<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P>(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j, K k,
                                                               L l, M m, N n, O o, P p) implements Tuple {
        @Override
        public List<Object> asList() {
            return List.of(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p);
        }
    }
}
