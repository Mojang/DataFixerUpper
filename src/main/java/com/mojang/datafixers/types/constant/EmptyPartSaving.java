// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EmptyPartSaving extends com.mojang.datafixers.types.Type<Dynamic<?>> {
    @Override
    public String toString() {
        return "EmptyPartSaving";
    }

    @Override
    public Optional<Dynamic<?>> point(final DynamicOps<?> ops) {
        return Optional.of(new Dynamic<>(ops));
    }

    @Override
    public boolean equals(final Object o, final boolean ignoreRecursionPoints, final boolean checkIndex) {
        return this == o;
    }

    @Override
    public TypeTemplate buildTemplate() {
        return DSL.constType(this);
    }

    @Override
    public <T> DataResult<Pair<Dynamic<?>, T>> read(final DynamicOps<T> ops, final T input) {
        return DataResult.success(Pair.of(new Dynamic<>(ops, input), ops.empty()));
    }

    @Override
    public final <T> DataResult<T> write(final DynamicOps<T> ops, final T rest, final Dynamic<?> value) {
        if (value.getValue() == value.getOps().empty()) {
            // nothing to merge, return rest
            return DataResult.success(rest);
        }

        final T casted = value.convert(ops).getValue();
        if (rest == ops.empty()) {
            // no need to merge anything, return the old value
            return DataResult.success(casted);
        }

        final DataResult<T> toMap = ops.getMapValues(casted).flatMap(map -> ops.mergeInto(rest, map.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))));
        return toMap.result().map(DataResult::success).orElseGet(() -> {
            final DataResult<T> toList = ops.getStream(casted).flatMap(stream -> ops.mergeInto(rest, stream.collect(Collectors.toList())));
            return toList.result().map(DataResult::success).orElseGet(() ->
                DataResult.error("Don't know how to merge " + rest + " and " + casted, rest)
            );
        });
    }
}
