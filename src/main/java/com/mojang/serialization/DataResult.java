// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Function3;

import java.util.Objects;
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
public class DataResult<R> implements App<DataResult.Mu, R> {
    public static final class Mu implements K1 {}

    public static <R> DataResult<R> unbox(final App<Mu, R> box) {
        return (DataResult<R>) box;
    }

    private final Either<R, PartialResult<R>> result;
    private final Lifecycle lifecycle;

    public static <R> DataResult<R> success(final R result) {
        return success(result, Lifecycle.experimental());
    }

    public static <R> DataResult<R> error(final Supplier<String> message, final R partialResult) {
        return error(message, partialResult, Lifecycle.experimental());
    }

    public static <R> DataResult<R> error(final Supplier<String> message) {
        return error(message, Lifecycle.experimental());
    }

    public static <R> DataResult<R> success(final R result, final Lifecycle experimental) {
        return new DataResult<>(Either.left(result), experimental);
    }

    public static <R> DataResult<R> error(final Supplier<String> message, final R partialResult, final Lifecycle lifecycle) {
        return new DataResult<>(Either.right(new PartialResult<>(message, Optional.of(partialResult))), lifecycle);
    }

    public static <R> DataResult<R> error(final Supplier<String> message, final Lifecycle lifecycle) {
        return new DataResult<>(Either.right(new PartialResult<>(message, Optional.empty())), lifecycle);
    }

    public static <K, V> Function<K, DataResult<V>> partialGet(final Function<K, V> partialGet, final Supplier<String> errorPrefix) {
        return name -> Optional.ofNullable(partialGet.apply(name)).map(DataResult::success).orElseGet(() -> error(() -> errorPrefix.get() + name));
    }

    private static <R> DataResult<R> create(final Either<R, PartialResult<R>> result, final Lifecycle lifecycle) {
        return new DataResult<>(result, lifecycle);
    }

    private DataResult(final Either<R, PartialResult<R>> result, final Lifecycle lifecycle) {
        this.result = result;
        this.lifecycle = lifecycle;
    }

    public Either<R, PartialResult<R>> get() {
        return result;
    }

    public Optional<R> result() {
        return result.left();
    }

    public Lifecycle lifecycle() {
        return lifecycle;
    }

    public Optional<R> resultOrPartial(final Consumer<String> onError) {
        return result.map(
            Optional::of,
            r -> {
                onError.accept(r.message.get());
                return r.partialResult;
            }
        );
    }

    public R getOrThrow(final boolean allowPartial, final Consumer<String> onError) {
        return result.map(
            l -> l,
            r -> {
                final String message = r.message.get();
                onError.accept(message);
                if (allowPartial && r.partialResult.isPresent()) {
                    return r.partialResult.get();
                }
                throw new RuntimeException(message);
            }
        );
    }

    public Optional<PartialResult<R>> error() {
        return result.right();
    }

    public <T> DataResult<T> map(final Function<? super R, ? extends T> function) {
        return create(result.mapBoth(
            function,
            r -> new PartialResult<>(r.message, r.partialResult.map(function))
        ), lifecycle);
    }

    public DataResult<R> promotePartial(final Consumer<String> onError) {
        return result.map(
            r -> new DataResult<>(Either.left(r), lifecycle),
            r -> {
                onError.accept(r.message.get());
                return r.partialResult
                    .map(pr -> new DataResult<>(Either.left(pr), lifecycle))
                    .orElseGet(() -> create(Either.right(r), lifecycle));
            }
        );
    }

    private static String appendMessages(final String first, final String second) {
        return first + "; " + second;
    }

    /**
     * Applies the function to either full or partial result, in case of partial concatenates errors.
     */
    public <R2> DataResult<R2> flatMap(final Function<? super R, ? extends DataResult<R2>> function) {
        return result.map(
            l -> {
                final DataResult<R2> second = function.apply(l);
                return create(second.get(), lifecycle.add(second.lifecycle));
            },
            r -> r.partialResult
                .map(value -> {
                    final DataResult<R2> second = function.apply(value);
                    return create(Either.right(second.get().map(
                        l2 -> new PartialResult<>(r.message, Optional.of(l2)),
                        r2 -> new PartialResult<>(() -> appendMessages(r.message.get(), r2.message.get()), r2.partialResult)
                    )), lifecycle.add(second.lifecycle));
                })
                .orElseGet(
                    () -> create(Either.right(new PartialResult<>(r.message, Optional.empty())), lifecycle)
                )
        );
    }

    public <R2> DataResult<R2> ap(final DataResult<Function<R, R2>> functionResult) {
        return create(result.map(
            arg -> functionResult.result.mapBoth(
                func -> func.apply(arg),
                funcError -> new PartialResult<>(funcError.message, funcError.partialResult.map(f -> f.apply(arg)))
            ),
            argError -> Either.right(functionResult.result.map(
                func -> new PartialResult<>(argError.message, argError.partialResult.map(func)),
                funcError -> new PartialResult<>(
                    () -> appendMessages(argError.message.get(), funcError.message.get()),
                    argError.partialResult.flatMap(a -> funcError.partialResult.map(f -> f.apply(a)))
                )
            ))
        ), lifecycle.add(functionResult.lifecycle));
    }

    public <R2, S> DataResult<S> apply2(final BiFunction<R, R2, S> function, final DataResult<R2> second) {
        return unbox(instance().apply2(function, this, second));
    }

    public <R2, S> DataResult<S> apply2stable(final BiFunction<R, R2, S> function, final DataResult<R2> second) {
        final Applicative<Mu, Instance.Mu> instance = instance();
        final DataResult<BiFunction<R, R2, S>> f = unbox(instance.point(function)).setLifecycle(Lifecycle.stable());
        return unbox(instance.ap2(f, this, second));
    }

    public <R2, R3, S> DataResult<S> apply3(final Function3<R, R2, R3, S> function, final DataResult<R2> second, final DataResult<R3> third) {
        return unbox(instance().apply3(function, this, second, third));
    }

    public DataResult<R> setPartial(final Supplier<R> partial) {
        return create(result.mapRight(r -> new PartialResult<>(r.message, Optional.of(partial.get()))), lifecycle);
    }

    public DataResult<R> setPartial(final R partial) {
        return create(result.mapRight(r -> new PartialResult<>(r.message, Optional.of(partial))), lifecycle);
    }

    public DataResult<R> mapError(final UnaryOperator<String> function) {
        return create(result.mapRight(r -> new PartialResult<>(() -> function.apply(r.message.get()), r.partialResult)), lifecycle);
    }

    public DataResult<R> setLifecycle(final Lifecycle lifecycle) {
        return create(result, lifecycle);
    }

    public DataResult<R> addLifecycle(final Lifecycle lifecycle) {
        return create(result, this.lifecycle.add(lifecycle));
    }

    public static Instance instance() {
        return Instance.INSTANCE;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DataResult<?> that = (DataResult<?>) o;
        return Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return result.hashCode();
    }

    @Override
    public String toString() {
        return "DataResult[" + result + ']';
    }

    public static class PartialResult<R> {
        private final Supplier<String> message;
        private final Optional<R> partialResult;

        public PartialResult(final Supplier<String> message, final Optional<R> partialResult) {
            this.message = message;
            this.partialResult = partialResult;
        }

        public <R2> PartialResult<R2> map(final Function<? super R, ? extends R2> function) {
            return new PartialResult<>(message, partialResult.map(function));
        }

        public <R2> PartialResult<R2> flatMap(final Function<R, PartialResult<R2>> function) {
            if (partialResult.isPresent()) {
                final PartialResult<R2> result = function.apply(partialResult.get());
                return new PartialResult<>(() -> appendMessages(message.get(), result.message.get()), result.partialResult);
            }
            return new PartialResult<>(message, Optional.empty());
        }

        public String message() {
            return message.get();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final PartialResult<?> that = (PartialResult<?>) o;
            return Objects.equals(message, that.message) && Objects.equals(partialResult, that.partialResult);
        }

        @Override
        public int hashCode() {
            int result = message.hashCode();
            result = 31 * result + partialResult.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "DynamicException[" + message() + ' ' + partialResult + ']';
        }
    }

    public enum Instance implements Applicative<Mu, Instance.Mu> {
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
            if (fr.result.left().isPresent()
                && ra.result.left().isPresent()
                && rb.result.left().isPresent()
            ) {
                return new DataResult<>(Either.left(fr.result.left().get().apply(
                    ra.result.left().get(),
                    rb.result.left().get()
                )), fr.lifecycle.add(ra.lifecycle).add(rb.lifecycle));
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
            if (fr.result.left().isPresent()
                && dr1.result.left().isPresent()
                && dr2.result.left().isPresent()
                && dr3.result.left().isPresent()
            ) {
                return new DataResult<>(Either.left(fr.result.left().get().apply(
                    dr1.result.left().get(),
                    dr2.result.left().get(),
                    dr3.result.left().get()
                )), fr.lifecycle.add(dr1.lifecycle).add(dr2.lifecycle).add(dr3.lifecycle));
            }

            return Applicative.super.ap3(func, t1, t2, t3);
        }
    }
}
