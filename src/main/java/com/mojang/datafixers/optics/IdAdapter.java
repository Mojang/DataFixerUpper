package com.mojang.datafixers.optics;

class IdAdapter<S, T> implements Adapter<S, T, S, T> {
    @Override
    public S from(final S s) {
        return s;
    }

    @Override
    public T to(final T b) {
        return b;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof IdAdapter<?, ?>;
    }

    @Override
    public String toString() {
        return "id";
    }
}
