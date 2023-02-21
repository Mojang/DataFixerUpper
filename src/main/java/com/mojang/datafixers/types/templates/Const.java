// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.templates;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.profunctors.AffineP;
import com.mojang.datafixers.optics.profunctors.Profunctor;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.TypeFamily;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.IntFunction;

public record Const(Type<?> type) implements TypeTemplate {
    @Override
    public int size() {
        return 0;
    }

    @Override
    public TypeFamily apply(final TypeFamily family) {
        return new TypeFamily() {
            @Override
            public Type<?> apply(final int index) {
                return type;
            }

            /*@Override
            public <A, B> Either<Type.FieldOptic<?, ?, A, B>, Type.FieldNotFoundException> findField(final int index, final String name, final Type<A> aType, final Type<B> bType) {
                return type.findField(name, aType, bType, false).mapLeft(o -> o);
            }*/
        };
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(final FamilyOptic<A, B> input, final Type<A> aType, final Type<B> bType) {
        if (Objects.equals(type, aType)) {
            return TypeFamily.familyOptic(i -> new TypedOptic<>(ImmutableSet.of(Profunctor.Mu.TYPE_TOKEN), aType, bType, aType, bType, Optics.id()));
        }
        final TypedOptic<?, ?, A, B> ignoreOptic = makeIgnoreOptic(type, aType, bType);
        return TypeFamily.familyOptic(i -> ignoreOptic);
    }

    private <T, A, B> TypedOptic<T, T, A, B> makeIgnoreOptic(final Type<T> type, final Type<A> aType, final Type<B> bType) {
        return new TypedOptic<>(
            AffineP.Mu.TYPE_TOKEN,
            type,
            type,
            aType,
            bType,
            Optics.affine(Either::left, (b, t) -> t)
        );
    }

    @Override
    public <FT, FR> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(final int index, @Nullable final String name, final Type<FT> type, final Type<FR> resultType) {
        return DSL.fieldFinder(name, type).findType(this.type, resultType, false).mapLeft(field -> new Const(field.tType()));
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(final TypeFamily family, final IntFunction<RewriteResult<?, ?>> function) {
        return i -> RewriteResult.nop(type);
    }

    @Override
    public String toString() {
        return "Const[" + type + "]";
    }

    public static final class PrimitiveType<A> extends Type<A> {
        private final Codec<A> codec;

        public PrimitiveType(final Codec<A> codec) {
            this.codec = codec;
        }

        @Override
        public boolean equals(final Object o, final boolean ignoreRecursionPoints, final boolean checkIndex) {
            return this == o;
        }

        @Override
        public TypeTemplate buildTemplate() {
            return DSL.constType(this);
        }

        @Override
        protected Codec<A> buildCodec() {
            return codec;
        }

        @Override
        public String toString() {
            return codec.toString();
        }
    }
}
