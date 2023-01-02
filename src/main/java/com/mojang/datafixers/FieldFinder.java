// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

import com.mojang.datafixers.optics.Adapter;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.Proj1;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.optics.profunctors.Profunctor;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Tag;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import javax.annotation.Nullable;
import java.util.Objects;

public final class FieldFinder<FT> implements OpticFinder<FT> {
    @Nullable
    private final String name;
    private final Type<FT> type;

    public FieldFinder(@Nullable final String name, final Type<FT> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public Type<FT> type() {
        return type;
    }

    @Override
    public <A, FR> Either<TypedOptic<A, ?, FT, FR>, Type.FieldNotFoundException> findType(final Type<A> containerType, final Type<FR> resultType, final boolean recurse) {
        return containerType.findTypeCached(type, resultType, new Matcher<>(name, type, resultType), recurse);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FieldFinder<?>)) {
            return false;
        }
        final FieldFinder<?> that = (FieldFinder<?>) o;
        return Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + type.hashCode();
        return result;
    }

    private static final class Matcher<FT, FR> implements Type.TypeMatcher<FT, FR> {
        private final Type<FR> resultType;
        @Nullable
        private final String name;
        private final Type<FT> type;

        public Matcher(@Nullable final String name, final Type<FT> type, final Type<FR> resultType) {
            this.resultType = resultType;
            this.name = name;
            this.type = type;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <S> Either<TypedOptic<S, ?, FT, FR>, Type.FieldNotFoundException> match(final Type<S> targetType) {
            if (name == null && type.equals(targetType, true, false)) {
                return Either.left((TypedOptic<S, FR, FT, FR>) new TypedOptic<>(
                    Profunctor.Mu.TYPE_TOKEN,
                    targetType,
                    resultType,
                    targetType,
                    resultType,
                    Optics.id()
                ));
            }
            if (targetType instanceof Tag.TagType<?>) {
                final Tag.TagType<S> tagType = (Tag.TagType<S>) targetType;
                if (!Objects.equals(tagType.name(), name)) {
                    return Either.right(new Type.FieldNotFoundException(String.format("Not found: \"%s\" (in type: %s)", name, targetType)));
                }
                if (!Objects.equals(type, tagType.element())) {
                    return Either.right(new Type.FieldNotFoundException(String.format("Type error for field \"%s\": expected type: %s, actual type: %s)", name, type, tagType.element())));
                }
                return Either.left(new TypedOptic<>(
                    Profunctor.Mu.TYPE_TOKEN,
                    tagType,
                    DSL.field(tagType.name(), resultType),
                    type,
                    resultType,
                    (Adapter<S, FR, FT, FR>) Optics.id()
                ));
            }
            if (targetType instanceof TaggedChoice.TaggedChoiceType<?>) {
                final TaggedChoice.TaggedChoiceType<FT> choiceType = (TaggedChoice.TaggedChoiceType<FT>) targetType;
                if (Objects.equals(name, choiceType.getName())) {
                    if (!Objects.equals(type, choiceType.getKeyType())) {
                        return Either.right(new Type.FieldNotFoundException(String.format("Type error for field \"%s\": expected type: %s, actual type: %s)", name, type, choiceType.getKeyType())));
                    }
                    if (!Objects.equals(type, resultType)) {
                        return Either.right(new Type.FieldNotFoundException("TaggedChoiceType key type change is unsupported."));
                    }
                    return Either.left((TypedOptic<S, ?, FT, FR>) capChoice(choiceType));
                }
            }
            return Either.right(new Type.Continue());
        }

        @SuppressWarnings("unchecked")
        private <V> TypedOptic<Pair<FT, V>, ?, FT, FT> capChoice(final Type<?> choiceType) {
            return new TypedOptic<>(
                Cartesian.Mu.TYPE_TOKEN,
                (Type<Pair<FT, V>>) choiceType,
                (Type<Pair<FT, V>>) choiceType,
                type,
                type,
                Optics.proj1()
            );
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Matcher<?, ?> matcher = (Matcher<?, ?>) o;
            return Objects.equals(resultType, matcher.resultType) && Objects.equals(name, matcher.name) && Objects.equals(type, matcher.type);
        }

        @Override
        public int hashCode() {
            int result = resultType.hashCode();
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + type.hashCode();
            return result;
        }
    }
}
