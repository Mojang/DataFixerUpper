// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;

import javax.annotation.Nullable;

public interface OpticFinder<FT> {
    Type<FT> type();

    <A, FR> Either<TypedOptic<A, ?, FT, FR>, Type.FieldNotFoundException> findType(final Type<A> containerType, final Type<FR> resultType, final boolean recurse);

    default <A> Either<TypedOptic<A, ?, FT, FT>, Type.FieldNotFoundException> findType(final Type<A> containerType, final boolean recurse) {
        return findType(containerType, type(), recurse);
    }

    default <GT> OpticFinder<FT> inField(@Nullable final String name, final Type<GT> type) {
        final OpticFinder<FT> outer = this;
        return new OpticFinder<FT>() {
            @Override
            public Type<FT> type() {
                return outer.type();
            }

            @Override
            public <A, FR> Either<TypedOptic<A, ?, FT, FR>, Type.FieldNotFoundException> findType(final Type<A> containerType, final Type<FR> resultType, final boolean recurse) {
                final Either<TypedOptic<GT, ?, FT, FR>, Type.FieldNotFoundException> secondOptic = outer.findType(type, resultType, recurse);
                return secondOptic.map(l -> cap(containerType, l, recurse), Either::right);
            }

            private <A, FR, GR> Either<TypedOptic<A, ?, FT, FR>, Type.FieldNotFoundException> cap(final Type<A> containterType, final TypedOptic<GT, GR, FT, FR> l1, final boolean recurse) {
                final Either<TypedOptic<A, ?, GT, GR>, Type.FieldNotFoundException> first = DSL.fieldFinder(name, type).findType(containterType, l1.tType(), recurse);
                return first.mapLeft(l -> l.compose(l1));
            }
        };
    }
}
