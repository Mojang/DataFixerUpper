// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

class IdAdapter<S, T> implements Adapter<S, T, S, T> {
    static final IdAdapter<?, ?> INSTANCE = new IdAdapter<>();

    private IdAdapter() {
    }

    @Override
    public S from(final S s) {
        return s;
    }

    @Override
    public T to(final T b) {
        return b;
    }

    @Override
    public String toString() {
        return "id";
    }
}
