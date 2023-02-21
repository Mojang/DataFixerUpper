// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.families;

import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.types.Type;

import java.util.function.IntFunction;

public interface TypeFamily {
    Type<?> apply(final int index);

    static <A, B> FamilyOptic<A, B> familyOptic(final IntFunction<TypedOptic<?, ?, A, B>> optics) {
        return optics::apply;
    }
}
