// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.templates.Const;

import java.util.Optional;

public final class NilDrop extends Const.ConstType<Unit> {
    @Override
    public <T> Pair<T, Optional<Unit>> read(final DynamicOps<T> ops, final T input) {
        return Pair.of(input, point(ops));
    }

    @Override
    public <T> T write(final DynamicOps<T> ops, final T rest, final Unit value) {
        return rest;
    }

    @Override
    public String toString() {
        return "NilDrop";
    }

    @Override
    public Optional<Unit> point(final DynamicOps<?> ops) {
        return Optional.of(Unit.INSTANCE);
    }
}
