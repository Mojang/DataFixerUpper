// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.google.common.collect.ImmutableList;

import java.util.function.UnaryOperator;

public interface ListBuilder<T> {
    DynamicOps<T> ops();

    DataResult<T> build(T prefix);

    ListBuilder<T> add(final T value);

    ListBuilder<T> add(final DataResult<T> value);

    ListBuilder<T> withErrorsFrom(final DataResult<?> result);

    ListBuilder<T> mapError(UnaryOperator<String> onError);

    default DataResult<T> build(final DataResult<T> prefix) {
        return prefix.flatMap(this::build);
    }

    default <E> ListBuilder<T> add(final E value, final Encoder<E> encoder) {
        return add(encoder.encodeStart(ops(), value));
    }

    default <E> ListBuilder<T> addAll(final Iterable<E> values, final Encoder<E> encoder) {
        values.forEach(v -> encoder.encode(v, ops(), ops().empty()));
        return this;
    }

    final class Builder<T> implements ListBuilder<T> {
        private final DynamicOps<T> ops;
        private DataResult<ImmutableList.Builder<T>> builder = DataResult.success(ImmutableList.builder(), Lifecycle.stable());

        public Builder(final DynamicOps<T> ops) {
            this.ops = ops;
        }

        @Override
        public DynamicOps<T> ops() {
            return ops;
        }

        @Override
        public ListBuilder<T> add(final T value) {
            builder = builder.map(b -> b.add(value));
            return this;
        }

        @Override
        public ListBuilder<T> add(final DataResult<T> value) {
            builder = builder.apply2stable(ImmutableList.Builder::add, value);
            return this;
        }

        @Override
        public ListBuilder<T> withErrorsFrom(final DataResult<?> result) {
            builder = builder.flatMap(r -> result.map(v -> r));
            return this;
        }

        @Override
        public ListBuilder<T> mapError(final UnaryOperator<String> onError) {
            builder = builder.mapError(onError);
            return this;
        }

        @Override
        public DataResult<T> build(final T prefix) {
            final DataResult<T> result = builder.flatMap(b -> ops.mergeToList(prefix, b.build()));
            builder = DataResult.success(ImmutableList.builder(), Lifecycle.stable());
            return result;
        }
    }
}
