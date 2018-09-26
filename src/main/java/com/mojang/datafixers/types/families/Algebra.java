// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.families;

import com.mojang.datafixers.RewriteResult;

public interface Algebra {
//    TypeTemplate template();

//    TypeFamily family();

    /**
     * template.apply(family).apply(i) == op(i).argType()
     * family.apply(i) == op(i).resType()
     * <p>
     * bitset of used recursive values, for optimization
     */
    RewriteResult<?, ?> apply(final int index);

    String toString(int level);
}
