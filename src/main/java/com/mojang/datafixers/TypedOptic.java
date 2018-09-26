// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.Inj1;
import com.mojang.datafixers.optics.Inj2;
import com.mojang.datafixers.optics.InjTagged;
import com.mojang.datafixers.optics.ListTraversal;
import com.mojang.datafixers.optics.Optic;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.Proj1;
import com.mojang.datafixers.optics.Proj2;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.optics.profunctors.Cocartesian;
import com.mojang.datafixers.optics.profunctors.Profunctor;
import com.mojang.datafixers.optics.profunctors.TraversalP;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class TypedOptic<S, T, A, B> {
    protected final Set<TypeToken<? extends K1>> proofBounds;
    protected final Type<S> sType;
    protected final Type<T> tType;
    protected final Type<A> aType;
    protected final Type<B> bType;
    private final Optic<?, S, T, A, B> optic;

    public TypedOptic(final TypeToken<? extends K1> proofBound, final Type<S> sType, final Type<T> tType, final Type<A> aType, final Type<B> bType, final Optic<?, S, T, A, B> optic) {
        this(ImmutableSet.of(proofBound), sType, tType, aType, bType, optic);
    }

    public TypedOptic(final Set<TypeToken<? extends K1>> proofBounds, final Type<S> sType, final Type<T> tType, final Type<A> aType, final Type<B> bType, final Optic<?, S, T, A, B> optic) {
        this.proofBounds = proofBounds;
        this.sType = sType;
        this.tType = tType;
        this.aType = aType;
        this.bType = bType;
        this.optic = optic;
    }

    public <P extends K2, Proof2 extends K1> App2<P, S, T> apply(final TypeToken<Proof2> token, final App<Proof2, P> proof, final App2<P, A, B> argument) {
        return upCast(token)
            .orElseThrow(() -> {
                return new IllegalArgumentException("Couldn't upcast");
            })
            .eval(proof)
            .apply(argument);
    }

    public Optic<?, S, T, A, B> optic() {
        return optic;
    }

    public Set<TypeToken<? extends K1>> bounds() {
        return proofBounds;
    }

    public Type<S> sType() {
        return sType;
    }

    public Type<T> tType() {
        return tType;
    }

    public Type<A> aType() {
        return aType;
    }

    public Type<B> bType() {
        return bType;
    }

    public <A1, B1> TypedOptic<S, T, A1, B1> compose(final TypedOptic<A, B, A1, B1> other) {
        final ImmutableSet.Builder<TypeToken<? extends K1>> builder = ImmutableSet.builder();
        builder.addAll(proofBounds);
        builder.addAll(other.proofBounds);
        return new TypedOptic<>(
            builder.build(),
            sType,
            tType,
            other.aType,
            other.bType,
            optic().composeUnchecked(other.optic())
        );
    }

    @SuppressWarnings("unchecked")
    public <Proof2 extends K1> Optional<Optic<? super Proof2, S, T, A, B>> upCast(final TypeToken<Proof2> proof) {
        if (instanceOf(proofBounds, proof)) {
            return Optional.of((Optic<? super Proof2, S, T, A, B>) optic);
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
            new Proj1<>()
        );
    }

    public static <F, G, G2> TypedOptic<Pair<F, G>, Pair<F, G2>, G, G2> proj2(final Type<F> fType, final Type<G> gType, final Type<G2> newType) {
        return new TypedOptic<>(
            Cartesian.Mu.TYPE_TOKEN,
            DSL.and(fType, gType),
            DSL.and(fType, newType),
            gType,
            newType,
            new Proj2<>()
        );
    }

    public static <F, G, F2> TypedOptic<Either<F, G>, Either<F2, G>, F, F2> inj1(final Type<F> fType, final Type<G> gType, final Type<F2> newType) {
        return new TypedOptic<>(
            Cocartesian.Mu.TYPE_TOKEN,
            DSL.or(fType, gType),
            DSL.or(newType, gType),
            fType,
            newType,
            new Inj1<>()
        );
    }

    public static <F, G, G2> TypedOptic<Either<F, G>, Either<F, G2>, G, G2> inj2(final Type<F> fType, final Type<G> gType, final Type<G2> newType) {
        return new TypedOptic<>(
            Cocartesian.Mu.TYPE_TOKEN,
            DSL.or(fType, gType),
            DSL.or(fType, newType),
            gType,
            newType,
            new Inj2<>()
        );
    }

    public static <K, V, K2> TypedOptic<java.util.List<Pair<K, V>>, java.util.List<Pair<K2, V>>, K, K2> compoundListKeys(final Type<K> aType, final Type<K2> bType, final Type<V> valueType) {
        return new TypedOptic<>(
            TraversalP.Mu.TYPE_TOKEN,
            DSL.compoundList(aType, valueType),
            DSL.compoundList(bType, valueType),
            aType,
            bType,
            new ListTraversal<Pair<K, V>, Pair<K2, V>>().compose(Optics.proj1())
        );
    }

    public static <K, V, V2> TypedOptic<java.util.List<Pair<K, V>>, java.util.List<Pair<K, V2>>, V, V2> compoundListElements(final Type<K> keyType, final Type<V> aType, final Type<V2> bType) {
        return new TypedOptic<>(
            TraversalP.Mu.TYPE_TOKEN,
            DSL.compoundList(keyType, aType),
            DSL.compoundList(keyType, bType),
            aType,
            bType,
            new ListTraversal<Pair<K, V>, Pair<K, V2>>().compose(Optics.proj2())
        );
    }

    public static <A, B> TypedOptic<java.util.List<A>, java.util.List<B>, A, B> list(final Type<A> aType, final Type<B> bType) {
        return new TypedOptic<>(
            TraversalP.Mu.TYPE_TOKEN,
            DSL.list(aType),
            DSL.list(bType),
            aType,
            bType,
            new ListTraversal<>()
        );
    }

    public static <K, A, B> TypedOptic<Pair<K, ?>, Pair<K, ?>, A, B> tagged(final TaggedChoice.TaggedChoiceType<K> sType, final K key, final Type<A> aType, final Type<B> bType) {
        if (!Objects.equals(sType.types().get(key), aType)) {
            throw new IllegalArgumentException("Focused type doesn't match.");
        }
        final Map<K, Type<?>> newTypes = Maps.newHashMap(sType.types());
        newTypes.put(key, bType);
        final Type<Pair<K, ?>> pairType = DSL.taggedChoiceType(sType.getName(), sType.getKeyType(), newTypes);
        return new TypedOptic<>(
            Cocartesian.Mu.TYPE_TOKEN,
            sType,
            pairType,
            aType,
            bType,
            new InjTagged<>(key)
        );
    }
}
