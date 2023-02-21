// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.InjTagged;
import com.mojang.datafixers.optics.Optic;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.optics.profunctors.Cocartesian;
import com.mojang.datafixers.optics.profunctors.Profunctor;
import com.mojang.datafixers.optics.profunctors.TraversalP;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public record TypedOptic<S, T, A, B>(Set<TypeToken<? extends K1>> bounds, List<? extends Element<?, ?, ?, ?>> elements) {
    public TypedOptic(final TypeToken<? extends K1> proofBound, final Type<S> sType, final Type<T> tType, final Type<A> aType, final Type<B> bType, final Optic<?, S, T, A, B> optic) {
        this(ImmutableSet.of(proofBound), sType, tType, aType, bType, optic);
    }

    public TypedOptic(final Set<TypeToken<? extends K1>> proofBounds, final Type<S> sType, final Type<T> tType, final Type<A> aType, final Type<B> bType, final Optic<?, S, T, A, B> optic) {
        this(proofBounds, List.of(new Element<>(sType, tType, aType, bType, optic)));
    }

    public <P extends K2, Proof2 extends K1> App2<P, S, T> apply(final TypeToken<Proof2> token, final App<Proof2, P> proof, final App2<P, A, B> argument) {
        return upCast(token)
            .orElseThrow(() -> {
                return new IllegalArgumentException("Couldn't upcast");
            })
            .eval(proof)
            .apply(argument);
    }

    public Optic<?, S, T, ?, ?> outermost() {
        return outermostElement().optic();
    }

    public Optic<?, ?, ?, A, B> innermost() {
        return innermostElement().optic();
    }

    @SuppressWarnings("unchecked")
    private Element<S, T, ?, ?> outermostElement() {
        return (Element<S, T, ?, ?>) elements.get(0);
    }

    @SuppressWarnings("unchecked")
    private Element<?, ?, A, B> innermostElement() {
        return (Element<?, ?, A, B>) elements.get(elements.size() - 1);
    }

    public Type<S> sType() {
        return outermostElement().sType();
    }

    public Type<T> tType() {
        return outermostElement().tType();
    }

    public Type<A> aType() {
        return innermostElement().aType();
    }

    public Type<B> bType() {
        return innermostElement().bType();
    }

    public <A1, B1> TypedOptic<S, T, A1, B1> compose(final TypedOptic<A, B, A1, B1> other) {
        final ImmutableSet.Builder<TypeToken<? extends K1>> proof = ImmutableSet.builder();
        proof.addAll(bounds);
        proof.addAll(other.bounds);
        final ImmutableList.Builder<Element<?, ?, ?, ?>> elements = ImmutableList.builderWithExpectedSize(elements().size() + other.elements().size());
        elements.addAll(elements());
        elements.addAll(other.elements());
        return new TypedOptic<>(proof.build(), elements.build());
    }

    @SuppressWarnings("unchecked")
    public <Proof2 extends K1> Optional<Optic<? super Proof2, S, T, A, B>> upCast(final TypeToken<Proof2> proof) {
        if (instanceOf(bounds, proof)) {
            if (elements.size() == 1) {
                return Optional.of((Optic<? super Proof2, S, T, A, B>) elements.get(0).optic());
            }
            final List<Optic<? super Proof2, ?, ?, ?, ?>> optics = elements.stream().map(e -> (Optic<? super Proof2, ?, ?, ?, ?>) e.optic()).collect(Collectors.toList());
            return Optional.of(new Optic.CompositionOptic<>(optics));
        }
        return Optional.empty();
    }

    public static <Proof2 extends K1> boolean instanceOf(final Collection<TypeToken<? extends K1>> bounds, final TypeToken<Proof2> proof) {
        return bounds.stream().allMatch(bound -> bound.isSupertypeOf(proof));
    }

    public static <S, T> TypedOptic<S, T, S, T> adapter(final Type<S> sType, final Type<T> tType) {
        return new TypedOptic<>(
            Profunctor.Mu.TYPE_TOKEN,
            sType,
            tType,
            sType,
            tType,
            Optics.id()
        );
    }

    public static <F, G, F2> TypedOptic<Pair<F, G>, Pair<F2, G>, F, F2> proj1(final Type<F> fType, final Type<G> gType, final Type<F2> newType) {
        return new TypedOptic<>(
            Cartesian.Mu.TYPE_TOKEN,
            DSL.and(fType, gType),
            DSL.and(newType, gType),
            fType,
            newType,
            Optics.proj1()
        );
    }

    public static <F, G, G2> TypedOptic<Pair<F, G>, Pair<F, G2>, G, G2> proj2(final Type<F> fType, final Type<G> gType, final Type<G2> newType) {
        return new TypedOptic<>(
            Cartesian.Mu.TYPE_TOKEN,
            DSL.and(fType, gType),
            DSL.and(fType, newType),
            gType,
            newType,
            Optics.proj2()
        );
    }

    public static <F, G, F2> TypedOptic<Either<F, G>, Either<F2, G>, F, F2> inj1(final Type<F> fType, final Type<G> gType, final Type<F2> newType) {
        return new TypedOptic<>(
            Cocartesian.Mu.TYPE_TOKEN,
            DSL.or(fType, gType),
            DSL.or(newType, gType),
            fType,
            newType,
            Optics.inj1()
        );
    }

    public static <F, G, G2> TypedOptic<Either<F, G>, Either<F, G2>, G, G2> inj2(final Type<F> fType, final Type<G> gType, final Type<G2> newType) {
        return new TypedOptic<>(
            Cocartesian.Mu.TYPE_TOKEN,
            DSL.or(fType, gType),
            DSL.or(fType, newType),
            gType,
            newType,
            Optics.inj2()
        );
    }

    public static <K, V, K2> TypedOptic<List<Pair<K, V>>, List<Pair<K2, V>>, K, K2> compoundListKeys(final Type<K> aType, final Type<K2> bType, final Type<V> valueType) {
        return new TypedOptic<>(
            TraversalP.Mu.TYPE_TOKEN,
            DSL.compoundList(aType, valueType),
            DSL.compoundList(bType, valueType),
            DSL.and(aType, valueType),
            DSL.and(bType, valueType),
            Optics.listTraversal()
        ).compose(new TypedOptic<>(
            TraversalP.Mu.TYPE_TOKEN,
            DSL.and(aType, valueType),
            DSL.and(bType, valueType),
            aType,
            bType,
            Optics.proj1()
        ));
    }

    public static <K, V, V2> TypedOptic<List<Pair<K, V>>, List<Pair<K, V2>>, V, V2> compoundListElements(final Type<K> keyType, final Type<V> aType, final Type<V2> bType) {
        return new TypedOptic<>(
            TraversalP.Mu.TYPE_TOKEN,
            DSL.compoundList(keyType, aType),
            DSL.compoundList(keyType, bType),
            DSL.and(keyType, aType),
            DSL.and(keyType, bType),
            Optics.listTraversal()
        ).compose(new TypedOptic<>(
            TraversalP.Mu.TYPE_TOKEN,
            DSL.and(keyType, aType),
            DSL.and(keyType, bType),
            aType,
            bType,
            Optics.proj2()
        ));
    }

    public static <A, B> TypedOptic<List<A>, List<B>, A, B> list(final Type<A> aType, final Type<B> bType) {
        return new TypedOptic<>(
            TraversalP.Mu.TYPE_TOKEN,
            DSL.list(aType),
            DSL.list(bType),
            aType,
            bType,
            Optics.listTraversal()
        );
    }

    public static <K, A, B> TypedOptic<Pair<K, ?>, Pair<K, ?>, A, B> tagged(final TaggedChoice.TaggedChoiceType<K> sType, final K key, final Type<A> aType, final Type<B> bType) {
        return new TypedOptic<>(
            Cocartesian.Mu.TYPE_TOKEN,
            sType,
            replaceTagged(sType, key, aType, bType),
            aType,
            bType,
            new InjTagged<>(key)
        );
    }

    private static <K, A, B> Type<Pair<K, ?>> replaceTagged(final TaggedChoice.TaggedChoiceType<K> sType, final K key, final Type<A> aType, final Type<B> bType) {
        if (Objects.equals(aType, bType)) {
            return sType;
        }
        if (!Objects.equals(sType.types().get(key), aType)) {
            throw new IllegalArgumentException("Focused type doesn't match.");
        }
        final Map<K, Type<?>> newTypes = Maps.newHashMap(sType.types());
        newTypes.put(key, bType);
        return DSL.taggedChoiceType(sType.getName(), sType.getKeyType(), newTypes);
    }

    public TypedOptic<S, T, A, B> castOuter(final Type<S> sType, final Type<T> tType) {
        return castOuterUnchecked(sType, tType);
    }

    public <S2, T2> TypedOptic<S2, T2, A, B> castOuterUnchecked(final Type<S2> sType, final Type<T2> tType) {
        final List<Element<?, ?, ?, ?>> newElements = new ArrayList<>(elements);
        newElements.set(0, newElements.get(0).castOuterUnchecked(sType, tType));
        return new TypedOptic<>(bounds, newElements);
    }

    @Override
    public String toString() {
        return "(" + elements.stream().map(Object::toString).collect(Collectors.joining(" \u25E6 ")) + ")";
    }

    public record Element<S, T, A, B>(
        Type<S> sType,
        Type<T> tType,
        Type<A> aType,
        Type<B> bType,
        Optic<?, S, T, A, B> optic
    ) {
        @SuppressWarnings("unchecked")
        public <S2, T2> Element<S2, T2, A, B> castOuterUnchecked(final Type<S2> sType, final Type<T2> tType) {
            return new Element<>(sType, tType, aType, bType, (Optic<?, S2, T2, A, B>) optic);
        }

        @Override
        public String toString() {
            return optic.toString();
        }
    }
}
