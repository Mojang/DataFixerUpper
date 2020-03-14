// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.google.common.collect.ImmutableMap;

public interface RecordBuilder<T> {
    DynamicOps<T> ops();

    RecordBuilder<T> add(T key, T value);

    RecordBuilder<T> add(T key, DataResult<T> value);

    RecordBuilder<T> add(DataResult<T> key, DataResult<T> value);

    DataResult<T> build(T prefix);

    default RecordBuilder<T> add(final String key, final T value) {
        return add(ops().createString(key), value);
    }

    default RecordBuilder<T> add(final String key, final DataResult<T> value) {
        return add(ops().createString(key), value);
    }

    default RecordBuilder<T> add(final String key, final Serializable value) {
        return add(key, value, ops().empty());
    }

    default RecordBuilder<T> add(final String key, final Serializable value, final T elementPrefix) {
        return add(key, value.serialize(ops(), elementPrefix));
    }

    final class Builder<T> implements RecordBuilder<T> {
        private final DynamicOps<T> ops;
        private DataResult<ImmutableMap.Builder<T, T>> builder = DataResult.success(ImmutableMap.builder());

        public Builder(final DynamicOps<T> ops) {
            this.ops = ops;
        }

        @Override
        public DynamicOps<T> ops() {
            return ops;
        }

        @Override
        public RecordBuilder<T> add(final T key, final T value) {
            builder = builder.map(b -> b.put(key, value));
            return this;
        }

        @Override
        public RecordBuilder<T> add(final T key, final DataResult<T> value) {
            builder = builder.flatMap(b -> value.map(v -> b.put(key, v)));
            return this;
        }

        @Override
        public RecordBuilder<T> add(final DataResult<T> key, final DataResult<T> value) {
            builder = builder.flatMap(b -> key.flatMap(k -> value.map(v -> b.put(k, v))));
            return this;
        }

        @Override
        public DataResult<T> build(final T prefix) {
            final DataResult<T> result = builder.flatMap(b -> ops.mergeToMap(prefix, b.build()));
            builder = DataResult.success(ImmutableMap.builder());
            return result;
        }
    }
}
