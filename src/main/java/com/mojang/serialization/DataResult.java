// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.mojang.datafixers.util.Either;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class DataResult<R> {
    private final Either<R, DynamicException<R>> result;

    public static <R> DataResult<R> success(final R result) {
        return new DataResult<>(Either.left(result));
    }

    public static <R> DataResult<R> error(final String message, final R partialResult) {
        return new DataResult<>(Either.right(new DynamicException<>(message, Optional.of(partialResult))));
    }

    public static <R> DataResult<R> error(final String message) {
        return new DataResult<>(Either.right(new DynamicException<>(message, Optional.empty())));
    }

    public static <R> DataResult<R> create(final Either<R, DynamicException<R>> result) {
        return new DataResult<>(result);
    }

    private DataResult(final Either<R, DynamicException<R>> result) {
        this.result = result;
    }

    public Either<R, DynamicException<R>> get() {
        return result;
    }

    public Optional<R> result() {
        return result.left();
    }

    public <T> T resultOrPartial(final Function<R, T> onSuccess, final BiFunction<String, Optional<R>, T> onFailure) {
        return result.map(
            onSuccess,
            r -> onFailure.apply(r.message, r.partialResult)
        );
    }

    public Optional<R> resultOrPartial(final Consumer<String> onError) {
        return result.map(
            Optional::of,
            r -> {
                onError.accept(r.message);
                return r.partialResult;
            }
        );
    }

    public Optional<DynamicException<R>> error() {
        return result.right();
    }

    public <T> DataResult<T> map(final Function<? super R, ? extends T> function) {
        return create(result.mapBoth(
            function,
            r -> new DynamicException<>(r.message, r.partialResult.map(function))
        ));
    }

    /**
     * Applies the function to either full or partial result, in case of partial concatenates errors.
     */
    public <R2> DataResult<R2> flatMap(final Function<R, DataResult<R2>> function) {
        return create(result.map(
            l -> function.apply(l).get(),
            r -> Either.right(r.partialResult
                .map(value -> function.apply(value).get().map(
                    l2 -> new DynamicException<>(r.message, Optional.of(l2)),
                    r2 -> new DynamicException<>(r.message + "; " + r2.message, r2.partialResult)
                ))
                .orElseGet(
                    () -> new DynamicException<>(r.message, Optional.empty())
                )
            )
        ));
    }

    public static class DynamicException<R> {
        private final String message;
        private final Optional<R> partialResult;

        public DynamicException(final String message, final Optional<R> partialResult) {
            this.message = message;
            this.partialResult = partialResult;
        }

        public RuntimeException error() {
            return new RuntimeException(message);
        }

        public <R2> DynamicException<R2> map(final Function<? super R, ? extends R2> function) {
            return new DynamicException<>(message, partialResult.map(function));
        }

        public <R2> DynamicException<R2> flatMap(final Function<R, DynamicException<R2>> function) {
            if (partialResult.isPresent()) {
                final DynamicException<R2> result = function.apply(partialResult.get());
                return new DynamicException<>(message + "; " + result.message, result.partialResult);
            }
            return new DynamicException<>(message, Optional.empty());
        }

        public String message() {
            return message;
        }
    }
}