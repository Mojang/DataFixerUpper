// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics.profunctors;

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.kinds.K2;

public interface AffineP<P extends K2, Mu extends AffineP.Mu> extends Cartesian<P, Mu>, Cocartesian<P, Mu> {
    interface Mu extends Cartesian.Mu, Cocartesian.Mu {
        TypeToken<Mu> TYPE_TOKEN = new TypeToken<Mu>() {};
    }
}
