// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.google.common.collect.ImmutableList;

public interface ListBuilder<T> {
    DynamicOps<T> ops();

    DataResult<T> build(T prefix);

    ListBuilder<T> add(final T value);

    ListBuilder<T> add(final DataResult<T> value);

    default DataResult<T> build(final DataResult<T> prefix) {
        return prefix.flatMap(this::build);
    }

    default ListBuilder<T> add(final Serializable value) {
        return add(value, ops().empty());
    }

    default ListBuilder<T> add(final Serializable value, final T elementPrefix) {
        return add(value.serialize(ops(), elementPrefix));
    }

    default <E> ListBuilder<T> add(final E value, final Encoder<E> encoder) {
        return add(encoder.encodeStart(ops(), value));
    }

    default ListBuilder<T> addAll(final Iterable<? extends Serializable> values) {
        values.forEach(this::add);
        return this;
    }

    default <E> ListBuilder<T> addAll(final Iterable<E> values, final Encoder<E> encoder) {
        values.forEach(v -> encoder.encode(ops(), ops().empty(), v));
        return this;
    }

    final class Builder<T> implements ListBuilder<T> {
        private final DynamicOps<T> ops;
        private DataResult<ImmutableList.Builder<T>> builder = DataResult.success(ImmutableList.builder());

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
            builder = builder.flatMap(b -> value.map(b::add));
            return this;
        }

        @Override
        public DataResult<T> build(final T prefix) {
            final DataResult<T> result = builder.flatMap(b -> ops.mergeToList(prefix, b.build()));
            builder = DataResult.success(ImmutableList.builder());
            return result;
        }
    }
}
