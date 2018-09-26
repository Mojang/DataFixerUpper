// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.templates;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.functions.Functions;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class Named implements TypeTemplate {
    private final String name;
    private final TypeTemplate element;

    public Named(final String name, final TypeTemplate element) {
        this.name = name;
        this.element = element;
    }

    @Override
    public int size() {
        return element.size();
    }

    @Override
    public TypeFamily apply(final TypeFamily family) {
        return index -> DSL.named(name, element.apply(family).apply(index));
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(final FamilyOptic<A, B> input, final Type<A> aType, final Type<B> bType) {
        return TypeFamily.familyOptic(i -> element.applyO(input, aType, bType).apply(i));
    }

    @Override
    public <FT, FR> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(final int index, @Nullable final String name, final Type<FT> type, final Type<FR> resultType) {
        return element.findFieldOrType(index, name, type, resultType);
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(final TypeFamily family, final IntFunction<RewriteResult<?, ?>> function) {
        return index -> {
            final RewriteResult<?, ?> elementResult = element.hmap(family, function).apply(index);
            return cap(family, index, elementResult);
        };
    }

    private <A> RewriteResult<Pair<String, A>, ?> cap(final TypeFamily family, final int index, final RewriteResult<A, ?> elementResult) {
        return NamedType.fix((NamedType<A>) apply(family).apply(index), elementResult);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Named)) {
            return false;
        }
        final Named that = (Named) obj;
        return Objects.equals(name, that.name) && Objects.equals(element, that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, element);
    }

    @Override
    public String toString() {
        return "NamedTypeTag[" + name + ": " + element + "]";
    }

    public static final class NamedType<A> extends Type<Pair<String, A>> {
        protected final String name;
        protected final Type<A> element;

        public NamedType(final String name, final Type<A> element) {
            this.name = name;
            this.element = element;
        }

        public static <A, B> RewriteResult<Pair<String, A>, ?> fix(final NamedType<A> type, final RewriteResult<A, B> instance) {
            if (Objects.equals(instance.view().function(), Functions.id())) {
                return RewriteResult.nop(type);
            }
            return opticView(type, instance, wrapOptic(type.name, TypedOptic.adapter(instance.view().type(), instance.view().newType())));
        }

        @Override
        public RewriteResult<Pair<String, A>, ?> all(final TypeRewriteRule rule, final boolean recurse, final boolean checkIndex) {
            final RewriteResult<A, ?> elementView = element.rewriteOrNop(rule);
            return fix(this, elementView);
        }

        @Override
        public Optional<RewriteResult<Pair<String, A>, ?>> one(final TypeRewriteRule rule) {
            final Optional<RewriteResult<A, ?>> view = rule.rewrite(element);
            return view.map(instance -> fix(this, instance));
        }

        @Override
        public Type<?> updateMu(final RecursiveTypeFamily newFamily) {
            return DSL.named(name, element.updateMu(newFamily));
        }

        @Override
        public TypeTemplate buildTemplate() {
            return DSL.named(name, element.template());
        }

        @Override
        public Optional<TaggedChoice.TaggedChoiceType<?>> findChoiceType(final String name, final int index) {
            return element.findChoiceType(name, index);
        }

        @Override
        public Optional<Type<?>> findCheckedType(final int index) {
            return element.findCheckedType(index);
        }

        @Override
        public <T> Pair<T, Optional<Pair<String, A>>> read(final DynamicOps<T> ops, final T input) {
            return element.read(ops, input).mapSecond(vo -> vo.map(v -> Pair.of(name, v)));
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final T rest, final Pair<String, A> value) {
            if (!Objects.equals(value.getFirst(), name)) {
                throw new IllegalStateException("Named type name doesn't match: expected: " + name + ", got: " + value.getFirst());
            }
            return element.write(ops, rest, value.getSecond());
        }

        @Override
        public String toString() {
            return "NamedType[\"" + name + "\", " + element + "]";
        }

        public String name() {
            return name;
        }

        public Type<A> element() {
            return element;
        }

        @Override
        public boolean equals(final Object obj, final boolean ignoreRecursionPoints, final boolean checkIndex) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof NamedType<?>)) {
                return false;
            }
            final NamedType<?> other = (NamedType<?>) obj;
            return Objects.equals(name, other.name) && element.equals(other.element, ignoreRecursionPoints, checkIndex);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, element);
        }

        public <A2> NamedType<A2> map(final Function<? super Type<A>, ? extends Type<A2>> function) {
            return new NamedType<>(name, function.apply(element));
        }

        @Override
        public Optional<Type<?>> findFieldTypeOpt(final String name) {
            return element.findFieldTypeOpt(name);
        }

        @Override
        public Optional<Pair<String, A>> point(final DynamicOps<?> ops) {
            return element.point(ops).map(value -> Pair.of(name, value));
        }

        @Override
        public <FT, FR> Either<TypedOptic<Pair<String, A>, ?, FT, FR>, FieldNotFoundException> findTypeInChildren(final Type<FT> type, final Type<FR> resultType, final TypeMatcher<FT, FR> matcher, final boolean recurse) {
            return element.findType(type, resultType, matcher, recurse).mapLeft(o -> wrapOptic(name, o));
        }

        protected static <A, B, FT, FR> TypedOptic<Pair<String, A>, Pair<String, B>, FT, FR> wrapOptic(final String name, final TypedOptic<A, B, FT, FR> optic) {
            final ImmutableSet.Builder<TypeToken<? extends K1>> builder = ImmutableSet.builder();
            builder.addAll(optic.bounds());
            builder.add(Cartesian.Mu.TYPE_TOKEN);
            return new TypedOptic<>(
                builder.build(),
                DSL.named(name, optic.sType()),
                DSL.named(name, optic.tType()),
                optic.aType(),
                optic.bType(),
                Optics.<String, A, B>proj2().composeUnchecked(optic.optic())
            );
        }
    }
}
