package com.mojang.serialization;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface Keyable {
    <T> Stream<T> keys(DynamicOps<T> ops);

    static Keyable forStrings(final Supplier<Stream<String>> keys) {
        return new Keyable() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return keys.get().map(ops::createString);
            }
        };
    }
}
