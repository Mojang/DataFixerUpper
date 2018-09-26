// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

public interface FamilyOptic<A, B> {
    OpticParts<A, B> apply(final int index);
}
