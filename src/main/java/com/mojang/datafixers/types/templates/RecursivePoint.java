// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.templates;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.Functions;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public record RecursivePoint(int index) implements TypeTemplate {
    @Override
    public int size() {
        return index + 1;
    }

    @Override
    public TypeFamily apply(final TypeFamily family) {
        final Type<?> result = family.apply(index);
        return new TypeFamily() {
            @Override
            public Type<?> apply(final int index) {
                return result;
            }

            /*@Override
            public <A, B> Either<Type.FieldOptic<?, ?, A, B>, Type.FieldNotFoundException> findField(final int index, final String name, final Type<A> aType, final Type<B> bType) {
                // Hmm
                return Either.right(new Type.FieldNotFoundException("Recursion point"));
            }*/
        };
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(final FamilyOptic<A, B> input, final Type<A> aType, final Type<B> bType) {
        return TypeFamily.familyOptic(i -> input.apply(index));
    }

    @Override
    public <FT, FR> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(final int index, @Nullable final String name, final Type<FT> type, final Type<FR> resultType) {
        return Either.right(new Type.FieldNotFoundException("Recursion point"));
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(final TypeFamily family, final IntFunction<RewriteResult<?, ?>> function) {
        return i -> {
            final RewriteResult<?, ?> result = function.apply(index);
            return cap(family, result);
        };
    }

    public <S, T> RewriteResult<S, T> cap(final TypeFamily family, final RewriteResult<S, T> result) {
        final Type<?> sourceType = family.apply(index);
        if (!(sourceType instanceof RecursivePointType<?>)) {
            throw new IllegalArgumentException("Type error: Recursive point template template got a non-recursice type as an input.");
        }
        if (!Objects.equals(result.view().type(), sourceType)) {
            throw new IllegalArgumentException("Type error: hmap function input type");
        }
        final BitSet bitSet = ObjectUtils.clone(result.recData());
        bitSet.set(index);
        return RewriteResult.create(result.view(), bitSet);
    }

    @Override
    public String toString() {
        return "Id[" + index + "]";
    }

    public static final class RecursivePointType<A> extends Type<A> {
        private final RecursiveTypeFamily family;
        private final int index;
        private final Supplier<Type<A>> delegate;
        @Nullable
        private volatile Type<A> type;

        public RecursivePointType(final RecursiveTypeFamily family, final int index, final Supplier<Type<A>> delegate) {
            this.family = family;
            this.index = index;
            this.delegate = delegate;
        }

        public RecursiveTypeFamily family() {
            return family;
        }

        public int index() {
            return index;
        }

        @SuppressWarnings("ConstantConditions")
        public Type<A> unfold() {
            if (type == null) {
                type = delegate.get();
            }
            return type;
        }

        /** needs to be lazy */
        @Override
        protected Codec<A> buildCodec() {
            return new Codec<A>() {
                @Override
                public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                    return unfold().codec().decode(ops, input).setLifecycle(Lifecycle.experimental());
                }

                @Override
                public <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
                    return unfold().codec().encode(input, ops, prefix).setLifecycle(Lifecycle.experimental());
                }
            };
        }

        @Override
        public RewriteResult<A, ?> all(final TypeRewriteRule rule, final boolean recurse, final boolean checkIndex) {
            // TODO: pass the template along the all, transform accordingly?
            return unfold().all(rule, recurse, checkIndex);
        }

        @Override
        public Optional<RewriteResult<A, ?>> one(final TypeRewriteRule rule) {
            return unfold().one(rule);
        }

        @Override
        public Optional<RewriteResult<A, ?>> everywhere(final TypeRewriteRule rule, final PointFreeRule optimizationRule, final boolean recurse, final boolean checkIndex) {
            if (recurse) {
                return family.everywhere(this.index, rule, optimizationRule).map(view -> (RewriteResult<A, ?>) view);
            }
            return Optional.of(RewriteResult.nop(this));
        }

        @Override
        public Type<?> updateMu(final RecursiveTypeFamily newFamily) {
            return newFamily.apply(index);
        }

        @Override
        public TypeTemplate buildTemplate() {
            return DSL.id(index);
        }

        @Override
        public Optional<TaggedChoice.TaggedChoiceType<?>> findChoiceType(final String name, final int index) {
            return unfold().findChoiceType(name, this.index);
        }

        @Override
        public Optional<Type<?>> findCheckedType(final int index) {
            return unfold().findCheckedType(this.index);
        }

        @Override
        public Optional<Type<?>> findFieldTypeOpt(final String name) {
            return unfold().findFieldTypeOpt(name);
        }

        @Override
        public Optional<A> point(final DynamicOps<?> ops) {
            return unfold().point(ops);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <FT, FR> Either<TypedOptic<A, ?, FT, FR>, FieldNotFoundException> findTypeInChildren(final Type<FT> type, final Type<FR> resultType, final TypeMatcher<FT, FR> matcher, final boolean recurse) {
            /*if (!recurse) {
                return delegate.get().findField(name, type, resultType, false).mapLeft(this::wrapOptic);
    //                return Either.right(new FieldNotFoundException("Recursion point"));
            }*/
            return family.findType(index, type, resultType, matcher, recurse).mapLeft(o -> {
                if (!Objects.equals(this, o.sType())) {
                    throw new IllegalStateException(":/");
                }
                return (TypedOptic<A, ?, FT, FR>) o;
            });
            /*final Either<Pair<TypeTemplate, FieldOptic<?, ?, FT, FR>>, FieldNotFoundException> newTemplate = family.template().findField(family, index, name, type, resultType);
            return newTemplate.mapLeft(nc -> {
                final TypeFamily.Mu newMu = new TypeFamily.Mu(family.name, nc.getLeft());
                final MuType<?> newType = newMu.apply(index);
                if (!Objects.equals(delegate.get(), nc.getRight().sType())) {
                    throw new IllegalStateException(":/");
                }
                return cap(newType, type, resultType, (FieldOptic<A, ?, FT, FR>) nc.getRight());
            });*/
        }

        /*private <B, FT, FR> FieldOptic<A, ?, FT, FR> cap(final MuType<B> newType, final Type<FT> type, final Type<FR> resultType, final FieldOptic<A, ?, FT, FR> optic) {
            final Type<B> newTypeType = newType.delegate.get();

            final Lenses.Traversal<A, ?, FT, FR> traversal = Lenses.toTraversal(optic.upCast(Lenses.TraversalP.Mu.TYPE_TOKEN).orElseThrow(IllegalArgumentException::new));

            // FIXME
            return new FieldOptic<A, B, FT, FR>(
                Lenses.TraversalP.Mu.TYPE_TOKEN,
                MuType.this,
                newType,
                type,
                resultType,
                new Lenses.Traversal<A, B, FT, FR>() {
                    @Override
                    public <F extends K1> Lenses.FunctionType<A, App<F, B>> wander(final Applicative<F> applicative, final Lenses.FunctionType<FT, App<F, FR>> input) {
                        return traversal.wander(applicative, input);
                    }
                }
            );
        }*/

        @Override
        public String toString() {
            return "MuType[" + family.name() + "_" + index + "]";
        }

        @Override
        public boolean equals(final Object obj, final boolean ignoreRecursionPoints, final boolean checkIndex) {
            if (!(obj instanceof RecursivePointType)) {
                return false;
            }
            final RecursivePointType<?> type = (RecursivePointType<?>) obj;
            return (ignoreRecursionPoints || Objects.equals(family, type.family)) && index == type.index;
        }

        @Override
        public int hashCode() {
            int result = family.hashCode();
            result = 31 * result + index;
            return result;
        }

        public View<A, A> in() {
            return View.create(Functions.in(this));
        }

        public View<A, A> out() {
            return View.create(Functions.out(this));
        }
    }
}
