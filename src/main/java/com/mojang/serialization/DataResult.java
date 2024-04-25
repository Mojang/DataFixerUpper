// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.util.Function3;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Represents either a successful operation, or a partial operation with an error message and a partial result (if available)
 * Also stores an additional lifecycle marker (monoidal)
 */
public sealed interface DataResult<R> extends App<DataResult.Mu, R> permits DataResult.Success, DataResult.Error {
    final class Mu implements K1 {}

    static <R> DataResult<R> unbox(final App<Mu, R> box) {
        return (DataResult<R>) box;
    }

    static <R> DataResult<R> success(final R result) {
        return success(result, Lifecycle.experimental());
    }

    static <R> DataResult<R> error(final Supplier<String> message, final R partialResult) {
        return error(message, partialResult, Lifecycle.experimental());
    }

    static <R> DataResult<R> error(final Supplier<String> message) {
        return error(message, Lifecycle.experimental());
    }

    static <R> DataResult<R> success(final R result, final Lifecycle lifecycle) {
        return new Success<>(result, lifecycle);
    }

    static <R> DataResult<R> error(final Supplier<String> message, final R partialResult, final Lifecycle lifecycle) {
        return new Error<>(message, Optional.of(partialResult), lifecycle);
    }

    static <R> DataResult<R> error(final Supplier<String> message, final Lifecycle lifecycle) {
        return new Error<>(message, Optional.empty(), lifecycle);
    }

    static <K, V> Function<K, DataResult<V>> partialGet(final Function<K, V> partialGet, final Supplier<String> errorPrefix) {
        return name -> Optional.ofNullable(partialGet.apply(name)).map(DataResult::success).orElseGet(() -> error(() -> errorPrefix.get() + name));
    }

    static Instance instance() {
        return Instance.INSTANCE;
    }

    static String appendMessages(final String first, final String second) {
        return first + "; " + second;
    }

    Optional<R> result();

    Optional<DataResult.Error<R>> error();

    Lifecycle lifecycle();

    boolean hasResultOrPartial();

    Optional<R> resultOrPartial(Consumer<String> onError);

    Optional<R> resultOrPartial();

    <E extends Throwable> R getOrThrow(Function<String, E> exceptionSupplier) throws E;

    <E extends Throwable> R getPartialOrThrow(Function<String, E> exceptionSupplier) throws E;

    default R getOrThrow() {
        return getOrThrow(IllegalStateException::new);
    }

    default R getPartialOrThrow() {
        return getPartialOrThrow(IllegalStateException::new);
    }

    <T> DataResult<T> map(Function<? super R, ? extends T> function);

    <T> T mapOrElse(Function<? super R, ? extends T> successFunction, Function<? super Error<R>, ? extends T> errorFunction);

    DataResult<R> ifSuccess(Consumer<? super R> ifSuccess);

    DataResult<R> ifError(Consumer<? super Error<R>> ifError);

    DataResult<R> promotePartial(Consumer<String> onError);

    /**
     * Applies the function to either full or partial result, in case of partial concatenates errors.
     */
    <R2> DataResult<R2> flatMap(Function<? super R, ? extends DataResult<R2>> function);

    <R2> DataResult<R2> ap(DataResult<Function<R, R2>> functionResult);

    default <R2, S> DataResult<S> apply2(final BiFunction<R, R2, S> function, final DataResult<R2> second) {
        return unbox(instance().apply2(function, this, second));
    }

    default <R2, S> DataResult<S> apply2stable(final BiFunction<R, R2, S> function, final DataResult<R2> second) {
        final Applicative<DataResult.Mu, DataResult.Instance.Mu> instance = instance();
        final DataResult<BiFunction<R, R2, S>> f = unbox(instance.point(function)).setLifecycle(Lifecycle.stable());
        return unbox(instance.ap2(f, this, second));
    }

    default <R2, R3, S> DataResult<S> apply3(final Function3<R, R2, R3, S> function, final DataResult<R2> second, final DataResult<R3> third) {
        return unbox(instance().apply3(function, this, second, third));
    }

    DataResult<R> setPartial(Supplier<R> partial);

    DataResult<R> setPartial(R partial);

    DataResult<R> mapError(UnaryOperator<String> function);

    DataResult<R> setLifecycle(Lifecycle lifecycle);

    default DataResult<R> addLifecycle(final Lifecycle lifecycle) {
        return setLifecycle(lifecycle().add(lifecycle));
    }

    boolean isSuccess();

    default boolean isError() {
        return !isSuccess();
    }

    record Success<R>(R value, Lifecycle lifecycle) implements DataResult<R> {
        @Override
        public Optional<R> result() {
            return Optional.of(value);
        }

        @Override
        public Optional<Error<R>> error() {
            return Optional.empty();
        }

        @Override
        public boolean hasResultOrPartial() {
            return true;
        }

        @Override
        public Optional<R> resultOrPartial(final Consumer<String> onError) {
            return Optional.of(value);
        }

        @Override
        public Optional<R> resultOrPartial() {
            return Optional.of(value);
        }

        @Override
        public <E extends Throwable> R getOrThrow(final Function<String, E> exceptionSupplier) throws E {
            return value;
        }

        @Override
        public <E extends Throwable> R getPartialOrThrow(final Function<String, E> exceptionSupplier) throws E {
            return value;
        }

        @Override
        public <T> DataResult<T> map(final Function<? super R, ? extends T> function) {
            return new Success<>(function.apply(value), lifecycle);
        }

        @Override
        public <T> T mapOrElse(final Function<? super R, ? extends T> successFunction, final Function<? super Error<R>, ? extends T> errorFunction) {
            return successFunction.apply(value);
        }

        @Override
        public DataResult<R> ifSuccess(final Consumer<? super R> ifSuccess) {
            ifSuccess.accept(value);
            return this;
        }

        @Override
        public DataResult<R> ifError(final Consumer<? super Error<R>> ifError) {
            return this;
        }

        @Override
        public DataResult<R> promotePartial(final Consumer<String> onError) {
            return this;
        }

        @Override
        public <R2> DataResult<R2> flatMap(final Function<? super R, ? extends DataResult<R2>> function) {
            return function.apply(value).addLifecycle(lifecycle);
        }

        @Override
        public <R2> DataResult<R2> ap(final DataResult<Function<R, R2>> functionResult) {
            final Lifecycle combinedLifecycle = lifecycle.add(functionResult.lifecycle());
            if (functionResult instanceof final Success<Function<R, R2>> funcSuccess) {
                return new Success<>(funcSuccess.value.apply(value), combinedLifecycle);
            } else if (functionResult instanceof final Error<Function<R, R2>> funcError) {
                return new Error<>(funcError.messageSupplier, funcError.partialValue.map(f -> f.apply(value)), combinedLifecycle);
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public DataResult<R> setPartial(final Supplier<R> partial) {
            return this;
        }

        @Override
        public DataResult<R> setPartial(final R partial) {
            return this;
        }

        @Override
        public DataResult<R> mapError(final UnaryOperator<String> function) {
            return this;
        }

        @Override
        public DataResult<R> setLifecycle(final Lifecycle lifecycle) {
            if (this.lifecycle.equals(lifecycle)) {
                return this;
            }
            return new Success<>(value, lifecycle);
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public String toString() {
            return "DataResult.Success[" + value + "]";
        }
    }

    record Error<R>(
        Supplier<String> messageSupplier,
        Optional<R> partialValue,
        Lifecycle lifecycle
    ) implements DataResult<R> {
        public String message() {
            return messageSupplier.get();
        }

        @Override
        public Optional<R> result() {
            return Optional.empty();
        }

        @Override
        public Optional<Error<R>> error() {
            return Optional.of(this);
        }

        @Override
        public boolean hasResultOrPartial() {
            return partialValue.isPresent();
        }

        @Override
        public Optional<R> resultOrPartial(final Consumer<String> onError) {
            onError.accept(messageSupplier.get());
            return partialValue;
        }

        @Override
        public Optional<R> resultOrPartial() {
            return partialValue;
        }

        @Override
        public <E extends Throwable> R getOrThrow(final Function<String, E> exceptionSupplier) throws E {
            throw exceptionSupplier.apply(message());
        }

        @Override
        public <E extends Throwable> R getPartialOrThrow(final Function<String, E> exceptionSupplier) throws E {
            if (partialValue.isPresent()) {
                return partialValue.get();
            }
            throw exceptionSupplier.apply(message());
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Error<T> map(final Function<? super R, ? extends T> function) {
            if (partialValue.isEmpty()) {
                return (Error<T>) this;
            }
            return new Error<>(messageSupplier, partialValue.map(function), lifecycle);
        }

        @Override
        public <T> T mapOrElse(final Function<? super R, ? extends T> successFunction, final Function<? super Error<R>, ? extends T> errorFunction) {
            return errorFunction.apply(this);
        }

        @Override
        public DataResult<R> ifSuccess(final Consumer<? super R> ifSuccess) {
            return this;
        }

        @Override
        public DataResult<R> ifError(final Consumer<? super Error<R>> ifError) {
            ifError.accept(this);
            return this;
        }

        @Override
        public DataResult<R> promotePartial(final Consumer<String> onError) {
            onError.accept(messageSupplier.get());
            return partialValue.<DataResult<R>>map(value -> new Success<>(value, lifecycle)).orElse(this);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <R2> Error<R2> flatMap(final Function<? super R, ? extends DataResult<R2>> function) {
            if (partialValue.isEmpty()) {
                return (Error<R2>) this;
            }
            final DataResult<R2> second = function.apply(partialValue.get());
            final Lifecycle combinedLifecycle = lifecycle.add(second.lifecycle());
            if (second instanceof final Success<R2> secondSuccess) {
                return new Error<>(messageSupplier, Optional.of(secondSuccess.value), combinedLifecycle);
            } else if (second instanceof final Error<R2> secondError) {
                return new Error<>(() -> appendMessages(messageSupplier.get(), secondError.messageSupplier.get()), secondError.partialValue, combinedLifecycle);
            } else {
                // TODO: Replace with record pattern matching in Java 21
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public <R2> Error<R2> ap(final DataResult<Function<R, R2>> functionResult) {
            final Lifecycle combinedLifecycle = lifecycle.add(functionResult.lifecycle());
            if (functionResult instanceof final Success<Function<R, R2>> func) {
                return new Error<>(messageSupplier, partialValue.map(func.value), combinedLifecycle);
            } else if (functionResult instanceof final Error<Function<R, R2>> funcError) {
                return new Error<>(
                    () -> appendMessages(messageSupplier.get(), funcError.messageSupplier.get()),
                    partialValue.flatMap(a -> funcError.partialValue.map(f -> f.apply(a))),
                    combinedLifecycle
                );
            } else {
                // TODO: Replace with record pattern matching in Java 21
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public Error<R> setPartial(final Supplier<R> partial) {
            return setPartial(partial.get());
        }

        @Override
        public Error<R> setPartial(final R partial) {
            return new Error<>(messageSupplier, Optional.of(partial), lifecycle);
        }

        @Override
        public Error<R> mapError(final UnaryOperator<String> function) {
            return new Error<>(() -> function.apply(messageSupplier.get()), partialValue, lifecycle);
        }

        @Override
        public Error<R> setLifecycle(final Lifecycle lifecycle) {
            if (this.lifecycle.equals(lifecycle)) {
                return this;
            }
            return new Error<>(messageSupplier, partialValue, lifecycle);
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String toString() {
            return "DataResult.Error['" + message() + "'" + partialValue.map(value -> ": " + value).orElse("") + "]";
        }
    }

    enum Instance implements Applicative<Mu, Instance.Mu> {
        INSTANCE;

        public static final class Mu implements Applicative.Mu {}

        @Override
        public <T, R> App<DataResult.Mu, R> map(final Function<? super T, ? extends R> func, final App<DataResult.Mu, T> ts) {
            return unbox(ts).map(func);
        }

        @Override
        public <A> App<DataResult.Mu, A> point(final A a) {
            return success(a);
        }

        @Override
        public <A, R> Function<App<DataResult.Mu, A>, App<DataResult.Mu, R>> lift1(final App<DataResult.Mu, Function<A, R>> function) {
            return fa -> ap(function, fa);
        }

        @Override
        public <A, R> App<DataResult.Mu, R> ap(final App<DataResult.Mu, Function<A, R>> func, final App<DataResult.Mu, A> arg) {
            return unbox(arg).ap(unbox(func));
        }

        @Override
        public <A, B, R> App<DataResult.Mu, R> ap2(final App<DataResult.Mu, BiFunction<A, B, R>> func, final App<DataResult.Mu, A> a, final App<DataResult.Mu, B> b) {
            final DataResult<BiFunction<A, B, R>> fr = unbox(func);
            final DataResult<A> ra = unbox(a);
            final DataResult<B> rb = unbox(b);

            // for less recursion
            if (fr.result().isPresent()
                && ra.result().isPresent()
                && rb.result().isPresent()
            ) {
                return new Success<>(fr.result().get().apply(
                    ra.result().get(),
                    rb.result().get()
                ), fr.lifecycle().add(ra.lifecycle()).add(rb.lifecycle()));
            }

            return Applicative.super.ap2(func, a, b);
        }

        @Override
        public <T1, T2, T3, R> App<DataResult.Mu, R> ap3(final App<DataResult.Mu, Function3<T1, T2, T3, R>> func, final App<DataResult.Mu, T1> t1, final App<DataResult.Mu, T2> t2, final App<DataResult.Mu, T3> t3) {
            final DataResult<Function3<T1, T2, T3, R>> fr = unbox(func);
            final DataResult<T1> dr1 = unbox(t1);
            final DataResult<T2> dr2 = unbox(t2);
            final DataResult<T3> dr3 = unbox(t3);

            // for less recursion
            if (fr.result().isPresent()
                && dr1.result().isPresent()
                && dr2.result().isPresent()
                && dr3.result().isPresent()
            ) {
                return new Success<>(fr.result().get().apply(
                    dr1.result().get(),
                    dr2.result().get(),
                    dr3.result().get()
                ), fr.lifecycle().add(dr1.lifecycle()).add(dr2.lifecycle()).add(dr3.lifecycle()));
            }

            return Applicative.super.ap3(func, t1, t2, t3);
        }
    }
}
