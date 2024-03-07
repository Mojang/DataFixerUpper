// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class MapCodec<A> extends CompressorHolder implements MapDecoder<A>, MapEncoder<A> {
    /**
     * Transforms the given {@link Codec} into a {@link MapCodec} by assuming that the result of all elements is a map.
     * <p>
     * This {@link MapCodec} will fail to encode or decode as long as the given {@link Codec} does not return or receive
     * a map.
     */
    public static <A> MapCodec<A> assumeMapUnsafe(final Codec<A> codec) {
        return new MapCodec<>() {
            private static final String COMPRESSED_VALUE_KEY = "value";

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return Stream.of(ops.createString(COMPRESSED_VALUE_KEY));
            }

            @Override
            public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                if (ops.compressMaps()) {
                    final T value = input.get(COMPRESSED_VALUE_KEY);
                    if (value == null) {
                        return DataResult.error(() -> "Missing value");
                    }
                    return codec.parse(ops, value);
                }
                return codec.parse(ops, ops.createMap(input.entries()));
            }

            @Override
            public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                final DataResult<T> encoded = codec.encodeStart(ops, input);
                if (ops.compressMaps()) {
                    return prefix.add(COMPRESSED_VALUE_KEY, encoded);
                }
                final DataResult<MapLike<T>> encodedMapResult = encoded.flatMap(ops::getMap);
                return encodedMapResult.map(encodedMap -> {
                    encodedMap.entries().forEach(pair -> prefix.add(pair.getFirst(), pair.getSecond()));
                    return prefix;
                }).result().orElseGet(() -> prefix.withErrorsFrom(encodedMapResult));
            }
        };
    }

    public final <O> RecordCodecBuilder<O, A> forGetter(final Function<O, A> getter) {
        return RecordCodecBuilder.of(getter, this);
    }

    public static <A> MapCodec<A> of(final MapEncoder<A> encoder, final MapDecoder<A> decoder) {
        return of(encoder, decoder, () -> "MapCodec[" + encoder + " " + decoder + "]");
    }

    public static <A> MapCodec<A> of(final MapEncoder<A> encoder, final MapDecoder<A> decoder, final Supplier<String> name) {
        return new MapCodec<A>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return Stream.concat(encoder.keys(ops), decoder.keys(ops));
            }

            @Override
            public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return decoder.decode(ops, input);
            }

            @Override
            public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                return encoder.encode(input, ops, prefix);
            }

            @Override
            public String toString() {
                return name.get();
            }
        };
    }

    public static <A> MapCodec<A> recursive(final String name, final Function<Codec<A>, MapCodec<A>> wrapped) {
        return new RecursiveMapCodec<>(name, wrapped);
    }

    private static class RecursiveMapCodec<A> extends MapCodec<A> {
        private final String name;
        private final Supplier<MapCodec<A>> wrapped;

        private RecursiveMapCodec(final String name, final Function<Codec<A>, MapCodec<A>> wrapped) {
            this.name = name;
            this.wrapped = Suppliers.memoize(() -> wrapped.apply(codec()));
        }

        @Override
        public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
            return wrapped.get().encode(input, ops, prefix);
        }

        @Override
        public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
            return wrapped.get().decode(ops, input);
        }

        @Override
        public <T> Stream<T> keys(final DynamicOps<T> ops) {
            return wrapped.get().keys(ops);
        }

        @Override
        public String toString() {
            return "RecursiveMapCodec[" + name + ']';
        }
    }

    public MapCodec<A> fieldOf(final String name) {
        return codec().fieldOf(name);
    }

    @Override
    public MapCodec<A> withLifecycle(final Lifecycle lifecycle) {
        return new MapCodec<A>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapCodec.this.keys(ops);
            }

            @Override
            public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return MapCodec.this.decode(ops, input).setLifecycle(lifecycle);
            }

            @Override
            public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                return MapCodec.this.encode(input, ops, prefix).setLifecycle(lifecycle);
            }

            @Override
            public String toString() {
                return MapCodec.this.toString();
            }
        };
    }

    public record MapCodecCodec<A>(MapCodec<A> codec) implements Codec<A> {
        @Override
        public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
            return codec.compressedDecode(ops, input).map(r -> Pair.of(r, input));
        }

        @Override
        public <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
            return codec.encode(input, ops, codec.compressedBuilder(ops)).build(prefix);
        }

        @Override
        public String toString() {
            return codec.toString();
        }
    }

    public Codec<A> codec() {
        return new MapCodecCodec<>(this);
    }

    public MapCodec<A> stable() {
        return withLifecycle(Lifecycle.stable());
    }

    public MapCodec<A> deprecated(final int since) {
        return withLifecycle(Lifecycle.deprecated(since));
    }

    public <S> MapCodec<S> xmap(final Function<? super A, ? extends S> to, final Function<? super S, ? extends A> from) {
        return MapCodec.of(comap(from), map(to), () -> toString() + "[xmapped]");
    }

    public <S> MapCodec<S> flatXmap(final Function<? super A, ? extends DataResult<? extends S>> to, final Function<? super S, ? extends DataResult<? extends A>> from) {
        return Codec.of(flatComap(from), flatMap(to), () -> toString() + "[flatXmapped]");
    }

    public MapCodec<A> validate(final Function<A, DataResult<A>> checker) {
        return flatXmap(checker, checker);
    }

    public <E> MapCodec<A> dependent(final MapCodec<E> initialInstance, final Function<A, Pair<E, MapCodec<E>>> splitter, final BiFunction<A, E, A> combiner) {
        return new Dependent<>(this, initialInstance, splitter, combiner);
    }

    private static class Dependent<O, E> extends MapCodec<O> {
        private final MapCodec<E> initialInstance;
        private final Function<O, Pair<E, MapCodec<E>>> splitter;
        private final MapCodec<O> codec;
        private final BiFunction<O, E, O> combiner;

        public Dependent(final MapCodec<O> codec, final MapCodec<E> initialInstance, final Function<O, Pair<E, MapCodec<E>>> splitter, final BiFunction<O, E, O> combiner) {
            this.initialInstance = initialInstance;
            this.splitter = splitter;
            this.codec = codec;
            this.combiner = combiner;
        }

        @Override
        public <T> Stream<T> keys(final DynamicOps<T> ops) {
            return Stream.concat(codec.keys(ops), initialInstance.keys(ops));
        }

        @Override
        public <T> DataResult<O> decode(final DynamicOps<T> ops, final MapLike<T> input) {
            return codec.decode(ops, input).flatMap((O base) ->
                splitter.apply(base).getSecond().decode(ops, input).map(e -> combiner.apply(base, e)).setLifecycle(Lifecycle.experimental())
            );
        }

        @Override
        public <T> RecordBuilder<T> encode(final O input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
            codec.encode(input, ops, prefix);
            final Pair<E, MapCodec<E>> e = splitter.apply(input);
            e.getSecond().encode(e.getFirst(), ops, prefix);
            return prefix.setLifecycle(Lifecycle.experimental());
        }
    }

    @Override
    public abstract <T> Stream<T> keys(final DynamicOps<T> ops);

    public interface ResultFunction<A> {
        <T> DataResult<A> apply(final DynamicOps<T> ops, final MapLike<T> input, final DataResult<A> a);

        <T> RecordBuilder<T> coApply(final DynamicOps<T> ops, final A input, final RecordBuilder<T> t);
    }

    public MapCodec<A> mapResult(final ResultFunction<A> function) {
        return new MapCodec<A>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapCodec.this.keys(ops);
            }

            @Override
            public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                return function.coApply(ops, input, MapCodec.this.encode(input, ops, prefix));
            }

            @Override
            public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return function.apply(ops, input, MapCodec.this.decode(ops, input));
            }

            @Override
            public String toString() {
                return MapCodec.this + "[mapResult " + function + "]";
            }
        };
    }

    public MapCodec<A> orElse(final Consumer<String> onError, final A value) {
        return orElse(DataFixUtils.consumerToFunction(onError), value);
    }

    public MapCodec<A> orElse(final UnaryOperator<String> onError, final A value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<A> apply(final DynamicOps<T> ops, final MapLike<T> input, final DataResult<A> a) {
                return DataResult.success(a.mapError(onError).result().orElse(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(final DynamicOps<T> ops, final A input, final RecordBuilder<T> t) {
                return t.mapError(onError);
            }

            @Override
            public String toString() {
                return "OrElse[" + onError + " " + value + "]";
            }
        });
    }

    public MapCodec<A> orElseGet(final Consumer<String> onError, final Supplier<? extends A> value) {
        return orElseGet(DataFixUtils.consumerToFunction(onError), value);
    }

    public MapCodec<A> orElseGet(final UnaryOperator<String> onError, final Supplier<? extends A> value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<A> apply(final DynamicOps<T> ops, final MapLike<T> input, final DataResult<A> a) {
                return DataResult.success(a.mapError(onError).result().orElseGet(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(final DynamicOps<T> ops, final A input, final RecordBuilder<T> t) {
                return t.mapError(onError);
            }

            @Override
            public String toString() {
                return "OrElseGet[" + onError + " " + value.get() + "]";
            }
        });
    }

    public MapCodec<A> orElse(final A value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<A> apply(final DynamicOps<T> ops, final MapLike<T> input, final DataResult<A> a) {
                return DataResult.success(a.result().orElse(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(final DynamicOps<T> ops, final A input, final RecordBuilder<T> t) {
                return t;
            }

            @Override
            public String toString() {
                return "OrElse[" + value + "]";
            }
        });
    }

    public MapCodec<A> orElseGet(final Supplier<? extends A> value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<A> apply(final DynamicOps<T> ops, final MapLike<T> input, final DataResult<A> a) {
                return DataResult.success(a.result().orElseGet(value));
            }

            @Override
            public <T> RecordBuilder<T> coApply(final DynamicOps<T> ops, final A input, final RecordBuilder<T> t) {
                return t;
            }

            @Override
            public String toString() {
                return "OrElseGet[" + value.get() + "]";
            }
        });
    }

    public MapCodec<A> setPartial(final Supplier<A> value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<A> apply(final DynamicOps<T> ops, final MapLike<T> input, final DataResult<A> a) {
                return a.setPartial(value);
            }

            @Override
            public <T> RecordBuilder<T> coApply(final DynamicOps<T> ops, final A input, final RecordBuilder<T> t) {
                return t;
            }

            @Override
            public String toString() {
                // FIXME: toString needs to be lazy everywhere, otherwise suppliers get resolved too early
                return "SetPartial[" + value + "]";
            }
        });
    }

    public static <A> MapCodec<A> unit(final A defaultValue) {
        return unit(() -> defaultValue);
    }

    public static <A> MapCodec<A> unit(final Supplier<A> defaultValue) {
        return MapCodec.of(Encoder.empty(), Decoder.unit(defaultValue));
    }
}
