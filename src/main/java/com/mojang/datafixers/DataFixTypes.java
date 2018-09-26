// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

public enum DataFixTypes implements DSL.TypeReference {
    LEVEL("level"),
    PLAYER("player"),
    CHUNK("chunk"),
    HOTBAR("hotbar"),
    OPTIONS("options"),
    STRUCTURE("structure"),
    STATS("stats"),
    SAVED_DATA("saved_data"),
    ADVANCEMENTS("advancements"),
    ;

    private final String name;

    DataFixTypes(final String name) {
        this.name = name;
    }

    @Override
    public TypeTemplate in(final Schema schema) {
        return schema.id(name);
    }

    @Override
    public String typeName() {
        return name;
    }
}
