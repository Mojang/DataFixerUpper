package com.mojang.datafixers.types.templates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.OpticParts;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.optics.ListTraversal;
import com.mojang.datafixers.optics.Optic;
import com.mojang.datafixers.optics.profunctors.TraversalP;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public final class List implements TypeTemplate {
    private final TypeTemplate element;

    public List(final TypeTemplate element) {
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
                return DSL.list(element.apply(family).apply(index));
            }

            /*@Override
            public <A, B> Either<Type.FieldOptic<?, ?, A, B>, Type.FieldNotFoundException> findField(final int index, final String name, final Type<A> aType, final Type<B> bType) {
                final Either<Type.FieldOptic<?, ?, A, B>, Type.FieldNotFoundException> either = element.apply(family).findField(index, name, aType, bType);
                return either.mapLeft(this::cap);
            }

            private <A, B, FT, FR> Type.FieldOptic<?, ?, FT, FR> cap(final Type.FieldOptic<A, B, FT, FR> optic) {
                return list(optic.sType(), optic.tType()).compose(optic);
            }*/
        };
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(final FamilyOptic<A, B> input, final Type<A> aType, final Type<B> bType) {
        return TypeFamily.familyOptic(
            i -> {
                final OpticParts<A, B> pair = element.applyO(input, aType, bType).apply(i);
                final Set<TypeToken<? extends K1>> bounds = Sets.newHashSet(pair.bounds());
                bounds.add(TraversalP.Mu.TYPE_TOKEN);
                return new OpticParts<>(bounds, cap(pair.optic()));
            }
        );
    }

    private <S, T, A, B> Optic<?, ?, ?, A, B> cap(final Optic<?, S, T, A, B> concreteOptic) {
        return new ListTraversal<S, T>().composeUnchecked(concreteOptic);
    }

    @Override
    public <FT, FR> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(final int index, @Nullable final String name, final Type<FT> type, final Type<FR> resultType) {
        return element.findFieldOrType(index, name, type, resultType).mapLeft(List::new);
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(final TypeFamily family, final IntFunction<RewriteResult<?, ?>> function) {
        return i -> {
            final RewriteResult<?, ?> view = element.hmap(family, function).apply(i);
            return cap(apply(family).apply(i), view);
        };
    }

    private <E> RewriteResult<?, ?> cap(final Type<?> type, final RewriteResult<E, ?> view) {
        return ((ListType<E>) type).fix(view);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof List && Objects.equals(element, ((List) obj).element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element);
    }

    @Override
    public String toString() {
        return "List[" + element + "]";
    }

    public static final class ListType<A> extends Type<java.util.List<A>> {
        protected final Type<A> element;

        public ListType(final Type<A> element) {
            this.element = element;
        }

        @Override
        public RewriteResult<java.util.List<A>, ?> all(final TypeRewriteRule rule, final boolean recurse, final boolean checkIndex) {
            final RewriteResult<A, ?> view = element.rewriteOrNop(rule);
            return fix(view);
        }

        @Override
        public Optional<RewriteResult<java.util.List<A>, ?>> one(final TypeRewriteRule rule) {
            return rule.rewrite(element).map(this::fix);
        }

        @Override
        public Type<?> updateMu(final RecursiveTypeFamily newFamily) {
            return DSL.list(element.updateMu(newFamily));
        }

        @Override
        public TypeTemplate buildTemplate() {
            return DSL.list(element.template());
        }

        @Override
        public Optional<java.util.List<A>> point(final DynamicOps<?> ops) {
            return Optional.of(ImmutableList.of());
        }

        @Override
        public <FT, FR> Either<TypedOptic<java.util.List<A>, ?, FT, FR>, FieldNotFoundException> findTypeInChildren(final Type<FT> type, final Type<FR> resultType, final TypeMatcher<FT, FR> matcher, final boolean recurse) {
            final Either<TypedOptic<A, ?, FT, FR>, FieldNotFoundException> firstFieldLens = element.findType(type, resultType, matcher, recurse);
            return firstFieldLens.mapLeft(this::capLeft);
        }

        private <FT, FR, B> TypedOptic<java.util.List<A>, ?, FT, FR> capLeft(final TypedOptic<A, B, FT, FR> optic) {
            return TypedOptic.list(optic.sType(), optic.tType()).compose(optic);
        }

        public <B> RewriteResult<java.util.List<A>, ?> fix(final RewriteResult<A, B> view) {
            return opticView(this, view, TypedOptic.list(element, view.view().newType()));
        }

        @Override
        public <T> Pair<T, Optional<java.util.List<A>>> read(final DynamicOps<T> ops, final T input) {
            // TODO: read the same way we read CompoundList? (as much elements as possible)
            return ops.getStream(input).map(stream -> {
                final java.util.List<Optional<A>> list = stream.map(value -> element.read(ops, value).getSecond()).collect(Collectors.toList());
                if (list.stream().anyMatch(o -> !o.isPresent())) {
                    return Pair.of(input, Optional.<java.util.List<A>>empty());
                }
                return Pair.of(ops.empty(), Optional.of(list.stream().map(Optional::get).collect(Collectors.toList())));
            }).orElseGet(() -> Pair.of(input, Optional.empty()));
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final T rest, final java.util.List<A> value) {
            return ops.merge(rest, ops.createList(value.stream().map(a -> element.write(ops, ops.empty(), a))));
        }

        @Override
        public String toString() {
            return "List[" + element + "]";
        }

        @Override
        public boolean equals(final Object obj, final boolean ignoreRecursionPoints, final boolean checkIndex) {
            return obj instanceof ListType<?> && element.equals(((ListType<?>) obj).element, ignoreRecursionPoints, checkIndex);
        }

        @Override
        public int hashCode() {
            return element.hashCode();
        }

        public Type<A> getElement() {
            return element;
        }
    }
}
