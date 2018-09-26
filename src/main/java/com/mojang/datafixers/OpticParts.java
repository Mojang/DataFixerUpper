// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.optics.Optic;

import java.util.Set;

/**
 * apply(i).sType == family.apply(i)
 */
public final class OpticParts<A, B> {
    private final Set<TypeToken<? extends K1>> bounds;
    private final Optic<?, ?, ?, A, B> optic;

    public OpticParts(final Set<TypeToken<? extends K1>> bounds, final Optic<?, ?, ?, A, B> optic) {
        this.bounds = bounds;
        this.optic = optic;
    }

    public Set<TypeToken<? extends K1>> bounds() {
        return bounds;
    }

    public Optic<?, ?, ?, A, B> optic() {
        return optic;
    }
}
