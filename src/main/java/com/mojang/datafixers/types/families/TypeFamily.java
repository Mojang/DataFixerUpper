package com.mojang.datafixers.types.families;

import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.OpticParts;
import com.mojang.datafixers.types.Type;

import java.util.function.IntFunction;

public interface TypeFamily {
    Type<?> apply(final int index);

    static <A, B> FamilyOptic<A, B> familyOptic(final IntFunction<OpticParts<A, B>> optics) {
        return optics::apply;
    }
}
