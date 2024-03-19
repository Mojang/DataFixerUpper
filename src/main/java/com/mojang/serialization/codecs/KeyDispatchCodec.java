package com.mojang.serialization.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import java.util.function.Function;
import java.util.stream.Stream;

public class KeyDispatchCodec<K, V> extends MapCodec<V> {
    private static final String COMPRESSED_VALUE_KEY = "value";
    private final String typeKey;
    private final Codec<K> keyCodec;
    private final Function<? super V, ? extends DataResult<? extends K>> type;
    private final Function<? super K, ? extends DataResult<? extends MapDecoder<? extends V>>> decoder;
    private final Function<? super V, ? extends DataResult<? extends MapEncoder<V>>> encoder;

    protected KeyDispatchCodec(final String typeKey, final Codec<K> keyCodec, final Function<? super V, ? extends DataResult<? extends K>> type, final Function<? super K, ? extends DataResult<? extends MapDecoder<? extends V>>> decoder, final Function<? super V, ? extends DataResult<? extends MapEncoder<V>>> encoder) {
        this.typeKey = typeKey;
        this.keyCodec = keyCodec;
        this.type = type;
        this.decoder = decoder;
        this.encoder = encoder;
    }

    /**
     * Assumes codec(type(V)) is MapCodec<V>
     */
    public KeyDispatchCodec(final String typeKey, final Codec<K> keyCodec, final Function<? super V, ? extends DataResult<? extends K>> type, final Function<? super K, ? extends DataResult<? extends MapCodec<? extends V>>> codec) {
        this(typeKey, keyCodec, type, codec, v -> getCodec(type, codec, v));
    }

    @Override
    public <T> DataResult<V> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final T elementName = input.get(typeKey);
        if (elementName == null) {
            return DataResult.error(() -> "Input does not contain a key [" + typeKey + "]: " + input);
        }

        return keyCodec.decode(ops, elementName).flatMap(type ->
            decoder.apply(type.getFirst()).flatMap(elementDecoder -> {
                if (ops.compressMaps()) {
                    final T value = input.get(ops.createString(COMPRESSED_VALUE_KEY));
                    if (value == null) {
                        return DataResult.error(() -> "Input does not have a \"value\" entry: " + input);
                    }
                    return elementDecoder.decoder().parse(ops, value).map(Function.identity());
                }
                return elementDecoder.decode(ops, input).map(Function.identity());
            })
        );
    }

    @Override
    public <T> RecordBuilder<T> encode(final V input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
        final DataResult<? extends MapEncoder<V>> encoderResult = encoder.apply(input);
        final RecordBuilder<T> builder = prefix.withErrorsFrom(encoderResult);
        if (encoderResult.isError()) {
            return builder;
        }

        final MapEncoder<V> elementEncoder = encoderResult.result().get();
        if (ops.compressMaps()) {
            return prefix
                .add(typeKey, type.apply(input).flatMap(t -> keyCodec.encodeStart(ops, t)))
                .add(COMPRESSED_VALUE_KEY, elementEncoder.encoder().encodeStart(ops, input));
        }

        return elementEncoder.encode(input, ops, prefix)
            .add(typeKey, type.apply(input).flatMap(t -> keyCodec.encodeStart(ops, t)));
    }

    @Override
    public <T> Stream<T> keys(final DynamicOps<T> ops) {
        return Stream.of(typeKey, COMPRESSED_VALUE_KEY).map(ops::createString);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> DataResult<? extends MapEncoder<V>> getCodec(final Function<? super V, ? extends DataResult<? extends K>> type, final Function<? super K, ? extends DataResult<? extends MapEncoder<? extends V>>> encoder, final V input) {
        return type.apply(input)
            .<MapEncoder<? extends V>>flatMap(key -> encoder.apply(key).map(Function.identity()))
            .map(c -> ((MapEncoder<V>) c));
    }

    @Override
    public String toString() {
        return "KeyDispatchCodec[" + keyCodec.toString() + " " + type + " " + decoder + "]";
    }
}
