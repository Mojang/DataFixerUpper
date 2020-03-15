// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization.codecs;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class RecordCodecBuilder<O, F> implements App<RecordCodecBuilder.Mu<O>, F> {
    public static final class Mu<O> implements K1 {
    }

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

    public static <O, F, C extends MapDecoder<F> & MapEncoder<F>> RecordCodecBuilder<O, F> of(final Function<O, F> getter, final C codec) {
        return new RecordCodecBuilder<>(getter, o -> codec, codec);
    }

    public static <O, F> RecordCodecBuilder<O, F> point(final F instance) {
        return new RecordCodecBuilder<>(o -> instance, o -> Encoder.empty(), Decoder.unit(instance));
    }

    public static <O> Codec<O> build(final App<RecordCodecBuilder.Mu<O>, O> builder) {
        return build(unbox(builder));
    }

    public static <O> Codec<O> build(final RecordCodecBuilder<O, O> builder) {
        return new Codec<O>() {
            @Override
            public <T> DataResult<Pair<O, T>> decode(final DynamicOps<T> ops, final T input) {
                return builder.decoder.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(final O input, final DynamicOps<T> ops, final T prefix) {
                return builder.encoder.apply(input).encode(input, ops, prefix);
            }

            @Override
            public String toString() {
                return "RecordCodec[" + builder.decoder + "]";
            }
        };
    }

    public static final class Instance<O> implements Applicative<Mu<O>, Instance.Mu<O>> {
        private static final class Mu<O> implements Applicative.Mu {
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

                        return new MapEncoder<R>() {
                            @Override
                            public <T> RecordBuilder<T> encode(final R input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                                aEnc.encode(aFromO, ops, prefix);
                                fEnc.encode(a1 -> input, ops, prefix);
                                return prefix;
                            }
                        };
                    },

                    new MapDecoder<R>() {
                        @Override
                        public <T> DataResult<R> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                            return a.decoder.decode(ops, input).flatMap(ar ->
                                f.decoder.decode(ops, input).map(fr ->
                                    fr.apply(ar)
                                )
                            );
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

                    return new MapEncoder<R>() {
                        @Override
                        public <T> RecordBuilder<T> encode(final R input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                            aEncoder.encode(aFromO, ops, prefix);
                            bEncoder.encode(bFromO, ops, prefix);
                            fEncoder.encode((a1, b1) -> input, ops, prefix);
                            return prefix;
                        }
                    };
                },
                new MapDecoder<R>() {
                    @Override
                    public <T> DataResult<R> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                        return DataResult.unbox(DataResult.instance().group(
                            fa.decoder.decode(ops, input),
                            fb.decoder.decode(ops, input)
                        ).apply(function.decoder.decode(ops, input)));
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

                    return new MapEncoder<R>() {
                        @Override
                        public <T> RecordBuilder<T> encode(final R input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                            e1.encode(v1, ops, prefix);
                            e2.encode(v2, ops, prefix);
                            e3.encode(v3, ops, prefix);
                            fEncoder.encode((t1, t2, t3) -> input, ops, prefix);
                            return prefix;
                        }
                    };
                },
                new MapDecoder<R>() {
                    @Override
                    public <T> DataResult<R> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                        return DataResult.unbox(DataResult.instance().group(
                            f1.decoder.decode(ops, input),
                            f2.decoder.decode(ops, input),
                            f3.decoder.decode(ops, input)
                        ).apply(function.decoder.decode(ops, input)));
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

                    return new MapEncoder<R>() {
                        @Override
                        public <T> RecordBuilder<T> encode(final R input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                            e1.encode(v1, ops, prefix);
                            e2.encode(v2, ops, prefix);
                            e3.encode(v3, ops, prefix);
                            e4.encode(v4, ops, prefix);
                            fEncoder.encode((t1, t2, t3, t4) -> input, ops, prefix);
                            return prefix;
                        }
                    };
                },
                new MapDecoder<R>() {
                    @Override
                    public <T> DataResult<R> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                        return DataResult.unbox(DataResult.instance().group(
                            f1.decoder.decode(ops, input),
                            f2.decoder.decode(ops, input),
                            f3.decoder.decode(ops, input),
                            f4.decoder.decode(ops, input)
                        ).apply(function.decoder.decode(ops, input)));
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
                o -> new MapEncoder<R>() {
                    @Override
                    public <U> RecordBuilder<U> encode(final R input, final DynamicOps<U> ops, final RecordBuilder<U> prefix) {
                        return unbox.encoder.apply(o).encode(getter.apply(o), ops, prefix);
                    }
                },
                unbox.decoder.map(func)
            );
        }
    }
}

