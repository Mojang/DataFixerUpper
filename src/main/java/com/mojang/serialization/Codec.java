// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.codecs.CompoundListCodec;
import com.mojang.serialization.codecs.EitherCodec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.OptionalFieldCodec;
import com.mojang.serialization.codecs.PairCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.SimpleMapCodec;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public interface Codec<A> extends Encoder<A>, Decoder<A> {
    static <A extends Serializable> Codec<A> of(final Decoder<A> decoder) {
        return of(Encoder.of(), decoder);
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

    static <F, S> Codec<Pair<F, S>> pair(final Codec<F> first, final Codec<S> second) {
        return new PairCodec<>(first, second);
    }

    static <F, S> Codec<Either<F, S>> either(final Codec<F> first, final Codec<S> second) {
        return new EitherCodec<>(first, second);
    }

    static <E> Codec<List<E>> list(final Codec<E> elementCodec) {
        return new ListCodec<>(elementCodec);
    }

    static <K, V> Codec<List<Pair<K, V>>> compoundList(final Codec<K> keyCodec, final Codec<V> elementCodec) {
        return new CompoundListCodec<>(keyCodec, elementCodec);
    }

    static <K, V> SimpleMapCodec<K, V> simpleMap(final Codec<K> keyCodec, final Codec<V> elementCodec, final Keyable keys) {
        return new SimpleMapCodec<>(keyCodec, elementCodec, keys);
    }

    static <F> MapCodec<Optional<F>> optionalField(final String name, final Codec<F> elementCodec) {
        return new OptionalFieldCodec<>(name, elementCodec);
    }

    default Codec<List<A>> listOf() {
        return list(this);
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
            "Field[" + name + ": " + toString() + "]"
        );
    }

    default MapCodec<Optional<A>> optionalFieldOf(final String name) {
        return optionalField(name, this);
    }

    @Override
    default Codec<A> withDefault(final A value) {
        return Codec.of(this, Decoder.super.withDefault(value));
    }

    @Override
    default Codec<A> withDefault(final Supplier<? extends A> value) {
        return Codec.of(this, Decoder.super.withDefault(value));
    }

    static <A> MapCodec<A> unit(final A defaultValue) {
        return unit(() -> defaultValue);
    }

    static <A> MapCodec<A> unit(final Supplier<A> defaultValue) {
        return MapCodec.of(Encoder.empty(), Decoder.unit(defaultValue));
    }

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

    Codec<Dynamic<?>> SAVING = new Codec<Dynamic<?>>() {
        @Override
        public <T> DataResult<Pair<Dynamic<?>, T>> decode(final DynamicOps<T> ops, final T input) {
            return DataResult.success(Pair.of(new Dynamic<>(ops, input), ops.empty()));
        }

        @Override
        public <T> DataResult<T> encode(final Dynamic<?> input, final DynamicOps<T> ops, final T prefix) {
            if (input.getValue() == input.getOps().empty()) {
                // nothing to merge, return rest
                return DataResult.success(prefix);
            }

            final T casted = input.convert(ops).getValue();
            if (prefix == ops.empty()) {
                // no need to merge anything, return the old value
                return DataResult.success(casted);
            }

            final DataResult<T> toMap = ops.getMap(casted).flatMap(map -> ops.mergeToMap(prefix, map));
            return toMap.result().map(DataResult::success).orElseGet(() -> {
                final DataResult<T> toList = ops.getStream(casted).flatMap(stream -> ops.mergeToList(prefix, stream.collect(Collectors.toList())));
                return toList.result().map(DataResult::success).orElseGet(() ->
                    DataResult.error("Don't know how to merge " + prefix + " and " + casted, prefix)
                );
            });
        }
    };

    Codec<Unit> EMPTY = Codec.of(Encoder.empty(), Decoder.unit(Unit.INSTANCE));
}
