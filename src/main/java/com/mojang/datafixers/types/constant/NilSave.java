// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.templates.Const;

import java.util.Optional;

public final class NilSave extends Const.ConstType<Dynamic<?>> {
    @Override
    public <T> Pair<T, Optional<Dynamic<?>>> read(final DynamicOps<T> ops, final T input) {
        return Pair.of(ops.empty(), Optional.of(new Dynamic<>(ops, input)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T write(final DynamicOps<T> ops, final T rest, final Dynamic<?> value) {
        return ops.mergeInto(ops.mergeInto(ops.emptyMap(), rest), value.cast(ops));
    }

    @Override
    public String toString() {
        return "NilSave";
    }

    @Override
    public Optional<Dynamic<?>> point(final DynamicOps<?> ops) {
        return Optional.of(capEmpty(ops));
    }

    private <T> Dynamic<T> capEmpty(final DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.emptyMap());
    }
}
