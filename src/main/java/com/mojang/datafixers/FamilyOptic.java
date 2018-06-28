package com.mojang.datafixers;

public interface FamilyOptic<A, B> {
    OpticParts<A, B> apply(final int index);
}
