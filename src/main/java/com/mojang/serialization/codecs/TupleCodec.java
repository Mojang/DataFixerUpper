package com.mojang.serialization.codecs;

import com.mojang.datafixers.util.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.mojang.datafixers.util.Tuple.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * The TupleCodec class provides a heterogeneous list akin to how Tuple relates to List in a similar way Object does to Map. <br/>
 * Example:
 * <pre>{@code
 *      var codec = TupleCodec.tuple(Codec.INT, Codec.FLOAT); // T2Codec<Integer, Float>
 *      codec.decode("[1, 2.0]"); // Decodes to List<Integer, Float>(1, 2.0)
 * }</pre>
 * @author TT432
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public sealed interface TupleCodec extends Codec<List<Object>> {
    List<Codec<?>> getCodecs();

    static <A> T1Codec<A> tuple(Codec<A> codec1) {
        return new T1Codec<>(codec1);
    }

    static <A, B> T2Codec<A, B> tuple(Codec<A> codec1, Codec<B> codec2) {
        return new T2Codec<>(codec1, codec2);
    }

    static <A, B, C> T3Codec<A, B, C> tuple(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3) {
        return new T3Codec<>(codec1, codec2, codec3);
    }

    static <A, B, C, D> T4Codec<A, B, C, D> tuple(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4) {
        return new T4Codec<>(codec1, codec2, codec3, codec4);
    }

    static <A, B, C, D, E> T5Codec<A, B, C, D, E> tuple(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4, Codec<E> codec5) {
        return new T5Codec<>(codec1, codec2, codec3, codec4, codec5);
    }

    static <A, B, C, D, E, F> T6Codec<A, B, C, D, E, F> tuple(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4, Codec<E> codec5, Codec<F> codec6) {
        return new T6Codec<>(codec1, codec2, codec3, codec4, codec5, codec6);
    }

    static <A, B, C, D, E, F, G> T7Codec<A, B, C, D, E, F, G> tuple(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4, Codec<E> codec5, Codec<F> codec6, Codec<G> codec7) {
        return new T7Codec<>(codec1, codec2, codec3, codec4, codec5, codec6, codec7);
    }

    static <A, B, C, D, E, F, G, H> T8Codec<A, B, C, D, E, F, G, H> tuple(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4, Codec<E> codec5, Codec<F> codec6, Codec<G> codec7, Codec<H> codec8) {
        return new T8Codec<>(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8);
    }

    static <A, B, C, D, E, F, G, H, I> T9Codec<A, B, C, D, E, F, G, H, I> tuple(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4, Codec<E> codec5, Codec<F> codec6, Codec<G> codec7, Codec<H> codec8, Codec<I> codec9) {
        return new T9Codec<>(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9);
    }

    static <A, B, C, D, E, F, G, H, I, J> T10Codec<A, B, C, D, E, F, G, H, I, J> tuple(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4, Codec<E> codec5, Codec<F> codec6, Codec<G> codec7, Codec<H> codec8, Codec<I> codec9, Codec<J> codec10) {
        return new T10Codec<>(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9, codec10);
    }

    static <A, B, C, D, E, F, G, H, I, J, K> T11Codec<A, B, C, D, E, F, G, H, I, J, K> tuple(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4, Codec<E> codec5, Codec<F> codec6, Codec<G> codec7, Codec<H> codec8, Codec<I> codec9, Codec<J> codec10, Codec<K> codec11) {
        return new T11Codec<>(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9, codec10, codec11);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L> T12Codec<A, B, C, D, E, F, G, H, I, J, K, L> tuple(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4, Codec<E> codec5, Codec<F> codec6, Codec<G> codec7, Codec<H> codec8, Codec<I> codec9, Codec<J> codec10, Codec<K> codec11, Codec<L> codec12) {
        return new T12Codec<>(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9, codec10, codec11, codec12);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M> T13Codec<A, B, C, D, E, F, G, H, I, J, K, L, M> tuple(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4, Codec<E> codec5, Codec<F> codec6, Codec<G> codec7, Codec<H> codec8, Codec<I> codec9, Codec<J> codec10, Codec<K> codec11, Codec<L> codec12, Codec<M> codec13) {
        return new T13Codec<>(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9, codec10, codec11, codec12, codec13);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M, N> T14Codec<A, B, C, D, E, F, G, H, I, J, K, L, M, N> tuple(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4, Codec<E> codec5, Codec<F> codec6, Codec<G> codec7, Codec<H> codec8, Codec<I> codec9, Codec<J> codec10, Codec<K> codec11, Codec<L> codec12, Codec<M> codec13, Codec<N> codec14) {
        return new T14Codec<>(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9, codec10, codec11, codec12, codec13, codec14);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O> T15Codec<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O> tuple(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4, Codec<E> codec5, Codec<F> codec6, Codec<G> codec7, Codec<H> codec8, Codec<I> codec9, Codec<J> codec10, Codec<K> codec11, Codec<L> codec12, Codec<M> codec13, Codec<N> codec14, Codec<O> codec15) {
        return new T15Codec<>(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9, codec10, codec11, codec12, codec13, codec14, codec15);
    }

    static <A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P> T16Codec<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P> tuple(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4, Codec<E> codec5, Codec<F> codec6, Codec<G> codec7, Codec<H> codec8, Codec<I> codec9, Codec<J> codec10, Codec<K> codec11, Codec<L> codec12, Codec<M> codec13, Codec<N> codec14, Codec<O> codec15, Codec<P> codec16) {
        return new T16Codec<>(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9, codec10, codec11, codec12, codec13, codec14, codec15, codec16);
    }

    @Override
    default <T> DataResult<Pair<List<Object>, T>> decode(DynamicOps<T> ops, T input) {
        DataResult<Stream<T>> stream = ops.getStream(input);
        return stream.error()
                .<DataResult<Pair<List<Object>, T>>>map(streamPartialResult ->
                        DataResult.error(streamPartialResult::message))
                .orElse(stream.result().<DataResult<Pair<List<Object>, T>>>map(s -> {
                    List<Codec<?>> codecs = getCodecs();
                    List<T> list = s.toList();

                    if (list.size() != codecs.size()) {
                        return DataResult.error(() -> "can't process as " + this + ", size not equals.");
                    }

                    List<Object> result = new ArrayList<>();

                    for (int i = 0; i < codecs.size(); i++) {
                        var decode = codecs.get(i).decode(ops, list.get(i)).get().mapBoth(Pair::getFirst, Function.identity());

                        decode.ifLeft(result::add);

                        var right = decode.right();

                        if (right.isPresent()) {
                            return DataResult.error(() -> this + " error: " + right.get().message());
                        }
                    }

                    return DataResult.success(Pair.of(result, input));
                }).orElse(DataResult.error(() -> this + " can't parse as list.")));
    }

    @Override
    default <T> DataResult<T> encode(List<Object> input, DynamicOps<T> ops, T prefix) {
        List<Codec<?>> codecs = getCodecs();

        if (input.size() != codecs.size())
            return DataResult.error(() -> "can't encode " + this + ", because input array size not equals this.");

        final ListBuilder<T> builder = ops.listBuilder();

        for (int i = 0; i < input.size(); i++) {
            builder.add(((Codec) codecs.get(i)).encodeStart(ops, input.get(i)));
        }

        return builder.build(prefix);
    }

    record T1Codec<A>(Codec<A> codec) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec);
        }

        public <T> Codec<T> bmap(Function<A, T> to, Function<T, A> from) {
            return xmap(l -> to.apply((A) l.get(0)), from.andThen(List::of));
        }

        @Override
        public String toString() {
            return "Tuple1[" + codec + "]";
        }
    }

    record T2Codec<A, B>(Codec<A> codec1, Codec<B> codec2) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec1, codec2);
        }

        public <T> Codec<T> bmap(BiFunction<A, B, T> to, Function<T, T2<A, B>> from) {
            return xmap(l -> to.apply((A) l.get(0), (B) l.get(1)), from.andThen(Tuple::asList));
        }

        @Override
        public String toString() {
            return "Tuple2[" + codec1 + ", " + codec2 + "]";
        }
    }

    record T3Codec<A, B, C>(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec1, codec2, codec3);
        }

        public <T> Codec<T> bmap(Function3<A, B, C, T> to, Function<T, T3<A, B, C>> from) {
            return xmap(l -> to.apply((A) l.get(0), (B) l.get(1), (C) l.get(2)), from.andThen(Tuple::asList));
        }

        @Override
        public String toString() {
            return "Tuple3[" + codec1 + ", " + codec2 + ", " + codec3 + "]";
        }
    }

    record T4Codec<A, B, C, D>(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3,
                               Codec<D> codec4) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec1, codec2, codec3, codec4);
        }

        public <T> Codec<T> bmap(Function4<A, B, C, D, T> to, Function<T, T4<A, B, C, D>> from) {
            return xmap(l -> to.apply((A) l.get(0), (B) l.get(1), (C) l.get(2), (D) l.get(3)), from.andThen(Tuple::asList));
        }

        @Override
        public String toString() {
            return "Tuple4[" + codec1 + ", " + codec2 + ", " + codec3 + ", " + codec4 + "]";
        }
    }

    record T5Codec<A, B, C, D, E>(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4,
                                  Codec<E> codec5) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec1, codec2, codec3, codec4, codec5);
        }

        public <T> Codec<T> bmap(Function5<A, B, C, D, E, T> to, Function<T, T5<A, B, C, D, E>> from) {
            return xmap(l -> to.apply((A) l.get(0), (B) l.get(1), (C) l.get(2), (D) l.get(3), (E) l.get(4)), from.andThen(Tuple::asList));
        }

        @Override
        public String toString() {
            return "Tuple5[" + codec1 + ", " + codec2 + ", " + codec3 + ", " + codec4 + ", " + codec5 + "]";
        }
    }

    record T6Codec<A, B, C, D, E, F>(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4,
                                     Codec<E> codec5, Codec<F> codec6) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec1, codec2, codec3, codec4, codec5, codec6);
        }

        public <T> Codec<T> bmap(Function6<A, B, C, D, E, F, T> to, Function<T, T6<A, B, C, D, E, F>> from) {
            return xmap(l -> to.apply((A) l.get(0), (B) l.get(1), (C) l.get(2), (D) l.get(3), (E) l.get(4), (F) l.get(5)), from.andThen(Tuple::asList));
        }

        @Override
        public String toString() {
            return "Tuple6[" + codec1 + ", " + codec2 + ", " + codec3 + ", " + codec4 + ", " + codec5 + ", " + codec6 + "]";
        }
    }

    record T7Codec<A, B, C, D, E, F, G>(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4,
                                        Codec<E> codec5, Codec<F> codec6, Codec<G> codec7) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec1, codec2, codec3, codec4, codec5, codec6, codec7);
        }

        public <T> Codec<T> bmap(Function7<A, B, C, D, E, F, G, T> to, Function<T, T7<A, B, C, D, E, F, G>> from) {
            return xmap(l -> to.apply((A) l.get(0), (B) l.get(1), (C) l.get(2), (D) l.get(3), (E) l.get(4), (F) l.get(5), (G) l.get(6)), from.andThen(Tuple::asList));
        }

        @Override
        public String toString() {
            return "Tuple7[" + codec1 + ", " + codec2 + ", " + codec3 + ", " + codec4 + ", " + codec5 + ", " + codec6 + ", " + codec7 + "]";
        }
    }

    record T8Codec<A, B, C, D, E, F, G, H>(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4,
                                           Codec<E> codec5, Codec<F> codec6, Codec<G> codec7,
                                           Codec<H> codec8) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8);
        }

        public <T> Codec<T> bmap(Function8<A, B, C, D, E, F, G, H, T> to, Function<T, T8<A, B, C, D, E, F, G, H>> from) {
            return xmap(l -> to.apply((A) l.get(0), (B) l.get(1), (C) l.get(2), (D) l.get(3), (E) l.get(4), (F) l.get(5), (G) l.get(6), (H) l.get(7)), from.andThen(Tuple::asList));
        }

        @Override
        public String toString() {
            return "Tuple8[" + codec1 + ", " + codec2 + ", " + codec3 + ", " + codec4 + ", " + codec5 + ", " + codec6 + ", " + codec7 + ", " + codec8 + "]";
        }
    }

    record T9Codec<A, B, C, D, E, F, G, H, I>(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4,
                                              Codec<E> codec5, Codec<F> codec6, Codec<G> codec7, Codec<H> codec8,
                                              Codec<I> codec9) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9);
        }

        public <T> Codec<T> bmap(Function9<A, B, C, D, E, F, G, H, I, T> to, Function<T, T9<A, B, C, D, E, F, G, H, I>> from) {
            return xmap(l -> to.apply((A) l.get(0), (B) l.get(1), (C) l.get(2), (D) l.get(3), (E) l.get(4), (F) l.get(5), (G) l.get(6), (H) l.get(7), (I) l.get(8)), from.andThen(Tuple::asList));
        }

        @Override
        public String toString() {
            return "Tuple9[" + codec1 + ", " + codec2 + ", " + codec3 + ", " + codec4 + ", " + codec5 + ", " + codec6 + ", " + codec7 + ", " + codec8 + ", " + codec9 + "]";
        }
    }

    record T10Codec<A, B, C, D, E, F, G, H, I, J>(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4,
                                                  Codec<E> codec5, Codec<F> codec6, Codec<G> codec7, Codec<H> codec8,
                                                  Codec<I> codec9, Codec<J> codec10) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9, codec10);
        }

        public <T> Codec<T> bmap(Function10<A, B, C, D, E, F, G, H, I, J, T> to, Function<T, T10<A, B, C, D, E, F, G, H, I, J>> from) {
            return xmap(l -> to.apply((A) l.get(0), (B) l.get(1), (C) l.get(2), (D) l.get(3), (E) l.get(4), (F) l.get(5), (G) l.get(6), (H) l.get(7), (I) l.get(8), (J) l.get(9)), from.andThen(Tuple::asList));
        }

        @Override
        public String toString() {
            return "Tuple10[" + codec1 + ", " + codec2 + ", " + codec3 + ", " + codec4 + ", " + codec5 + ", " + codec6 + ", " + codec7 + ", " + codec8 + ", " + codec9 + ", " + codec10 + "]";
        }
    }

    record T11Codec<A, B, C, D, E, F, G, H, I, J, K>(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3, Codec<D> codec4,
                                                     Codec<E> codec5, Codec<F> codec6, Codec<G> codec7, Codec<H> codec8,
                                                     Codec<I> codec9, Codec<J> codec10,
                                                     Codec<K> codec11) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9, codec10, codec11);
        }

        public <T> Codec<T> bmap(Function11<A, B, C, D, E, F, G, H, I, J, K, T> to, Function<T, T11<A, B, C, D, E, F, G, H, I, J, K>> from) {
            return xmap(l -> to.apply((A) l.get(0), (B) l.get(1), (C) l.get(2), (D) l.get(3), (E) l.get(4), (F) l.get(5), (G) l.get(6), (H) l.get(7), (I) l.get(8), (J) l.get(9), (K) l.get(10)), from.andThen(Tuple::asList));
        }

        @Override
        public String toString() {
            return "Tuple11[" + codec1 + ", " + codec2 + ", " + codec3 + ", " + codec4 + ", " + codec5 + ", " + codec6 + ", " + codec7 + ", " + codec8 + ", " + codec9 + ", " + codec10 + ", " + codec11 + "]";
        }
    }

    record T12Codec<A, B, C, D, E, F, G, H, I, J, K, L>(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3,
                                                        Codec<D> codec4, Codec<E> codec5, Codec<F> codec6,
                                                        Codec<G> codec7, Codec<H> codec8, Codec<I> codec9,
                                                        Codec<J> codec10, Codec<K> codec11,
                                                        Codec<L> codec12) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9, codec10, codec11, codec12);
        }

        public <T> Codec<T> bmap(Function12<A, B, C, D, E, F, G, H, I, J, K, L, T> to, Function<T, T12<A, B, C, D, E, F, G, H, I, J, K, L>> from) {
            return xmap(l -> to.apply((A) l.get(0), (B) l.get(1), (C) l.get(2), (D) l.get(3), (E) l.get(4), (F) l.get(5), (G) l.get(6), (H) l.get(7), (I) l.get(8), (J) l.get(9), (K) l.get(10), (L) l.get(11)), from.andThen(Tuple::asList));
        }

        @Override
        public String toString() {
            return "Tuple12[" + codec1 + ", " + codec2 + ", " + codec3 + ", " + codec4 + ", " + codec5 + ", " + codec6 + ", " + codec7 + ", " + codec8 + ", " + codec9 + ", " + codec10 + ", " + codec11 + ", " + codec12 + "]";
        }
    }

    record T13Codec<A, B, C, D, E, F, G, H, I, J, K, L, M>(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3,
                                                           Codec<D> codec4, Codec<E> codec5, Codec<F> codec6,
                                                           Codec<G> codec7, Codec<H> codec8, Codec<I> codec9,
                                                           Codec<J> codec10, Codec<K> codec11, Codec<L> codec12,
                                                           Codec<M> codec13) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9, codec10, codec11, codec12, codec13);
        }

        public <T> Codec<T> bmap(Function13<A, B, C, D, E, F, G, H, I, J, K, L, M, T> to, Function<T, T13<A, B, C, D, E, F, G, H, I, J, K, L, M>> from) {
            return xmap(l -> to.apply((A) l.get(0), (B) l.get(1), (C) l.get(2), (D) l.get(3), (E) l.get(4), (F) l.get(5), (G) l.get(6), (H) l.get(7), (I) l.get(8), (J) l.get(9), (K) l.get(10), (L) l.get(11), (M) l.get(12)), from.andThen(Tuple::asList));
        }

        @Override
        public String toString() {
            return "Tuple13[" + codec1 + ", " + codec2 + ", " + codec3 + ", " + codec4 + ", " + codec5 + ", " + codec6 + ", " + codec7 + ", " + codec8 + ", " + codec9 + ", " + codec10 + ", " + codec11 + ", " + codec12 + ", " + codec13 + "]";
        }
    }

    record T14Codec<A, B, C, D, E, F, G, H, I, J, K, L, M, N>(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3,
                                                              Codec<D> codec4, Codec<E> codec5, Codec<F> codec6,
                                                              Codec<G> codec7, Codec<H> codec8, Codec<I> codec9,
                                                              Codec<J> codec10, Codec<K> codec11, Codec<L> codec12,
                                                              Codec<M> codec13,
                                                              Codec<N> codec14) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9, codec10, codec11, codec12, codec13, codec14);
        }

        public <T> Codec<T> bmap(Function14<A, B, C, D, E, F, G, H, I, J, K, L, M, N, T> to, Function<T, T14<A, B, C, D, E, F, G, H, I, J, K, L, M, N>> from) {
            return xmap(l -> to.apply((A) l.get(0), (B) l.get(1), (C) l.get(2), (D) l.get(3), (E) l.get(4), (F) l.get(5), (G) l.get(6), (H) l.get(7), (I) l.get(8), (J) l.get(9), (K) l.get(10), (L) l.get(11), (M) l.get(12), (N) l.get(13)), from.andThen(Tuple::asList));
        }

        @Override
        public String toString() {
            return "Tuple14[" + codec1 + ", " + codec2 + ", " + codec3 + ", " + codec4 + ", " + codec5 + ", " + codec6 + ", " + codec7 + ", " + codec8 + ", " + codec9 + ", " + codec10 + ", " + codec11 + ", " + codec12 + ", " + codec13 + ", " + codec14 + "]";
        }
    }

    record T15Codec<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O>(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3,
                                                                 Codec<D> codec4, Codec<E> codec5, Codec<F> codec6,
                                                                 Codec<G> codec7, Codec<H> codec8, Codec<I> codec9,
                                                                 Codec<J> codec10, Codec<K> codec11, Codec<L> codec12,
                                                                 Codec<M> codec13, Codec<N> codec14,
                                                                 Codec<O> codec15) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9, codec10, codec11, codec12, codec13, codec14, codec15);
        }

        public <T> Codec<T> bmap(Function15<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, T> to, Function<T, T15<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O>> from) {
            return xmap(l -> to.apply((A) l.get(0), (B) l.get(1), (C) l.get(2), (D) l.get(3), (E) l.get(4), (F) l.get(5), (G) l.get(6), (H) l.get(7), (I) l.get(8), (J) l.get(9), (K) l.get(10), (L) l.get(11), (M) l.get(12), (N) l.get(13), (O) l.get(14)), from.andThen(Tuple::asList));
        }

        @Override
        public String toString() {
            return "Tuple15[" + codec1 + ", " + codec2 + ", " + codec3 + ", " + codec4 + ", " + codec5 + ", " + codec6 + ", " + codec7 + ", " + codec8 + ", " + codec9 + ", " + codec10 + ", " + codec11 + ", " + codec12 + ", " + codec13 + ", " + codec14 + ", " + codec15 + "]";
        }
    }

    record T16Codec<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P>(Codec<A> codec1, Codec<B> codec2, Codec<C> codec3,
                                                                    Codec<D> codec4, Codec<E> codec5, Codec<F> codec6,
                                                                    Codec<G> codec7, Codec<H> codec8, Codec<I> codec9,
                                                                    Codec<J> codec10, Codec<K> codec11,
                                                                    Codec<L> codec12, Codec<M> codec13,
                                                                    Codec<N> codec14, Codec<O> codec15,
                                                                    Codec<P> codec16) implements TupleCodec {
        @Override
        public List<Codec<?>> getCodecs() {
            return List.of(codec1, codec2, codec3, codec4, codec5, codec6, codec7, codec8, codec9, codec10, codec11, codec12, codec13, codec14, codec15, codec16);
        }

        public <T> Codec<T> bmap(Function16<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, T> to, Function<T, T16<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P>> from) {
            return xmap(l -> to.apply((A) l.get(0), (B) l.get(1), (C) l.get(2), (D) l.get(3), (E) l.get(4), (F) l.get(5), (G) l.get(6), (H) l.get(7), (I) l.get(8), (J) l.get(9), (K) l.get(10), (L) l.get(11), (M) l.get(12), (N) l.get(13), (O) l.get(14), (P) l.get(15)), from.andThen(Tuple::asList));
        }

        @Override
        public String toString() {
            return "Tuple16[" + codec1 + ", " + codec2 + ", " + codec3 + ", " + codec4 + ", " + codec5 + ", " + codec6 + ", " + codec7 + ", " + codec8 + ", " + codec9 + ", " + codec10 + ", " + codec11 + ", " + codec12 + ", " + codec13 + ", " + codec14 + ", " + codec15 + ", " + codec16 + "]";
        }
    }
}
