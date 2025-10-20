// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.codecs.CompoundListCodec;
import com.mojang.serialization.codecs.DispatchedMapCodec;
import com.mojang.serialization.codecs.EitherCodec;
import com.mojang.serialization.codecs.EitherMapCodec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.OptionalFieldCodec;
import com.mojang.serialization.codecs.PairCodec;
import com.mojang.serialization.codecs.PairMapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.SimpleMapCodec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import com.mojang.serialization.codecs.XorCodec;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface Codec<A> extends Encoder<A>, Decoder<A> {
    @Override
    default Codec<A> withLifecycle(final Lifecycle lifecycle) {
        return new Codec<A>() {
            @Override
            public <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
                return Codec.this.encode(input, ops, prefix).setLifecycle(lifecycle);
            }

            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return Codec.this.decode(ops, input).setLifecycle(lifecycle);
            }

            @Override
            public String toString() {
                return Codec.this.toString();
            }
        };
    }

    default Codec<A> stable() {
        return withLifecycle(Lifecycle.stable());
    }

    default Codec<A> deprecated(final int since) {
        return withLifecycle(Lifecycle.deprecated(since));
    }

    static <A> Codec<A> of(final Encoder<A> encoder, final Decoder<A> decoder) {
        return of(encoder, decoder, "Codec[" + encoder + " " + decoder + "]");
    }

    static <A> Codec<A> of(final Encoder<A> encoder, final Decoder<A> decoder, final String name) {
        return new Codec<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return decoder.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
                return encoder.encode(input, ops, prefix);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    static <A> MapCodec<A> of(final MapEncoder<A> encoder, final MapDecoder<A> decoder) {
        return of(encoder, decoder, () -> "MapCodec[" + encoder + " " + decoder + "]");
    }

    static <A> MapCodec<A> of(final MapEncoder<A> encoder, final MapDecoder<A> decoder, final Supplier<String> name) {
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

    static <F, S> Codec<Pair<F, S>> pair(final Codec<F> first, final Codec<S> second) {
        return new PairCodec<>(first, second);
    }

    static <F, S> Codec<Either<F, S>> either(final Codec<F> first, final Codec<S> second) {
        return new EitherCodec<>(first, second);
    }

    static <F, S> Codec<Either<F, S>> xor(final Codec<F> first, final Codec<S> second) {
        return new XorCodec<>(first, second);
    }

    static <T> Codec<T> withAlternative(final Codec<T> primary, final Codec<? extends T> alternative) {
        return primary.withAlternative(alternative);
    }

    default Codec<A> withAlternative(final Codec<? extends A> alternative) {
        return Codec.either(
            this,
            alternative
        ).xmap(
            Either::unwrap,
            Either::left
        );
    }

    static <T, U> Codec<T> withAlternative(final Codec<T> primary, final Codec<U> alternative, final Function<U, T> converter) {
        return primary.withAlternative(alternative, converter);
    }

    default <U> Codec<A> withAlternative(final Codec<U> alternative, final Function<U, A> converter) {
        return Codec.either(
            this,
            alternative
        ).xmap(
            either -> either.map(v -> v, converter),
            Either::left
        );
    }

    static <F, S> MapCodec<Pair<F, S>> mapPair(final MapCodec<F> first, final MapCodec<S> second) {
        return new PairMapCodec<>(first, second);
    }

    static <F, S> MapCodec<Either<F, S>> mapEither(final MapCodec<F> first, final MapCodec<S> second) {
        return new EitherMapCodec<>(first, second);
    }

    static <E> Codec<List<E>> list(final Codec<E> elementCodec) {
        return list(elementCodec, 0, Integer.MAX_VALUE);
    }

    static <E> Codec<List<E>> list(final Codec<E> elementCodec, final int minSize, final int maxSize) {
        return new ListCodec<>(elementCodec, minSize, maxSize);
    }

    static <K, V> Codec<List<Pair<K, V>>> compoundList(final Codec<K> keyCodec, final Codec<V> elementCodec) {
        return new CompoundListCodec<>(keyCodec, elementCodec);
    }

    static <K, V> SimpleMapCodec<K, V> simpleMap(final Codec<K> keyCodec, final Codec<V> elementCodec, final Keyable keys) {
        return new SimpleMapCodec<>(keyCodec, elementCodec, keys);
    }

    static <K, V> UnboundedMapCodec<K, V> unboundedMap(final Codec<K> keyCodec, final Codec<V> elementCodec) {
        return new UnboundedMapCodec<>(keyCodec, elementCodec);
    }

    static <K, V> Codec<Map<K, V>> dispatchedMap(final Codec<K> keyCodec, final Function<K, Codec<? extends V>> valueCodecFunction) {
        return new DispatchedMapCodec<>(keyCodec, valueCodecFunction);
    }

    static <E> Codec<E> stringResolver(final Function<E, String> toString, final Function<String, E> fromString) {
        return Codec.STRING.flatXmap(
            name -> Optional.ofNullable(fromString.apply(name)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown element name:" + name)),
            e -> Optional.ofNullable(toString.apply(e)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Element with unknown name: " + e))
        );
    }

    static <F> MapCodec<Optional<F>> optionalField(final String name, final Codec<F> elementCodec, final boolean lenient) {
        return new OptionalFieldCodec<>(name, elementCodec, lenient);
    }

    static <A> Codec<A> recursive(final String name, final Function<Codec<A>, Codec<A>> wrapped) {
        return new RecursiveCodec<>(name, wrapped);
    }

    static <A> Codec<A> lazyInitialized(final Supplier<Codec<A>> delegate) {
        return new RecursiveCodec<>(delegate.toString(), self -> delegate.get());
    }

    class RecursiveCodec<T> implements Codec<T> {
        private final String name;
        private final Supplier<Codec<T>> wrapped;

        private RecursiveCodec(final String name, final Function<Codec<T>, Codec<T>> wrapped) {
            this.name = name;
            this.wrapped = Suppliers.memoize(() -> wrapped.apply(this));
        }

        @Override
        public <S> DataResult<Pair<T, S>> decode(final DynamicOps<S> ops, final S input) {
            return wrapped.get().decode(ops, input);
        }

        @Override
        public <S> DataResult<S> encode(final T input, final DynamicOps<S> ops, final S prefix) {
            return wrapped.get().encode(input, ops, prefix);
        }

        @Override
        public String toString() {
            return "RecursiveCodec[" + name + ']';
        }
    }

    default Codec<List<A>> listOf() {
        return list(this);
    }

    default Codec<List<A>> listOf(final int minSize, final int maxSize) {
        return list(this, minSize, maxSize);
    }

    default Codec<List<A>> sizeLimitedListOf(final int maxSize) {
        return listOf(0, maxSize);
    }

    default <S> Codec<S> xmap(final Function<? super A, ? extends S> to, final Function<? super S, ? extends A> from) {
        return Codec.of(comap(from), map(to), toString() + "[xmapped]");
    }

    default <S> Codec<S> comapFlatMap(final Function<? super A, ? extends DataResult<? extends S>> to, final Function<? super S, ? extends A> from) {
        return Codec.of(comap(from), flatMap(to), toString() + "[comapFlatMapped]");
    }

    default <S> Codec<S> flatComapMap(final Function<? super A, ? extends S> to, final Function<? super S, ? extends DataResult<? extends A>> from) {
        return Codec.of(flatComap(from), map(to), toString() + "[flatComapMapped]");
    }

    default <S> Codec<S> flatXmap(final Function<? super A, ? extends DataResult<? extends S>> to, final Function<? super S, ? extends DataResult<? extends A>> from) {
        return Codec.of(flatComap(from), flatMap(to), toString() + "[flatXmapped]");
    }

    @Override
    default MapCodec<A> fieldOf(final String name) {
        return MapCodec.of(
            Encoder.super.fieldOf(name),
            Decoder.super.fieldOf(name),
            () -> "Field[" + name + ": " + toString() + "]"
        );
    }

    default MapCodec<Optional<A>> optionalFieldOf(final String name) {
        return optionalField(name, this, false);
    }

    default MapCodec<A> optionalFieldOf(final String name, final A defaultValue) {
        return optionalFieldOf(name, defaultValue, false);
    }

    default MapCodec<A> optionalFieldOf(final String name, final A defaultValue, final Lifecycle lifecycleOfDefault) {
        return optionalFieldOf(name, Lifecycle.experimental(), defaultValue, lifecycleOfDefault);
    }

    default MapCodec<A> optionalFieldOf(final String name, final Lifecycle fieldLifecycle, final A defaultValue, final Lifecycle lifecycleOfDefault) {
        // setting lifecycle to stable on the outside since it will be overriden by the passed parameters
        return optionalFieldOf(name, fieldLifecycle, defaultValue, lifecycleOfDefault, false);
    }

    default MapCodec<Optional<A>> lenientOptionalFieldOf(final String name) {
        return optionalField(name, this, true);
    }

    default MapCodec<A> lenientOptionalFieldOf(final String name, final A defaultValue) {
        return optionalFieldOf(name, defaultValue, true);
    }

    default MapCodec<A> lenientOptionalFieldOf(final String name, final A defaultValue, final Lifecycle lifecycleOfDefault) {
        return lenientOptionalFieldOf(name, Lifecycle.experimental(), defaultValue, lifecycleOfDefault);
    }

    default MapCodec<A> lenientOptionalFieldOf(final String name, final Lifecycle fieldLifecycle, final A defaultValue, final Lifecycle lifecycleOfDefault) {
        return optionalFieldOf(name, fieldLifecycle, defaultValue, lifecycleOfDefault, true);
    }

    private MapCodec<A> optionalFieldOf(final String name, final A defaultValue, final boolean lenient) {
        return optionalField(name, this, lenient).xmap(
            o -> o.orElse(defaultValue),
            a -> Objects.equals(a, defaultValue) ? Optional.empty() : Optional.of(a)
        );
    }

    private MapCodec<A> optionalFieldOf(final String name, final Lifecycle fieldLifecycle, final A defaultValue, final Lifecycle lifecycleOfDefault, final boolean lenient) {
        // setting lifecycle to stable on the outside since it will be overriden by the passed parameters
        return optionalField(name, this, lenient).stable().flatXmap(
            o -> o.map(v -> DataResult.success(v, fieldLifecycle)).orElse(DataResult.success(defaultValue, lifecycleOfDefault)),
            a -> Objects.equals(a, defaultValue) ? DataResult.success(Optional.empty(), lifecycleOfDefault) : DataResult.success(Optional.of(a), fieldLifecycle)
        );
    }

    interface ResultFunction<A> {
        <T> DataResult<Pair<A, T>> apply(final DynamicOps<T> ops, final T input, final DataResult<Pair<A, T>> a);

        <T> DataResult<T> coApply(final DynamicOps<T> ops, final A input, final DataResult<T> t);
    }

    default Codec<A> mapResult(final ResultFunction<A> function) {
        return new Codec<A>() {
            @Override
            public <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
                return function.coApply(ops, input, Codec.this.encode(input, ops, prefix));
            }

            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return function.apply(ops, input, Codec.this.decode(ops, input));
            }

            @Override
            public String toString() {
                return Codec.this + "[mapResult " + function + "]";
            }
        };
    }

    default Codec<A> orElse(final Consumer<String> onError, final A value) {
        return orElse(DataFixUtils.consumerToFunction(onError), value);
    }

    default Codec<A> orElse(final UnaryOperator<String> onError, final A value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> apply(final DynamicOps<T> ops, final T input, final DataResult<Pair<A, T>> a) {
                return DataResult.success(a.mapError(onError).result().orElseGet(() -> Pair.of(value, input)));
            }

            @Override
            public <T> DataResult<T> coApply(final DynamicOps<T> ops, final A input, final DataResult<T> t) {
                return t.mapError(onError);
            }

            @Override
            public String toString() {
                return "OrElse[" + onError + " " + value + "]";
            }
        });
    }

    default Codec<A> orElseGet(final Consumer<String> onError, final Supplier<? extends A> value) {
        return orElseGet(DataFixUtils.consumerToFunction(onError), value);
    }

    default Codec<A> orElseGet(final UnaryOperator<String> onError, final Supplier<? extends A> value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> apply(final DynamicOps<T> ops, final T input, final DataResult<Pair<A, T>> a) {
                return DataResult.success(a.mapError(onError).result().orElseGet(() -> Pair.of(value.get(), input)));
            }

            @Override
            public <T> DataResult<T> coApply(final DynamicOps<T> ops, final A input, final DataResult<T> t) {
                return t.mapError(onError);
            }

            @Override
            public String toString() {
                return "OrElseGet[" + onError + " " + value.get() + "]";
            }
        });
    }

    default Codec<A> orElse(final A value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> apply(final DynamicOps<T> ops, final T input, final DataResult<Pair<A, T>> a) {
                return DataResult.success(a.result().orElseGet(() -> Pair.of(value, input)));
            }

            @Override
            public <T> DataResult<T> coApply(final DynamicOps<T> ops, final A input, final DataResult<T> t) {
                return t;
            }

            @Override
            public String toString() {
                return "OrElse[" + value + "]";
            }
        });
    }

    default Codec<A> orElseGet(final Supplier<? extends A> value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> apply(final DynamicOps<T> ops, final T input, final DataResult<Pair<A, T>> a) {
                return DataResult.success(a.result().orElseGet(() -> Pair.of(value.get(), input)));
            }

            @Override
            public <T> DataResult<T> coApply(final DynamicOps<T> ops, final A input, final DataResult<T> t) {
                return t;
            }

            @Override
            public String toString() {
                return "OrElseGet[" + value.get() + "]";
            }
        });
    }

    @Override
    default Codec<A> promotePartial(final Consumer<String> onError) {
        return Codec.of(this, Decoder.super.promotePartial(onError));
    }

    default <E> Codec<E> dispatch(final Function<? super E, ? extends A> type, final Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return dispatch("type", type, codec);
    }

    default <E> Codec<E> dispatch(final String typeKey, final Function<? super E, ? extends A> type, final Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return fieldOf(typeKey).dispatch(type, codec);
    }

    default <E> Codec<E> dispatchStable(final Function<? super E, ? extends A> type, final Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return fieldOf("type").dispatchStable(type, codec);
    }

    default <E> Codec<E> partialDispatch(final String typeKey, final Function<? super E, ? extends DataResult<? extends A>> type, final Function<? super A, ? extends DataResult<? extends MapCodec<? extends E>>> codec) {
        return fieldOf(typeKey).partialDispatch(type, codec);
    }

    default <E> MapCodec<E> dispatchMap(final Function<? super E, ? extends A> type, final Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return dispatchMap("type", type, codec);
    }

    default <E> MapCodec<E> dispatchMap(final String typeKey, final Function<? super E, ? extends A> type, final Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return fieldOf(typeKey).dispatchMap(type, codec);
    }

    default Codec<A> validate(final Function<A, DataResult<A>> checker) {
        return flatXmap(checker, checker);
    }

    // private
    static <N extends Number & Comparable<N>> Function<N, DataResult<N>> checkRange(final N minInclusive, final N maxInclusive) {
        return value -> {
            if (value.compareTo(minInclusive) >= 0 && value.compareTo(maxInclusive) <= 0) {
                return DataResult.success(value);
            }
            return DataResult.error(() -> "Value " + value + " outside of range [" + minInclusive + ":" + maxInclusive + "]");
        };
    }

    static Codec<Integer> intRange(final int minInclusive, final int maxInclusive) {
        final Function<Integer, DataResult<Integer>> checker = checkRange(minInclusive, maxInclusive);
        return Codec.INT.flatXmap(checker, checker);
    }

    static Codec<Float> floatRange(final float minInclusive, final float maxInclusive) {
        final Function<Float, DataResult<Float>> checker = checkRange(minInclusive, maxInclusive);
        return Codec.FLOAT.flatXmap(checker, checker);
    }

    static Codec<Double> doubleRange(final double minInclusive, final double maxInclusive) {
        final Function<Double, DataResult<Double>> checker = checkRange(minInclusive, maxInclusive);
        return Codec.DOUBLE.flatXmap(checker, checker);
    }

    static Codec<String> string(final int minSize, final int maxSize) {
        return Codec.STRING.validate(value -> {
            final int length = value.length();
            if (length < minSize) {
                return DataResult.error(() -> "String \"" + value + "\" is too short: " + length + ", expected range [" + minSize + "-" + maxSize + "]");
            }
            if (length > maxSize) {
                return DataResult.error(() -> "String \"" + value + "\" is too long: " + length + ", expected range [" + minSize + "-" + maxSize + "]");
            }
            return DataResult.success(value);
        });
    }

    static Codec<String> sizeLimitedString(final int maxSize) {
        return string(0, maxSize);
    }

    PrimitiveCodec<Boolean> BOOL = new PrimitiveCodec<Boolean>() {
        @Override
        public <T> DataResult<Boolean> read(final DynamicOps<T> ops, final T input) {
            return ops
                .getBooleanValue(input);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Boolean value) {
            return ops.createBoolean(value);
        }

        @Override
        public String toString() {
            return "Bool";
        }
    };

    PrimitiveCodec<Byte> BYTE = new PrimitiveCodec<Byte>() {
        @Override
        public <T> DataResult<Byte> read(final DynamicOps<T> ops, final T input) {
            return ops
                .getNumberValue(input)
                .map(Number::byteValue);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Byte value) {
            return ops.createByte(value);
        }

        @Override
        public String toString() {
            return "Byte";
        }
    };

    PrimitiveCodec<Short> SHORT = new PrimitiveCodec<Short>() {
        @Override
        public <T> DataResult<Short> read(final DynamicOps<T> ops, final T input) {
            return ops
                .getNumberValue(input)
                .map(Number::shortValue);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Short value) {
            return ops.createShort(value);
        }

        @Override
        public String toString() {
            return "Short";
        }
    };

    PrimitiveCodec<Integer> INT = new PrimitiveCodec<Integer>() {
        @Override
        public <T> DataResult<Integer> read(final DynamicOps<T> ops, final T input) {
            return ops
                .getNumberValue(input)
                .map(Number::intValue);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Integer value) {
            return ops.createInt(value);
        }

        @Override
        public String toString() {
            return "Int";
        }
    };

    PrimitiveCodec<Long> LONG = new PrimitiveCodec<Long>() {
        @Override
        public <T> DataResult<Long> read(final DynamicOps<T> ops, final T input) {
            return ops
                .getNumberValue(input)
                .map(Number::longValue);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Long value) {
            return ops.createLong(value);
        }

        @Override
        public String toString() {
            return "Long";
        }
    };

    PrimitiveCodec<Float> FLOAT = new PrimitiveCodec<Float>() {
        @Override
        public <T> DataResult<Float> read(final DynamicOps<T> ops, final T input) {
            return ops
                .getNumberValue(input)
                .map(Number::floatValue);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Float value) {
            return ops.createFloat(value);
        }

        @Override
        public String toString() {
            return "Float";
        }
    };

    PrimitiveCodec<Double> DOUBLE = new PrimitiveCodec<Double>() {
        @Override
        public <T> DataResult<Double> read(final DynamicOps<T> ops, final T input) {
            return ops
                .getNumberValue(input)
                .map(Number::doubleValue);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Double value) {
            return ops.createDouble(value);
        }

        @Override
        public String toString() {
            return "Double";
        }
    };

    PrimitiveCodec<String> STRING = new PrimitiveCodec<String>() {
        @Override
        public <T> DataResult<String> read(final DynamicOps<T> ops, final T input) {
            return ops
                .getStringValue(input);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final String value) {
            return ops.createString(value);
        }

        @Override
        public String toString() {
            return "String";
        }
    };

    PrimitiveCodec<ByteBuffer> BYTE_BUFFER = new PrimitiveCodec<ByteBuffer>() {
        @Override
        public <T> DataResult<ByteBuffer> read(final DynamicOps<T> ops, final T input) {
            return ops
                .getByteBuffer(input);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final ByteBuffer value) {
            return ops.createByteList(value);
        }

        @Override
        public String toString() {
            return "ByteBuffer";
        }
    };

    PrimitiveCodec<IntStream> INT_STREAM = new PrimitiveCodec<IntStream>() {
        @Override
        public <T> DataResult<IntStream> read(final DynamicOps<T> ops, final T input) {
            return ops
                .getIntStream(input);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final IntStream value) {
            return ops.createIntList(value);
        }

        @Override
        public String toString() {
            return "IntStream";
        }
    };

    PrimitiveCodec<LongStream> LONG_STREAM = new PrimitiveCodec<LongStream>() {
        @Override
        public <T> DataResult<LongStream> read(final DynamicOps<T> ops, final T input) {
            return ops
                .getLongStream(input);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final LongStream value) {
            return ops.createLongList(value);
        }

        @Override
        public String toString() {
            return "LongStream";
        }
    };

    Codec<Dynamic<?>> PASSTHROUGH = new Codec<Dynamic<?>>() {
        @Override
        public <T> DataResult<Pair<Dynamic<?>, T>> decode(final DynamicOps<T> ops, final T input) {
            return DataResult.success(Pair.of(new Dynamic<>(ops, input), ops.empty()));
        }

        @Override
        public <T> DataResult<T> encode(final Dynamic<?> input, final DynamicOps<T> ops, final T prefix) {
            if (input.getValue() == input.getOps().empty()) {
                // nothing to merge, return rest
                return DataResult.success(prefix, Lifecycle.experimental());
            }

            final T casted = input.convert(ops).getValue();
            if (prefix == ops.empty()) {
                // no need to merge anything, return the old value
                return DataResult.success(casted, Lifecycle.experimental());
            }

            final DataResult<T> toMap = ops.getMap(casted).flatMap(map -> ops.mergeToMap(prefix, map));
            return toMap.result().map(DataResult::success).orElseGet(() -> {
                final DataResult<T> toList = ops.getStream(casted).flatMap(stream -> ops.mergeToList(prefix, stream.collect(Collectors.toList())));
                return toList.result().map(DataResult::success).orElseGet(() ->
                    DataResult.error(() -> "Don't know how to merge " + prefix + " and " + casted, prefix, Lifecycle.experimental())
                );
            });
        }

        @Override
        public String toString() {
            return "passthrough";
        }
    };

    MapCodec<Unit> EMPTY = MapCodec.unit(Unit.INSTANCE);
}
