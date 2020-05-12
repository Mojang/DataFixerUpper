// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.templates;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.TypeFamily;
import com.mojang.datafixers.util.Either;

import javax.annotation.Nullable;
import java.util.function.IntFunction;

public interface TypeTemplate {
    int size();

    TypeFamily apply(final TypeFamily family);

    default Type<?> toSimpleType() {
        return apply(new TypeFamily() {
            @Override
            public Type<?> apply(final int index) {
                return DSL.emptyPartType();
            }

            /*@Override
            public <A, B> Either<Type.FieldOptic<?, ?, A, B>, Type.FieldNotFoundException> findField(final int index, final String name, final Type<A> aType, final Type<B> bType) {
                return Either.right(new Type.FieldNotFoundException("Simple type rec dummy"));
            }*/
        }).apply(-1);
    }

    /**
     * returned optic will accept template<family<index>> with the input template, and will return the same with the returned template
     * (template, optic) = Left(result)
     * this.apply(family).apply(index) == optic.sType
     * template.apply(family).apply(index) == optic.tType
     */
    <A, B> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(final int index, @Nullable String name, Type<A> type, Type<B> resultType);

    /**
     * constraint: family, argFamily and resFamily are matched
     * result.function(i) :: this.apply(function.argFamily()).apply(i) -> this.apply(function.resFamily()).apply(i)
     */
    IntFunction<RewriteResult<?, ?>> hmap(final TypeFamily family, final IntFunction<RewriteResult<?, ?>> function);

    <A, B> FamilyOptic<A, B> applyO(final FamilyOptic<A, B> input, final Type<A> aType, final Type<B> bType);
}
