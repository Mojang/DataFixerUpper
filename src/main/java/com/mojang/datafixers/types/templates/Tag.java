// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.templates;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.Functions;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public final class Tag implements TypeTemplate {
    private final String name;
    private final TypeTemplate element;

    public Tag(final String name, final TypeTemplate element) {
        this.name = name;
        this.element = element;
    }

    @Override
    public int size() {
        return element.size();
    }

    @Override
    public TypeFamily apply(final TypeFamily family) {
        return new TypeFamily() {
            @Override
            public Type<?> apply(final int index) {
                return DSL.field(name, element.apply(family).apply(index));
            }

            /*@Override
            public <A, B> Either<Type.FieldOptic<?, ?, A, B>, Type.FieldNotFoundException> findField(final int index, final String name, final Type<A> aType, final Type<B> bType) {
                if (!Objects.equals(name, NameTag.this.name)) {
                    return Either.right(new Type.FieldNotFoundException("Names don't match"));
                }
                if (element instanceof Const) {
                    final Const c = (Const) element;
                    if (Objects.equals(aType, c.type)) {
                        final Type.FieldOptic<A, B, A, B> optic = new Type.FieldOptic<>(
                            Lenses.Profunctor.Mu.TYPE_TOKEN,
                            Type.tag(name, aType), // this.apply(family).apply(index)
                            Type.tag(name, bType), // newCode.apply(family).apply(index)
                            aType,
                            bType,
                            Lenses.id()
                        );
                        return Either.left(optic);
                    }
                    return Either.right(new Type.FieldNotFoundException("don't match"));
                }
                return Either.right(new Type.FieldNotFoundException("Recursive field"));
            }*/
        };
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(final FamilyOptic<A, B> input, final Type<A> aType, final Type<B> bType) {
        return TypeFamily.familyOptic(i -> element.applyO(input, aType, bType).apply(i));
    }

    @Override
    public <FT, FR> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(final int index, @Nullable final String name, final Type<FT> type, final Type<FR> resultType) {
        if (!Objects.equals(name, this.name)) {
            return Either.right(new Type.FieldNotFoundException("Names don't match"));
        }
        if (element instanceof Const) {
            final Const c = (Const) element;
            if (Objects.equals(type, c.type())) {
                return Either.left(new Tag(name, new Const(resultType)));
            }
            return Either.right(new Type.FieldNotFoundException("don't match"));
        }
        // safe to return the same template
        if (Objects.equals(type, resultType)) {
            return Either.left(this);
        }
        if (type instanceof RecursivePoint.RecursivePointType<?> && element instanceof RecursivePoint) {
            if (((RecursivePoint) element).index() == ((RecursivePoint.RecursivePointType<?>) type).index()) {
                if (resultType instanceof RecursivePoint.RecursivePointType<?>) {
                    if (((RecursivePoint.RecursivePointType<?>) resultType).index() == ((RecursivePoint) element).index()) {
                        return Either.left(this);
                    }
                } else {
                    return Either.left(DSL.constType(resultType));
                }
            }
        }
        return Either.right(new Type.FieldNotFoundException("Recursive field"));
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(final TypeFamily family, final IntFunction<RewriteResult<?, ?>> function) {
        return element.hmap(family, function);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Tag)) {
            return false;
        }
        final Tag that = (Tag) obj;
        return Objects.equals(name, that.name) && Objects.equals(element, that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, element);
    }

    @Override
    public String toString() {
        return "NameTag[" + name + ": " + element + "]";
    }

    public static final class TagType<A> extends Type<A> {
        protected final String name;
        protected final Type<A> element;

        public TagType(final String name, final Type<A> element) {
            this.name = name;
            this.element = element;
        }

        @Override
        public RewriteResult<A, ?> all(final TypeRewriteRule rule, final boolean recurse, final boolean checkIndex) {
            final RewriteResult<A, ?> elementView = element.rewriteOrNop(rule);
            return RewriteResult.create(cap(elementView.view()), elementView.recData());
        }

        private <B> View<A, ?> cap(final View<A, B> instance) {
            if (Objects.equals(instance.function(), Functions.id())) {
                return View.nopView(this);
            }
            return View.create(this, DSL.field(name, instance.newType()), instance.function());
        }

        @Override
        public Optional<RewriteResult<A, ?>> one(final TypeRewriteRule rule) {
            final Optional<RewriteResult<A, ?>> view = rule.rewrite(element);
            return view.map(instance -> RewriteResult.create(cap(instance.view()), instance.recData()));
        }

        @Override
        public Type<?> updateMu(final RecursiveTypeFamily newFamily) {
            return DSL.field(name, element.updateMu(newFamily));
        }

        @Override
        public TypeTemplate buildTemplate() {
            return DSL.field(name, element.template());
        }

        @Override
        public <T> Pair<T, Optional<A>> read(final DynamicOps<T> ops, final T input) {
            final Optional<Map<T, T>> map = ops.getMapValues(input);
            final T nameObject = ops.createString(name);
            if (map.isPresent() && map.get().containsKey(nameObject)) {
                final T elementValue = map.get().get(nameObject);
                final Optional<A> value = element.read(ops, elementValue).getSecond();
                if (value.isPresent()) {
                    return Pair.of(ops.createMap(map.get().entrySet().stream().filter(e -> !Objects.equals(e.getKey(), nameObject)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))), value);
                }
            }
            return Pair.of(input, Optional.empty());
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final T rest, final A value) {
            return ops.mergeInto(rest, ops.createString(name), element.write(ops, ops.empty(), value));
        }

        @Override
        public String toString() {
            return "Tag[\"" + name + "\", " + element + "]";
        }

        @Override
        public boolean equals(final Object o, final boolean ignoreRecursionPoints, final boolean checkIndex) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final TagType<?> tagType = (TagType<?>) o;
            return Objects.equals(name, tagType.name) && element.equals(tagType.element, ignoreRecursionPoints, checkIndex);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, element);
        }

        public <A2> TagType<A2> map(final Function<? super Type<A>, ? extends Type<A2>> function) {
            return new TagType<>(name, function.apply(element));
        }

        @Override
        public Optional<Type<?>> findFieldTypeOpt(final String name) {
            if (Objects.equals(name, this.name)) {
                return Optional.of(element);
            }
            return Optional.empty();
        }

        @Override
        public Optional<A> point(final DynamicOps<?> ops) {
            return element.point(ops);
        }

        @Override
        public <FT, FR> Either<TypedOptic<A, ?, FT, FR>, FieldNotFoundException> findTypeInChildren(final Type<FT> type, final Type<FR> resultType, final TypeMatcher<FT, FR> matcher, final boolean recurse) {
            return element.findType(type, resultType, matcher, recurse).mapLeft(this::wrapOptic);
        }

        private <B, FT, FR> TypedOptic<A, B, FT, FR> wrapOptic(final TypedOptic<A, B, FT, FR> optic) {
            return new TypedOptic<>(
                optic.bounds(),
                DSL.field(name, optic.sType()),
                DSL.field(name, optic.tType()),
                optic.aType(),
                optic.bType(),
                optic.optic()
            );
        }

        public String name() {
            return name;
        }

        public Type<A> element() {
            return element;
        }
    }
}
