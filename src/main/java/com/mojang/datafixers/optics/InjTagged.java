// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import java.util.Objects;

/**
 * Unchecked cast if name matches
 */
public final class InjTagged<K, A, B> implements Prism<Pair<K, ?>, Pair<K, ?>, A, B> {
    private final K key;

    public InjTagged(final K key) {
        this.key = key;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Either<Pair<K, ?>, A> match(final Pair<K, ?> pair) {
        return Objects.equals(key, pair.getFirst()) ? Either.right((A) pair.getSecond()) : Either.left(pair);
    }

    @Override
    public Pair<K, ?> build(final B b) {
        return Pair.of(key, b);
    }

    @Override
    public String toString() {
        return "inj[" + key + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof InjTagged<?, ?, ?> && Objects.equals(((InjTagged<?, ?, ?>) obj).key, key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
