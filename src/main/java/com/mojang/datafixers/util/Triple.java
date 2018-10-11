// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.util;

import java.util.Objects;

public class Triple<F, S, T> {

    private final F first;
    private final S second;
    private final T third;

    public Triple(final F first, final S second, final T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    public T getThird() {
        return third;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Triple)) {
            return false;
        }

        final Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second) && Objects.equals(third, other.third);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(first, second, third);
    }

    public static <F, S, T> Triple<F, S, T> of(final F first, final S second, T third) {
        return new Triple<>(first, second, third);
    }
}
