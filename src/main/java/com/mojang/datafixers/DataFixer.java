// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

import com.mojang.datafixers.schemas.Schema;

public interface DataFixer {
    <T> Dynamic<T> update(DSL.TypeReference type, Dynamic<T> input, int version, int newVersion);

    Schema getSchema(int key);
}
