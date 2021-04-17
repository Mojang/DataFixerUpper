// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.constant;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import java.util.Optional;

public final class EmptyPartPassthrough extends Type<Dynamic<?>> {
    @Override
    public String toString() {
        return "EmptyPartPassthrough";
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
    public Codec<Dynamic<?>> buildCodec() {
        return Codec.PASSTHROUGH;
    }
}
