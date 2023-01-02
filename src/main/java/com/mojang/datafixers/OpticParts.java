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
public record OpticParts<A, B>(Set<TypeToken<? extends K1>> bounds, Optic<?, ?, ?, A, B> optic) {
}
