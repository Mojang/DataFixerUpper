// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.families;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.Functions;
import com.mojang.datafixers.functions.PointFree;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.RecursivePoint;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class RecursiveTypeFamily implements TypeFamily {
    private static final Interner<TypeTemplate> TEMPLATE_INTERNER = Interners.newWeakInterner();

    private final String name;
    private final TypeTemplate template;
    private final int size;

    private final Int2ObjectMap<RecursivePoint.RecursivePointType<?>> types = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());
    private final int hashCode;

    public RecursiveTypeFamily(final String name, final TypeTemplate template) {
        this.name = name;
        this.template = TEMPLATE_INTERNER.intern(template);
        size = template.size();
        hashCode = Objects.hashCode(template);
    }

    @SuppressWarnings("unchecked")
    public <A> RecursivePoint.RecursivePointType<A> buildMuType(final Type<A> newType, @Nullable RecursiveTypeFamily newFamily) {
        if (newFamily == null) {
            // G
            final TypeTemplate newTypeTemplate = newType.template();
            // Mu G
            if (Objects.equals(template, newTypeTemplate)) {
                newFamily = this;
            } else {
                newFamily = new RecursiveTypeFamily("ruled " + name, newTypeTemplate);
            }
        }
        // find index of B in G
        RecursivePoint.RecursivePointType<A> newMuType = null;
        for (int i1 = 0; i1 < newFamily.size; i1++) {
            final RecursivePoint.RecursivePointType<?> type = newFamily.apply(i1);
            final Type<?> unfold = type.unfold();
            if (newType.equals(unfold, true, false)) {
                newMuType = (RecursivePoint.RecursivePointType<A>) type;
                break;
            }
        }
        if (newMuType == null) {
            throw new IllegalStateException("Couldn't determine the new type properly");
        }
        return newMuType;
    }

    public String name() {
        return name;
    }

    public TypeTemplate template() {
        return template;
    }

    public int size() {
        return size;
    }

    /**
     * returns family.apply(index) -> algebra.family.apply(index)
     */
    public IntFunction<RewriteResult<?, ?>> fold(final Algebra algebra, final RecursiveTypeFamily newFamily) {
        return index -> {
            final RewriteResult<?, ?> result = algebra.apply(index);
            // FIXME: is this corrext?
            return RewriteResult.create(View.create(foldUnchecked(this, newFamily, algebra, index)), result.recData());
        };
    }

    @SuppressWarnings("unchecked")
    private static <A, B> PointFree<Function<A, B>> foldUnchecked(final RecursiveTypeFamily family, final RecursiveTypeFamily newFamily, final Algebra algebra, final int index) {
        final RecursivePoint.RecursivePointType<A> type = (RecursivePoint.RecursivePointType<A>) family.apply(index);
        final RecursivePoint.RecursivePointType<B> newType = (RecursivePoint.RecursivePointType<B>) newFamily.apply(index);
        return Functions.fold(type, newType, algebra, index);
    }

    @Override
    public RecursivePoint.RecursivePointType<?> apply(final int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }
        return types.computeIfAbsent(index, i -> new RecursivePoint.RecursivePointType<>(this, i, () -> template.apply(this).apply(i)));
    }

    public <A, B> Either<TypedOptic<?, ?, A, B>, Type.FieldNotFoundException> findType(final int index, final Type<A> aType, final Type<B> bType, final Type.TypeMatcher<A, B> matcher, final boolean recurse) {
        return apply(index).unfold().findType(aType, bType, matcher, false).flatMap(optic -> {
            final TypeTemplate nc = optic.tType().template();
            final List<FamilyOptic<A, B>> fo = Lists.newArrayList();
            final RecursiveTypeFamily newFamily = new RecursiveTypeFamily(name, nc);

            final RecursivePoint.RecursivePointType<?> sType = apply(index);
            final RecursivePoint.RecursivePointType<?> tType = newFamily.apply(index);

            if (recurse) {
                final FamilyOptic<A, B> arg = i -> fo.get(0).apply(i);

                fo.add(template.applyO(arg, aType, bType));
                final TypedOptic<?, ?, A, B> parts = fo.get(0).apply(index);
                return Either.left(parts.castOuterUnchecked(sType, tType));
            } else {
                return mkSimpleOptic(sType, tType, aType, bType, matcher);
            }
        });
    }

    private <S, T, A, B> Either<TypedOptic<?, ?, A, B>, Type.FieldNotFoundException> mkSimpleOptic(final RecursivePoint.RecursivePointType<S> sType, final RecursivePoint.RecursivePointType<T> tType, final Type<A> aType, final Type<B> bType, final Type.TypeMatcher<A, B> matcher) {
        return sType.unfold().findType(aType, bType, matcher, false).mapLeft(o -> o.castOuterUnchecked(sType, tType));
    }

    public Optional<RewriteResult<?, ?>> everywhere(final int index, final TypeRewriteRule rule, final PointFreeRule optimizationRule) {
        final Type<?> sourceType = apply(index).unfold();
        final RewriteResult<?, ?> sourceView = DataFixUtils.orElse(sourceType.everywhere(rule, optimizationRule, false, false), RewriteResult.nop(sourceType));
        final RecursivePoint.RecursivePointType<?> newType = buildMuType(sourceView.view().newType(), null);
        final RecursiveTypeFamily newFamily = newType.family();

        final List<RewriteResult<?, ?>> views = Lists.newArrayList();
        boolean foundAny = false;
        // FB -> B
        for (int i = 0; i < size; i++) {
            final RecursivePoint.RecursivePointType<?> type = apply(i);
            final Type<?> unfold = type.unfold();
            boolean nop1 = true;
            // FB -> GB
            final RewriteResult<?, ?> view = DataFixUtils.orElse(unfold.everywhere(rule, optimizationRule, false, true), RewriteResult.nop(unfold));
            if (!view.view().isNop()) {
                nop1 = false;
            }

            final RecursivePoint.RecursivePointType<?> newMuType = buildMuType(view.view().newType(), newFamily);
            final boolean nop = cap2(views, type, rule, optimizationRule, nop1, view, newMuType);
            foundAny = foundAny || !nop;
        }
        if (!foundAny) {
            return Optional.empty();
        }
        final Algebra algebra = new ListAlgebra("everywhere", views);
        final RewriteResult<?, ?> fold = fold(algebra, newFamily).apply(index);
        return Optional.of(RewriteResult.create(View.create(fold.view().function()), fold.recData()));
    }

    private <A, B> boolean cap2(final List<RewriteResult<?, ?>> views, final RecursivePoint.RecursivePointType<A> type, final TypeRewriteRule rule, final PointFreeRule optimizationRule, boolean nop, RewriteResult<?, ?> view, final RecursivePoint.RecursivePointType<B> newType) {
        // GB -> B
        final RewriteResult<A, B> newView = RewriteResult.create(newType.in(), new BitSet()).compose((RewriteResult<A, B>) view);
        // B -> B
        final Optional<RewriteResult<B, ?>> rewrite = rule.rewrite(newView.view().newType());
        if (rewrite.isPresent() && !rewrite.get().view().isNop()) {
            nop = false;
            view = rewrite.get().compose((RewriteResult<A, B>) newView);
        }
        view = RewriteResult.create(view.view().rewriteOrNop(optimizationRule), view.recData());
        views.add(view);
        return nop;
    }

    @Override
    public String toString() {
        return "Mu[" + name + ", " + size + ", " + template + "]";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecursiveTypeFamily)) {
            return false;
        }
        final RecursiveTypeFamily family = (RecursiveTypeFamily) o;
        return template == family.template;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
