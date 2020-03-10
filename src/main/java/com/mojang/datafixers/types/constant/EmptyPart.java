// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.Optional;

public final class EmptyPart extends com.mojang.datafixers.types.Type<Unit> {
    @Override
    public String toString() {
        return "EmptyPart";
    }

    @Override
    public Optional<Unit> point(final DynamicOps<?> ops) {
        return Optional.of(Unit.INSTANCE);
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
    public <T> Pair<T, Optional<Unit>> read(final DynamicOps<T> ops, final T input) {
        return Pair.of(input, point(ops));
    }

    @Override
    public final <T> DataResult<T> write(final DynamicOps<T> ops, final T rest, final Unit value) {
        return DataResult.success(rest);
    }
}
