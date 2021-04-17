// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.util;

import java.util.Objects;

public class ValueHolder<T> {
    private T value;

    public ValueHolder(final T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public ValueHolder<T> setValue(final T value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ValueHolder)) {
            return false;
        }

        ValueHolder<?> other = (ValueHolder<?>) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
