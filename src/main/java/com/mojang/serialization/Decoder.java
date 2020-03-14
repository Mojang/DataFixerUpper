// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.util.Pair;

import java.util.function.Function;
import java.util.function.Supplier;

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

    default <B> Decoder<B> map(final Function<? super A, ? extends B> function) {
        final Decoder<A> self = this;
        return new Decoder<B>() {
            @Override
            public <T> DataResult<Pair<B, T>> decode(final DynamicOps<T> ops, final T input) {
                return self.decode(ops, input).map(p -> p.mapFirst(function));
            }
        };
    }

    static <A> Decoder<A> ofTerminal(final Terminal<? extends A> terminal) {
        return terminal.decoder().map(Function.identity());
    }

    static <A> Decoder<A> ofBoxed(final Boxed<? extends A> boxed) {
        return boxed.decoder().map(Function.identity());
    }

    static <A> Decoder<A> ofSimple(final Simple<? extends A> simple) {
        return simple.decoder().map(Function.identity());
    }

    static <A> Decoder<A> unit(final A instance) {
        return new Decoder<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return DataResult.success(Pair.of(instance, input));
            }

            @Override
            public String toString() {
                return "UnitDecoder[" + instance + "]";
            }
        };
    }

    static <A> Decoder<A> unit(final Supplier<A> instance) {
        return new Decoder<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return DataResult.success(Pair.of(instance.get(), input));
            }

            @Override
            public String toString() {
                return "UnitDecoder[" + instance.get() + "]";
            }
        };
    }

    static <A> Decoder<A> error(final String error) {
        return new Decoder<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return DataResult.error(error);
            }

            @Override
            public String toString() {
                return "ErrorDecoder[" + error + ']';
            }
        };
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
