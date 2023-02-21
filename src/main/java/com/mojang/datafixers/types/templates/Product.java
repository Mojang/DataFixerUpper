// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.templates;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.optics.Optic;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.Traversal;
import com.mojang.datafixers.optics.profunctors.TraversalP;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.IntFunction;

public record Product(TypeTemplate f, TypeTemplate g) implements TypeTemplate {
    @Override
    public int size() {
        return Math.max(f.size(), g.size());
    }

    @Override
    public TypeFamily apply(final TypeFamily family) {
        return new TypeFamily() {
            @Override
            public Type<?> apply(final int index) {
                return DSL.and(f.apply(family).apply(index), g.apply(family).apply(index));
            }

            /*@Override
            public <A, B> Either<Type.FieldOptic<?, ?, A, B>, Type.FieldNotFoundException> findField(final int index, final String name, final Type<A> aType, final Type<B> bType) {
                final Either<Type.FieldOptic<?, ?, A, B>, Type.FieldNotFoundException> either = f.apply(family).findField(index, name, aType, bType);
                return either.map(
                    f2 -> Either.left(capLeft(g.apply(family).apply(index), f2)),
                    r -> g.apply(family).findField(index, name, aType, bType).mapLeft(g2 -> capRight(f.apply(family).apply(index), g2))
                );
            }

            private <A, B, FT, FR> Type.FieldOptic<?, ?, FT, FR> capLeft(final Type<?> secondType, final Type.FieldOptic<A, B, FT, FR> optic) {
                return proj1(optic.sType(), secondType, optic.tType()).compose(optic);
            }

            private <A, B, FT, FR> Type.FieldOptic<?, ?, FT, FR> capRight(final Type<?> firstType, final Type.FieldOptic<A, B, FT, FR> optic) {
                return proj2(firstType, optic.sType(), optic.tType()).compose(optic);
            }*/
        };
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(final FamilyOptic<A, B> input, final Type<A> aType, final Type<B> bType) {
        return TypeFamily.familyOptic(
            i -> cap(
                f.applyO(input, aType, bType),
                g.applyO(input, aType, bType),
                i
            )
        );
    }

    @SuppressWarnings("unchecked")
    private <A, B, LS, RS, LT, RT> TypedOptic<?, ?, A, B> cap(final FamilyOptic<A, B> lo, final FamilyOptic<A, B> ro, final int index) {
        final TypeToken<TraversalP.Mu> bound = TraversalP.Mu.TYPE_TOKEN;

        final TypedOptic<LS, LT, A, B> lp = (TypedOptic<LS, LT, A, B>) lo.apply(index);
        final TypedOptic<RS, RT, A, B> rp = (TypedOptic<RS, RT, A, B>) ro.apply(index);

        final Optic<? super TraversalP.Mu, LS, LT, A, B> l = lp.upCast(bound).orElseThrow(IllegalArgumentException::new);
        final Optic<? super TraversalP.Mu, RS, RT, A, B> r = rp.upCast(bound).orElseThrow(IllegalArgumentException::new);

        final Traversal<LS, LT, A, B> lt = Optics.toTraversal(l);
        final Traversal<RS, RT, A, B> rt = Optics.toTraversal(r);

        return new TypedOptic<>(
            ImmutableSet.of(bound),
            DSL.and(lp.sType(), rp.sType()),
            DSL.and(lp.tType(), rp.tType()),
            lp.aType(), lp.bType(),
            new Traversal<>() {
                @Override
                public <F extends K1> FunctionType<Pair<LS, RS>, App<F, Pair<LT, RT>>> wander(final Applicative<F, ?> applicative, final FunctionType<A, App<F, B>> input) {
                    return p -> applicative.ap2(applicative.point(Pair::of),
                        lt.wander(applicative, input).apply(p.getFirst()),
                        rt.wander(applicative, input).apply(p.getSecond())
                    );
                }
            }
        );
    }

    @Override
    public <FT, FR> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(final int index, @Nullable final String name, final Type<FT> type, final Type<FR> resultType) {
        final Either<TypeTemplate, Type.FieldNotFoundException> either = f.findFieldOrType(index, name, type, resultType);
        return either.map(
            f2 -> Either.left(new Product(f2, g)),
            r -> g.findFieldOrType(index, name, type, resultType).mapLeft(g2 -> new Product(f, g2))
        );
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(final TypeFamily family, final IntFunction<RewriteResult<?, ?>> function) {
        return i -> {
            final RewriteResult<?, ?> f1 = f.hmap(family, function).apply(i);
            final RewriteResult<?, ?> f2 = g.hmap(family, function).apply(i);
            return cap(apply(family).apply(i), f1, f2);
        };
    }

    private <L, R> RewriteResult<?, ?> cap(final Type<?> type, final RewriteResult<L, ?> f1, final RewriteResult<R, ?> f2) {
        return ((ProductType<L, R>) type).mergeViews(f1, f2);
    }

    @Override
    public String toString() {
        return "(" + f + ", " + g + ")";
    }

    public static final class ProductType<F, G> extends Type<Pair<F, G>> {
        protected final Type<F> first;
        protected final Type<G> second;
        private int hashCode;

        public ProductType(final Type<F> first, final Type<G> second) {
            this.first = first;
            this.second = second;
        }

        public Type<F> first() {
            return first;
        }

        public Type<G> second() {
            return second;
        }

        @Override
        public RewriteResult<Pair<F, G>, ?> all(final TypeRewriteRule rule, final boolean recurse, final boolean checkIndex) {
            return mergeViews(first.rewriteOrNop(rule), second.rewriteOrNop(rule));
        }

        public <F2, G2> RewriteResult<Pair<F, G>, ?> mergeViews(final RewriteResult<F, F2> leftView, final RewriteResult<G, G2> rightView) {
            final RewriteResult<Pair<F, G>, Pair<F2, G>> v1 = fixLeft(this, first, second, leftView);
            final RewriteResult<Pair<F2, G>, Pair<F2, G2>> v2 = fixRight(v1.view().newType(), leftView.view().newType(), second, rightView);
            return v2.compose(v1);
        }

        @Override
        public Optional<RewriteResult<Pair<F, G>, ?>> one(final TypeRewriteRule rule) {
            return DataFixUtils.or(
                rule.rewrite(first).map(v -> fixLeft(this, first, second, v)),
                () -> rule.rewrite(second).map(v -> fixRight(this, first, second, v))
            );
        }

        private static <F, G, F2> RewriteResult<Pair<F, G>, Pair<F2, G>> fixLeft(final Type<Pair<F, G>> type, final Type<F> first, final Type<G> second, final RewriteResult<F, F2> view) {
            return opticView(type, view, TypedOptic.proj1(first, second, view.view().newType()));
        }

        private static <F, G, G2> RewriteResult<Pair<F, G>, Pair<F, G2>> fixRight(final Type<Pair<F, G>> type, final Type<F> first, final Type<G> second, final RewriteResult<G, G2> view) {
            return opticView(type, view, TypedOptic.proj2(first, second, view.view().newType()));
        }

        @Override
        public Type<?> updateMu(final RecursiveTypeFamily newFamily) {
            return DSL.and(first.updateMu(newFamily), second.updateMu(newFamily));
        }

        @Override
        public TypeTemplate buildTemplate() {
            return DSL.and(first.template(), second.template());
        }

        @Override
        public Optional<TaggedChoice.TaggedChoiceType<?>> findChoiceType(final String name, final int index) {
            return DataFixUtils.or(first.findChoiceType(name, index), () -> second.findChoiceType(name, index));
        }

        @Override
        public Optional<Type<?>> findCheckedType(final int index) {
            return DataFixUtils.or(first.findCheckedType(index), () -> second.findCheckedType(index));
        }

        @Override
        public Codec<Pair<F, G>> buildCodec() {
            return Codec.pair(first.codec(), second.codec());
        }

        @Override
        public String toString() {
            return "(" + first + ", " + second + ")";
        }

        @Override
        public boolean equals(final Object obj, final boolean ignoreRecursionPoints, final boolean checkIndex) {
            if (!(obj instanceof ProductType<?, ?>)) {
                return false;
            }
            final ProductType<?, ?> that = (ProductType<?, ?>) obj;
            return first.equals(that.first, ignoreRecursionPoints, checkIndex) && second.equals(that.second, ignoreRecursionPoints, checkIndex);
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                int result = first.hashCode();
                result = 31 * result + second.hashCode();
                hashCode = result;
            }
            return hashCode;
        }

        @Override
        public Optional<Type<?>> findFieldTypeOpt(final String name) {
            return DataFixUtils.or(first.findFieldTypeOpt(name), () -> second.findFieldTypeOpt(name));
        }

        @Override
        public Optional<Pair<F, G>> point(final DynamicOps<?> ops) {
            return first.point(ops).flatMap(f -> second.point(ops).map(g -> Pair.of(f, g)));
        }

        @Override
        public <FT, FR> Either<TypedOptic<Pair<F, G>, ?, FT, FR>, FieldNotFoundException> findTypeInChildren(final Type<FT> type, final Type<FR> resultType, final TypeMatcher<FT, FR> matcher, final boolean recurse) {
            final Either<TypedOptic<F, ?, FT, FR>, FieldNotFoundException> firstFieldLens = first.findType(type, resultType, matcher, recurse);
            return firstFieldLens.map(
                this::capLeft,
                r -> {
                    final Either<TypedOptic<G, ?, FT, FR>, FieldNotFoundException> secondFieldLens = second.findType(type, resultType, matcher, recurse);
                    return secondFieldLens.mapLeft(this::capRight);
                }
            );
        }

        private <FT, F2, FR> Either<TypedOptic<Pair<F, G>, ?, FT, FR>, FieldNotFoundException> capLeft(final TypedOptic<F, F2, FT, FR> optic) {
            return Either.left(TypedOptic.proj1(optic.sType(), second, optic.tType()).compose(optic));
        }

        private <FT, G2, FR> TypedOptic<Pair<F, G>, ?, FT, FR> capRight(final TypedOptic<G, G2, FT, FR> optic) {
            return TypedOptic.proj2(first, optic.sType(), optic.tType()).compose(optic);
        }
    }
}
