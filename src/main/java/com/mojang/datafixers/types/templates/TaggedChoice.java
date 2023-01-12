// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.templates;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.Functions;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.optics.Affine;
import com.mojang.datafixers.optics.Lens;
import com.mojang.datafixers.optics.Optic;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.Traversal;
import com.mojang.datafixers.optics.profunctors.AffineP;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.optics.profunctors.TraversalP;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class TaggedChoice<K> implements TypeTemplate {
    private final String name;
    private final Type<K> keyType;
    private final Object2ObjectMap<K, TypeTemplate> templates;
    private final Map<Pair<TypeFamily, Integer>, Type<?>> types = Maps.newConcurrentMap();
    private final int size;

    public TaggedChoice(final String name, final Type<K> keyType, final Object2ObjectMap<K, TypeTemplate> templates) {
        this.name = name;
        this.keyType = keyType;
        this.templates = templates;
        size = templates.values().stream().mapToInt(TypeTemplate::size).max().orElse(0);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public TypeFamily apply(final TypeFamily family) {
        return index -> types.computeIfAbsent(Pair.of(family, index), key -> {
            final Object2ObjectMap<K, Type<?>> types = new Object2ObjectOpenHashMap<>(templates.size());
            for (final Map.Entry<K, TypeTemplate> entry : Object2ObjectMaps.fastIterable(templates)) {
                types.put(entry.getKey(), entry.getValue().apply(key.getFirst()).apply(key.getSecond()));
            }
            return DSL.taggedChoiceType(name, keyType, types);
        });
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(final FamilyOptic<A, B> input, final Type<A> aType, final Type<B> bType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A, B> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(final int index, @Nullable final String name, final Type<A> type, final Type<B> resultType) {
        return Either.right(new Type.FieldNotFoundException("Not implemented"));
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(final TypeFamily family, final IntFunction<RewriteResult<?, ?>> function) {
        return index -> {
            RewriteResult<Pair<K, ?>, Pair<K, ?>> result = RewriteResult.nop((TaggedChoiceType<K>) apply(family).apply(index));
            for (final Map.Entry<K, TypeTemplate> entry : templates.entrySet()) {
                final RewriteResult<?, ?> elementResult = entry.getValue().hmap(family, function).apply(index);
                result = TaggedChoiceType.elementResult(entry.getKey(), (TaggedChoiceType<K>) result.view().newType(), elementResult).compose(result);
            }
            return result;
        };
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TaggedChoice)) {
            return false;
        }
        final TaggedChoice<?> other = (TaggedChoice<?>) obj;
        return Objects.equals(name, other.name) && Objects.equals(keyType, other.keyType) && Objects.equals(templates, other.templates);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + keyType.hashCode();
        result = 31 * result + templates.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TaggedChoice[" + name + ", " + Joiner.on(", ").withKeyValueSeparator(" -> ").join(templates) + "]";
    }

    public static final class TaggedChoiceType<K> extends Type<Pair<K, ?>> {
        private final String name;
        private final Type<K> keyType;
        protected final Object2ObjectMap<K, Type<?>> types;
        private final int hashCode;

        public TaggedChoiceType(final String name, final Type<K> keyType, final Object2ObjectMap<K, Type<?>> types) {
            this.name = name;
            this.keyType = keyType;
            this.types = types;
            hashCode = Objects.hash(name, keyType, types);
        }

        @Override
        public RewriteResult<Pair<K, ?>, ?> all(final TypeRewriteRule rule, final boolean recurse, final boolean checkIndex) {
            final Object2ObjectMap<K, RewriteResult<?, ?>> results = new Object2ObjectOpenHashMap<>(types.size());
            for (final Map.Entry<K, Type<?>> entry : Object2ObjectMaps.fastIterable(types)) {
                final Optional<? extends RewriteResult<?, ?>> result = rule.rewrite(entry.getValue());
                if (result.isPresent() && !result.get().view().isNop()) {
                    results.put(entry.getKey(), result.get());
                }
            }

            if (results.isEmpty()) {
                return RewriteResult.nop(this);
            } else if (results.size() == 1) {
                final Map.Entry<K, ? extends RewriteResult<?, ?>> entry = results.entrySet().iterator().next();
                return elementResult(entry.getKey(), this, entry.getValue());
            }
            final Object2ObjectMap<K, Type<?>> newTypes = new Object2ObjectOpenHashMap<>(types);
            final BitSet recData = new BitSet();
            for (final Map.Entry<K, ? extends RewriteResult<?, ?>> entry : Object2ObjectMaps.fastIterable(results)) {
                newTypes.put(entry.getKey(), entry.getValue().view().newType());
                recData.or(entry.getValue().recData());
            }
            return RewriteResult.create(View.create(Functions.fun("TaggedChoiceTypeRewriteResult " + results.size(), new RewriteFunc<>(results), this, DSL.taggedChoiceType(name, keyType, newTypes))), recData);
        }

        public static <K, FT, FR> RewriteResult<Pair<K, ?>, Pair<K, ?>> elementResult(final K key, final TaggedChoiceType<K> type, final RewriteResult<FT, FR> result) {
            return opticView(type, result, TypedOptic.tagged(type, key, result.view().type(), result.view().newType()));
        }

        @Override
        public Optional<RewriteResult<Pair<K, ?>, ?>> one(final TypeRewriteRule rule) {
            for (final Map.Entry<K, Type<?>> entry : types.entrySet()) {
                final Optional<? extends RewriteResult<?, ?>> elementResult = rule.rewrite(entry.getValue());
                if (elementResult.isPresent()) {
                    return Optional.of(elementResult(entry.getKey(), this, elementResult.get()));
                }
            }
            return Optional.empty();
        }

        @Override
        public Type<?> updateMu(final RecursiveTypeFamily newFamily) {
            final Object2ObjectMap<K, Type<?>> newTypes = new Object2ObjectOpenHashMap<>(types.size());
            for (final Object2ObjectMap.Entry<K, Type<?>> entry : Object2ObjectMaps.fastIterable(types)) {
                newTypes.put(entry.getKey(), entry.getValue().updateMu(newFamily));
            }
            return DSL.taggedChoiceType(name, keyType, newTypes);
        }

        @Override
        public TypeTemplate buildTemplate() {
            final Object2ObjectMap<K, TypeTemplate> templates = new Object2ObjectOpenHashMap<>(types.size());
            for (final Object2ObjectMap.Entry<K, Type<?>> entry : Object2ObjectMaps.fastIterable(types)) {
                templates.put(entry.getKey(), entry.getValue().template());
            }
            return DSL.taggedChoice(name, keyType, templates);
        }

        @SuppressWarnings("unchecked")
        private <V> DataResult<? extends Encoder<Pair<K, ?>>> encoder(final Pair<K, V> pair) {
            return getCodec(pair.getFirst()).map(c -> ((Encoder<V>) c).comap(p -> (V) p.getSecond()));
        }

        @Override
        protected Codec<Pair<K, ?>> buildCodec() {
            return KeyDispatchCodec.<K, Pair<K, ?>>unsafe(
                name,
                keyType.codec(),
                p -> DataResult.success(p.getFirst()),
                k -> getCodec(k).map(c -> c.map(v -> Pair.of(k, v))),
                this::encoder
            ).codec();
        }

        private DataResult<? extends Codec<?>> getCodec(final K k) {
            return Optional.ofNullable(types.get(k)).map(t -> DataResult.success(t.codec())).orElseGet(() -> DataResult.error(() -> "Unsupported key: " + k));
        }

        @Override
        public Optional<Type<?>> findFieldTypeOpt(final String name) {
            return types.values().stream().map(t -> t.findFieldTypeOpt(name)).filter(Optional::isPresent).findFirst().flatMap(Function.identity());
        }

        @Override
        public Optional<Pair<K, ?>> point(final DynamicOps<?> ops) {
            return types.entrySet().stream().map(e -> e.getValue().point(ops).map(value -> Pair.of(e.getKey(), value))).filter(Optional::isPresent).findFirst().flatMap(Function.identity()).map(p -> p);
        }

        public Optional<Typed<Pair<K, ?>>> point(final DynamicOps<?> ops, final K key, final Object value) {
            if (!types.containsKey(key)) {
                return Optional.empty();
            }
            return Optional.of(new Typed<>(this, ops, Pair.of(key, value)));
        }

        @Override
        public <FT, FR> Either<TypedOptic<Pair<K, ?>, ?, FT, FR>, FieldNotFoundException> findTypeInChildren(final Type<FT> type, final Type<FR> resultType, final TypeMatcher<FT, FR> matcher, final boolean recurse) {
            final Map<K, ? extends TypedOptic<?, ?, FT, FR>> optics = types.entrySet().stream()
                .map(e -> Pair.of(e.getKey(), e.getValue().findType(type, resultType, matcher, recurse)))
                .filter(e -> e.getSecond().left().isPresent())
                .map(e -> e.mapSecond(o -> o.left().get()))
                .collect(Pair.toMap())
                ;

            if (optics.isEmpty()) {
                return Either.right(new FieldNotFoundException("Not found in any choices"));
            } else if (optics.size() == 1) {
                final Map.Entry<K, ? extends TypedOptic<?, ?, FT, FR>> entry = optics.entrySet().iterator().next();
                return Either.left(cap(this, entry.getKey(), entry.getValue()));
            } else {
                final Set<TypeToken<? extends K1>> bounds = Sets.newHashSet();
                optics.values().forEach(o -> bounds.addAll(o.bounds()));

                final Optic<?, Pair<K, ?>, Pair<K, ?>, FT, FR> optic;
                final TypeToken<? extends K1> bound;

                // TODO: cache casts

                if (TypedOptic.instanceOf(bounds, Cartesian.Mu.TYPE_TOKEN) && optics.size() == types.size()) {
                    bound = Cartesian.Mu.TYPE_TOKEN;

                    optic = new Lens<Pair<K, ?>, Pair<K, ?>, FT, FR>() {
                        @Override
                        public FT view(final Pair<K, ?> s) {
                            final TypedOptic<?, ?, FT, FR> optic = optics.get(s.getFirst());
                            return capView(s, optic);
                        }

                        @SuppressWarnings("unchecked")
                        private <S, T> FT capView(final Pair<K, ?> s, final TypedOptic<S, T, FT, FR> optic) {
                            return Optics.toLens(optic.upCast(Cartesian.Mu.TYPE_TOKEN).orElseThrow(IllegalArgumentException::new)).view((S) s.getSecond());
                        }

                        @Override
                        public Pair<K, ?> update(final FR b, final Pair<K, ?> s) {
                            final TypedOptic<?, ?, FT, FR> optic = optics.get(s.getFirst());
                            return capUpdate(b, s, optic);
                        }

                        @SuppressWarnings("unchecked")
                        private <S, T> Pair<K, ?> capUpdate(final FR b, final Pair<K, ?> s, final TypedOptic<S, T, FT, FR> optic) {
                            return Pair.of(s.getFirst(), Optics.toLens(optic.upCast(Cartesian.Mu.TYPE_TOKEN).orElseThrow(IllegalArgumentException::new)).update(b, (S) s.getSecond()));
                        }
                    };
                } else if (TypedOptic.instanceOf(bounds, AffineP.Mu.TYPE_TOKEN)) {
                    bound = AffineP.Mu.TYPE_TOKEN;

                    optic = new Affine<Pair<K, ?>, Pair<K, ?>, FT, FR>() {
                        @Override
                        public Either<Pair<K, ?>, FT> preview(final Pair<K, ?> s) {
                            if (!optics.containsKey(s.getFirst())) {
                                return Either.left(s);
                            }
                            final TypedOptic<?, ?, FT, FR> optic = optics.get(s.getFirst());
                            return capPreview(s, optic);
                        }

                        @SuppressWarnings("unchecked")
                        private <S, T> Either<Pair<K, ?>, FT> capPreview(final Pair<K, ?> s, final TypedOptic<S, T, FT, FR> optic) {
                            return Optics.toAffine(optic.upCast(AffineP.Mu.TYPE_TOKEN).orElseThrow(IllegalArgumentException::new)).preview((S) s.getSecond()).mapLeft(t -> (Pair<K, ?>) Pair.of(s.getFirst(), t));
                        }

                        @Override
                        public Pair<K, ?> set(final FR b, final Pair<K, ?> s) {
                            if (!optics.containsKey(s.getFirst())) {
                                return s;
                            }
                            final TypedOptic<?, ?, FT, FR> optic = optics.get(s.getFirst());
                            return capSet(b, s, optic);
                        }

                        @SuppressWarnings("unchecked")
                        private <S, T> Pair<K, ?> capSet(final FR b, final Pair<K, ?> s, final TypedOptic<S, T, FT, FR> optic) {
                            return Pair.of(s.getFirst(), Optics.toAffine(optic.upCast(AffineP.Mu.TYPE_TOKEN).orElseThrow(IllegalArgumentException::new)).set(b, (S) s.getSecond()));
                        }
                    };
                } else if (TypedOptic.instanceOf(bounds, TraversalP.Mu.TYPE_TOKEN)) {
                    bound = TraversalP.Mu.TYPE_TOKEN;

                    optic = new Traversal<Pair<K, ?>, Pair<K, ?>, FT, FR>() {
                        @Override
                        public <F extends K1> FunctionType<Pair<K, ?>, App<F, Pair<K, ?>>> wander(final Applicative<F, ?> applicative, final FunctionType<FT, App<F, FR>> input) {
                            return pair -> {
                                if (!optics.containsKey(pair.getFirst())) {
                                    return applicative.point(pair);
                                }
                                final TypedOptic<?, ?, FT, FR> optic = optics.get(pair.getFirst());
                                return capTraversal(applicative, input, pair, optic);
                            };
                        }

                        @SuppressWarnings("unchecked")
                        private <S, T, F extends K1> App<F, Pair<K, ?>> capTraversal(final Applicative<F, ?> applicative, final FunctionType<FT, App<F, FR>> input, final Pair<K, ?> pair, final TypedOptic<S, T, FT, FR> optic) {
                            final Traversal<S, T, FT, FR> traversal = Optics.toTraversal(optic.upCast(TraversalP.Mu.TYPE_TOKEN).orElseThrow(IllegalArgumentException::new));
                            return applicative.ap(value -> Pair.of(pair.getFirst(), value), traversal.wander(applicative, input).apply((S) pair.getSecond()));
                        }
                    };
                } else {
                    throw new IllegalStateException("Could not merge TaggedChoiceType optics, unknown bound: " + Arrays.toString(bounds.toArray()));
                }

                final Object2ObjectMap<K, Type<?>> newTypes = new Object2ObjectOpenHashMap<>(types);
                for (final Object2ObjectMap.Entry<K, Type<?>> entry : Object2ObjectMaps.fastIterable(newTypes)) {
                    final TypedOptic<?, ?, FT, FR> typeOptic = optics.get(entry.getKey());
                    if (typeOptic != null) {
                        entry.setValue(typeOptic.tType());
                    }
                }

                return Either.left(new TypedOptic<>(
                    bound,
                    this,
                    DSL.taggedChoiceType(name, keyType, newTypes),
                    type,
                    resultType,
                    optic
                ));
            }
        }

        private <S, T, FT, FR> TypedOptic<Pair<K, ?>, Pair<K, ?>, FT, FR> cap(final TaggedChoiceType<K> choiceType, final K key, final TypedOptic<S, T, FT, FR> optic) {
            return TypedOptic.tagged(choiceType, key, optic.sType(), optic.tType()).compose(optic);
        }

        @Override
        public Optional<TaggedChoiceType<?>> findChoiceType(final String name, final int index) {
            if (Objects.equals(name, this.name)) {
                return Optional.of(this);
            }
            return Optional.empty();
        }

        @Override
        public Optional<Type<?>> findCheckedType(final int index) {
            return types.values().stream().map(type -> type.findCheckedType(index)).filter(Optional::isPresent).findFirst().flatMap(Function.identity());
        }

        @Override
        public boolean equals(final Object obj, final boolean ignoreRecursionPoints, final boolean checkIndex) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TaggedChoiceType)) {
                return false;
            }
            final TaggedChoiceType<?> other = (TaggedChoiceType<?>) obj;
            if (!Objects.equals(name, other.name)) {
                return false;
            }
            if (!(keyType.equals(other.keyType, ignoreRecursionPoints, checkIndex))) {
                return false;
            }
            if (types.size() != other.types.size()) {
                return false;
            }
            for (final Map.Entry<K, Type<?>> entry : types.entrySet()) {
                if (!entry.getValue().equals(other.types.get(entry.getKey()), ignoreRecursionPoints, checkIndex)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return "TaggedChoiceType[" + name + ", " + Joiner.on(", \n").withKeyValueSeparator(" -> ").join(types) + "]\n";
        }

        public String getName() {
            return name;
        }

        public Type<K> getKeyType() {
            return keyType;
        }

        public boolean hasType(final K key) {
            return types.containsKey(key);
        }

        public Map<K, Type<?>> types() {
            return types;
        }

        private static final class RewriteFunc<K> implements Function<DynamicOps<?>, Function<Pair<K, ?>, Pair<K, ?>>> {
            private final Map<K, ? extends RewriteResult<?, ?>> results;

            public RewriteFunc(final Map<K, ? extends RewriteResult<?, ?>> results) {
                this.results = results;
            }

            @Override
            public FunctionType<Pair<K, ?>, Pair<K, ?>> apply(final DynamicOps<?> ops) {
                return input -> {
                    final RewriteResult<?, ?> result = results.get(input.getFirst());
                    if (result == null) {
                        return input;
                    }
                    return capRuleApply(ops, input, result);
                };
            }

            @SuppressWarnings("unchecked")
            private <A, B> Pair<K, B> capRuleApply(final DynamicOps<?> ops, final Pair<K, ?> input, final RewriteResult<A, B> result) {
                return input.mapSecond(v -> result.view().function().evalCached().apply(ops).apply((A) v));
            }

            @Override
            public boolean equals(final Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                final RewriteFunc<?> that = (RewriteFunc<?>) o;
                return Objects.equals(results, that.results);
            }

            @Override
            public int hashCode() {
                return results.hashCode();
            }
        }
    }
}
