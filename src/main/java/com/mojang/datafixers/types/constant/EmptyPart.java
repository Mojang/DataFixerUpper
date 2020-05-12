// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;

import java.util.Optional;

public final class EmptyPart extends Type<Unit> {
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
    protected Codec<Unit> buildCodec() {
        return Codec.EMPTY.codec();
    }
}
