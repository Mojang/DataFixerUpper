// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.util.Pair;

public interface Decoder<A> {
    <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input);

    // TODO: rename to read after Type.read is no more
    default <T> DataResult<A> parse(final DynamicOps<T> ops, final T input) {
        return decode(ops, input).map(Pair::getFirst);
    }

    default  <T> DataResult<Pair<A, T>> decode(final Dynamic<T> input) {
        return decode(input.getOps(), input.getValue());
    }

    default <T> DataResult<A> parse(final Dynamic<T> input) {
        return decode(input).map(Pair::getFirst);
    }

    default Terminal<A> terminal() {
        return this::parse;
    }

    default Boxed<A> boxed() {
        return this::decode;
    }

    default Simple<A> simple() {
        return this::parse;
    }

    interface Terminal<A> {
        <T> DataResult<A> decode(final DynamicOps<T> ops, final T input);

        default Decoder<A> decoder() {
            return new Decoder<A>() {
                @Override
                public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                    return Terminal.this.decode(ops, input).map(a -> Pair.of(a, ops.empty()));
                }

                @Override
                public String toString() {
                    return "TerminalDecoder[" + Terminal.this + "]";
                }
            };
        }
    }

    interface Boxed<A> {
        <T> DataResult<Pair<A, T>> decode(final Dynamic<T> input);

        default Decoder<A> decoder() {
            return new Decoder<A>() {
                @Override
                public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                    return Boxed.this.decode(new Dynamic<>(ops, input));
                }

                @Override
                public String toString() {
                    return "BoxedDecoder[" + Boxed.this + "]";
                }
            };
        }
    }

    interface Simple<A> {
        <T> DataResult<A> decode(final Dynamic<T> input);

        default Decoder<A> decoder() {
            return new Decoder<A>() {
                @Override
                public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                    return Simple.this.decode(new Dynamic<>(ops, input)).map(a -> Pair.of(a, ops.empty()));
                }

                @Override
                public String toString() {
                    return "SimpleDecoder[" + Simple.this + "]";
                }
            };
        }
    }
}
