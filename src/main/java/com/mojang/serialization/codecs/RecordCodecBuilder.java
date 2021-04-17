// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization.codecs;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public final class RecordCodecBuilder<O, F> implements App<RecordCodecBuilder.Mu<O>, F> {
    public static final class Mu<O> implements K1 {}

    public static <O, F> RecordCodecBuilder<O, F> unbox(final App<Mu<O>, F> box) {
        return ((RecordCodecBuilder<O, F>) box);
    }

    private final Function<O, F> getter;
    private final Function<O, MapEncoder<F>> encoder;
    private final MapDecoder<F> decoder;

    private RecordCodecBuilder(final Function<O, F> getter, final Function<O, MapEncoder<F>> encoder, final MapDecoder<F> decoder) {
        this.getter = getter;
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public static <O> Instance<O> instance() {
        return new Instance<>();
    }

    public static <O, F> RecordCodecBuilder<O, F> of(final Function<O, F> getter, final String name, final Codec<F> fieldCodec) {
        return of(getter, fieldCodec.fieldOf(name));
    }

    public static <O, F> RecordCodecBuilder<O, F> of(final Function<O, F> getter, final MapCodec<F> codec) {
        return new RecordCodecBuilder<>(getter, o -> codec, codec);
    }

    public static <O, F> RecordCodecBuilder<O, F> point(final F instance) {
        return new RecordCodecBuilder<>(o -> instance, o -> Encoder.empty(), Decoder.unit(instance));
    }

    public static <O, F> RecordCodecBuilder<O, F> stable(final F instance) {
        return point(instance, Lifecycle.stable());
    }

    public static <O, F> RecordCodecBuilder<O, F> deprecated(final F instance, final int since) {
        return point(instance, Lifecycle.deprecated(since));
    }

    public static <O, F> RecordCodecBuilder<O, F> point(final F instance, final Lifecycle lifecycle) {
        return new RecordCodecBuilder<>(o -> instance, o -> Encoder.<F>empty().withLifecycle(lifecycle), Decoder.unit(instance).withLifecycle(lifecycle));
    }

    public static <O> Codec<O> create(final Function<Instance<O>, ? extends App<Mu<O>, O>> builder) {
        return build(builder.apply(instance())).codec();
    }

    public static <O> MapCodec<O> mapCodec(final Function<Instance<O>, ? extends App<Mu<O>, O>> builder) {
        return build(builder.apply(instance()));
    }

    public <E> RecordCodecBuilder<O, E> dependent(final Function<O, E> getter, final MapEncoder<E> encoder, final Function<? super F, ? extends MapDecoder<E>> decoderGetter) {
        return new RecordCodecBuilder<>(
            getter,
            o -> encoder,
            new MapDecoder.Implementation<E>() {
                @Override
                public <T> DataResult<E> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                    return decoder.decode(ops, input).map(decoderGetter).flatMap(decoder1 -> decoder1.decode(ops, input).map(Function.identity()));
                }

                @Override
                public <T> Stream<T> keys(final DynamicOps<T> ops) {
                    return encoder.keys(ops);
                }

                @Override
                public String toString() {
                    return "Dependent[" + encoder + "]";
                }
            }
        );
    }

    public static <O> MapCodec<O> build(final App<Mu<O>, O> builderBox) {
        final RecordCodecBuilder<O, O> builder = unbox(builderBox);
        return new MapCodec<O>() {
            @Override
            public <T> DataResult<O> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return builder.decoder.decode(ops, input);
            }

            @Override
            public <T> RecordBuilder<T> encode(final O input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                return builder.encoder.apply(input).encode(input, ops, prefix);
            }

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return builder.decoder.keys(ops);
            }

            @Override
            public String toString() {
                return "RecordCodec[" + builder.decoder + "]";
            }
        };
    }

    public static final class Instance<O> implements Applicative<Mu<O>, Instance.Mu<O>> {
        private static final class Mu<O> implements Applicative.Mu {}

        public <A> App<RecordCodecBuilder.Mu<O>, A> stable(final A a) {
            return RecordCodecBuilder.stable(a);
        }

        public <A> App<RecordCodecBuilder.Mu<O>, A> deprecated(final A a, final int since) {
            return RecordCodecBuilder.deprecated(a, since);
        }

        public <A> App<RecordCodecBuilder.Mu<O>, A> point(final A a, final Lifecycle lifecycle) {
            return RecordCodecBuilder.point(a, lifecycle);
        }

        @Override
        public <A> App<RecordCodecBuilder.Mu<O>, A> point(final A a) {
            return RecordCodecBuilder.point(a);
        }

        @Override
        public <A, R> Function<App<RecordCodecBuilder.Mu<O>, A>, App<RecordCodecBuilder.Mu<O>, R>> lift1(final App<RecordCodecBuilder.Mu<O>, Function<A, R>> function) {
            return fa -> {
                final RecordCodecBuilder<O, Function<A, R>> f = unbox(function);
                final RecordCodecBuilder<O, A> a = unbox(fa);

                return new RecordCodecBuilder<>(
                    o -> f.getter.apply(o).apply(a.getter.apply(o)),
                    o -> {
                        final MapEncoder<Function<A, R>> fEnc = f.encoder.apply(o);
                        final MapEncoder<A> aEnc = a.encoder.apply(o);
                        final A aFromO = a.getter.apply(o);

                        return new MapEncoder.Implementation<R>() {
                            @Override
                            public <T> RecordBuilder<T> encode(final R input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                                aEnc.encode(aFromO, ops, prefix);
                                fEnc.encode(a1 -> input, ops, prefix);
                                return prefix;
                            }

                            @Override
                            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                                return Stream.concat(aEnc.keys(ops), fEnc.keys(ops));
                            }

                            @Override
                            public String toString() {
                                return fEnc + " * " + aEnc;
                            }
                        };
                    },

                    new MapDecoder.Implementation<R>() {
                        @Override
                        public <T> DataResult<R> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                            return a.decoder.decode(ops, input).flatMap(ar ->
                                f.decoder.decode(ops, input).map(fr ->
                                    fr.apply(ar)
                                )
                            );
                        }

                        @Override
                        public <T> Stream<T> keys(final DynamicOps<T> ops) {
                            return Stream.concat(a.decoder.keys(ops), f.decoder.keys(ops));
                        }

                        @Override
                        public String toString() {
                            return f.decoder + " * " + a.decoder;
                        }
                    }
                );
            };
        }

        @Override
        public <A, B, R> App<RecordCodecBuilder.Mu<O>, R> ap2(final App<RecordCodecBuilder.Mu<O>, BiFunction<A, B, R>> func, final App<RecordCodecBuilder.Mu<O>, A> a, final App<RecordCodecBuilder.Mu<O>, B> b) {
            final RecordCodecBuilder<O, BiFunction<A, B, R>> function = unbox(func);
            final RecordCodecBuilder<O, A> fa = unbox(a);
            final RecordCodecBuilder<O, B> fb = unbox(b);

            return new RecordCodecBuilder<>(
                o -> function.getter.apply(o).apply(fa.getter.apply(o), fb.getter.apply(o)),
                o -> {
                    final MapEncoder<BiFunction<A, B, R>> fEncoder = function.encoder.apply(o);
                    final MapEncoder<A> aEncoder = fa.encoder.apply(o);
                    final A aFromO = fa.getter.apply(o);
                    final MapEncoder<B> bEncoder = fb.encoder.apply(o);
                    final B bFromO = fb.getter.apply(o);

                    return new MapEncoder.Implementation<R>() {
                        @Override
                        public <T> RecordBuilder<T> encode(final R input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                            aEncoder.encode(aFromO, ops, prefix);
                            bEncoder.encode(bFromO, ops, prefix);
                            fEncoder.encode((a1, b1) -> input, ops, prefix);
                            return prefix;
                        }

                        @Override
                        public <T> Stream<T> keys(final DynamicOps<T> ops) {
                            return Stream.of(
                                fEncoder.keys(ops),
                                aEncoder.keys(ops),
                                bEncoder.keys(ops)
                            ).flatMap(Function.identity());
                        }

                        @Override
                        public String toString() {
                            return fEncoder + " * " + aEncoder + " * " + bEncoder;
                        }
                    };
                },
                new MapDecoder.Implementation<R>() {
                    @Override
                    public <T> DataResult<R> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                        return DataResult.unbox(DataResult.instance().ap2(
                            function.decoder.decode(ops, input),
                            fa.decoder.decode(ops, input),
                            fb.decoder.decode(ops, input)
                        ));
                    }

                    @Override
                    public <T> Stream<T> keys(final DynamicOps<T> ops) {
                        return Stream.of(
                            function.decoder.keys(ops),
                            fa.decoder.keys(ops),
                            fb.decoder.keys(ops)
                        ).flatMap(Function.identity());
                    }

                    @Override
                    public String toString() {
                        return function.decoder + " * " + fa.decoder + " * " + fb.decoder;
                    }
                }
            );
        }

        @Override
        public <T1, T2, T3, R> App<RecordCodecBuilder.Mu<O>, R> ap3(final App<RecordCodecBuilder.Mu<O>, Function3<T1, T2, T3, R>> func, final App<RecordCodecBuilder.Mu<O>, T1> t1, final App<RecordCodecBuilder.Mu<O>, T2> t2, final App<RecordCodecBuilder.Mu<O>, T3> t3) {
            final RecordCodecBuilder<O, Function3<T1, T2, T3, R>> function = unbox(func);
            final RecordCodecBuilder<O, T1> f1 = unbox(t1);
            final RecordCodecBuilder<O, T2> f2 = unbox(t2);
            final RecordCodecBuilder<O, T3> f3 = unbox(t3);

            return new RecordCodecBuilder<>(
                o -> function.getter.apply(o).apply(
                    f1.getter.apply(o),
                    f2.getter.apply(o),
                    f3.getter.apply(o)
                ),
                o -> {
                    final MapEncoder<Function3<T1, T2, T3, R>> fEncoder = function.encoder.apply(o);
                    final MapEncoder<T1> e1 = f1.encoder.apply(o);
                    final T1 v1 = f1.getter.apply(o);
                    final MapEncoder<T2> e2 = f2.encoder.apply(o);
                    final T2 v2 = f2.getter.apply(o);
                    final MapEncoder<T3> e3 = f3.encoder.apply(o);
                    final T3 v3 = f3.getter.apply(o);

                    return new MapEncoder.Implementation<R>() {
                        @Override
                        public <T> RecordBuilder<T> encode(final R input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                            e1.encode(v1, ops, prefix);
                            e2.encode(v2, ops, prefix);
                            e3.encode(v3, ops, prefix);
                            fEncoder.encode((t1, t2, t3) -> input, ops, prefix);
                            return prefix;
                        }

                        @Override
                        public <T> Stream<T> keys(final DynamicOps<T> ops) {
                            return Stream.of(
                                fEncoder.keys(ops),
                                e1.keys(ops),
                                e2.keys(ops),
                                e3.keys(ops)
                            ).flatMap(Function.identity());
                        }

                        @Override
                        public String toString() {
                            return fEncoder + " * " + e1 + " * " + e2 + " * " + e3;
                        }
                    };
                },
                new MapDecoder.Implementation<R>() {
                    @Override
                    public <T> DataResult<R> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                        return DataResult.unbox(DataResult.instance().ap3(
                            function.decoder.decode(ops, input),
                            f1.decoder.decode(ops, input),
                            f2.decoder.decode(ops, input),
                            f3.decoder.decode(ops, input)
                        ));
                    }

                    @Override
                    public <T> Stream<T> keys(final DynamicOps<T> ops) {
                        return Stream.of(
                            function.decoder.keys(ops),
                            f1.decoder.keys(ops),
                            f2.decoder.keys(ops),
                            f3.decoder.keys(ops)
                        ).flatMap(Function.identity());
                    }

                    @Override
                    public String toString() {
                        return function.decoder + " * " + f1.decoder + " * " + f2.decoder + " * " + f3.decoder;
                    }
                }
            );
        }

        @Override
        public <T1, T2, T3, T4, R> App<RecordCodecBuilder.Mu<O>, R> ap4(final App<RecordCodecBuilder.Mu<O>, Function4<T1, T2, T3, T4, R>> func, final App<RecordCodecBuilder.Mu<O>, T1> t1, final App<RecordCodecBuilder.Mu<O>, T2> t2, final App<RecordCodecBuilder.Mu<O>, T3> t3, final App<RecordCodecBuilder.Mu<O>, T4> t4) {
            final RecordCodecBuilder<O, Function4<T1, T2, T3, T4, R>> function = unbox(func);
            final RecordCodecBuilder<O, T1> f1 = unbox(t1);
            final RecordCodecBuilder<O, T2> f2 = unbox(t2);
            final RecordCodecBuilder<O, T3> f3 = unbox(t3);
            final RecordCodecBuilder<O, T4> f4 = unbox(t4);

            return new RecordCodecBuilder<>(
                o -> function.getter.apply(o).apply(
                    f1.getter.apply(o),
                    f2.getter.apply(o),
                    f3.getter.apply(o),
                    f4.getter.apply(o)
                ),
                o -> {
                    final MapEncoder<Function4<T1, T2, T3, T4, R>> fEncoder = function.encoder.apply(o);
                    final MapEncoder<T1> e1 = f1.encoder.apply(o);
                    final T1 v1 = f1.getter.apply(o);
                    final MapEncoder<T2> e2 = f2.encoder.apply(o);
                    final T2 v2 = f2.getter.apply(o);
                    final MapEncoder<T3> e3 = f3.encoder.apply(o);
                    final T3 v3 = f3.getter.apply(o);
                    final MapEncoder<T4> e4 = f4.encoder.apply(o);
                    final T4 v4 = f4.getter.apply(o);

                    return new MapEncoder.Implementation<R>() {
                        @Override
                        public <T> RecordBuilder<T> encode(final R input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                            e1.encode(v1, ops, prefix);
                            e2.encode(v2, ops, prefix);
                            e3.encode(v3, ops, prefix);
                            e4.encode(v4, ops, prefix);
                            fEncoder.encode((t1, t2, t3, t4) -> input, ops, prefix);
                            return prefix;
                        }

                        @Override
                        public <T> Stream<T> keys(final DynamicOps<T> ops) {
                            return Stream.of(
                                fEncoder.keys(ops),
                                e1.keys(ops),
                                e2.keys(ops),
                                e3.keys(ops),
                                e4.keys(ops)
                            ).flatMap(Function.identity());
                        }

                        @Override
                        public String toString() {
                            return fEncoder + " * " + e1 + " * " + e2 + " * " + e3 + " * " + e4;
                        }
                    };
                },
                new MapDecoder.Implementation<R>() {
                    @Override
                    public <T> DataResult<R> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                        return DataResult.unbox(DataResult.instance().ap4(
                            function.decoder.decode(ops, input),
                            f1.decoder.decode(ops, input),
                            f2.decoder.decode(ops, input),
                            f3.decoder.decode(ops, input),
                            f4.decoder.decode(ops, input)
                        ));
                    }

                    @Override
                    public <T> Stream<T> keys(final DynamicOps<T> ops) {
                        return Stream.of(
                            function.decoder.keys(ops),
                            f1.decoder.keys(ops),
                            f2.decoder.keys(ops),
                            f3.decoder.keys(ops),
                            f4.decoder.keys(ops)
                        ).flatMap(Function.identity());
                    }

                    @Override
                    public String toString() {
                        return function.decoder + " * " + f1.decoder + " * " + f2.decoder + " * " + f3.decoder + " * " + f4.decoder;
                    }
                }
            );
        }

        @Override
        public <T, R> App<RecordCodecBuilder.Mu<O>, R> map(final Function<? super T, ? extends R> func, final App<RecordCodecBuilder.Mu<O>, T> ts) {
            final RecordCodecBuilder<O, T> unbox = unbox(ts);
            final Function<O, T> getter = unbox.getter;
            return new RecordCodecBuilder<>(
                getter.andThen(func),
                o -> new MapEncoder.Implementation<R>() {
                    private final MapEncoder<T> encoder = unbox.encoder.apply(o);

                    @Override
                    public <U> RecordBuilder<U> encode(final R input, final DynamicOps<U> ops, final RecordBuilder<U> prefix) {
                        return encoder.encode(getter.apply(o), ops, prefix);
                    }

                    @Override
                    public <U> Stream<U> keys(final DynamicOps<U> ops) {
                        return encoder.keys(ops);
                    }

                    @Override
                    public String toString() {
                        return encoder + "[mapped]";
                    }
                },
                unbox.decoder.map(func)
            );
        }
    }
}
