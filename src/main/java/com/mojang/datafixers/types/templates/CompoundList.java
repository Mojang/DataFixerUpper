// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.templates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.OpticParts;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.optics.ListTraversal;
import com.mojang.datafixers.optics.Optic;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.profunctors.TraversalP;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public final class CompoundList implements TypeTemplate {
    private final TypeTemplate key;
    private final TypeTemplate element;

    public CompoundList(final TypeTemplate key, final TypeTemplate element) {
        this.key = key;
        this.element = element;
    }

    @Override
    public int size() {
        return Math.max(key.size(), element.size());
    }

    @Override
    public TypeFamily apply(final TypeFamily family) {
        return index -> DSL.compoundList(key.apply(family).apply(index), element.apply(family).apply(index));
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(final FamilyOptic<A, B> input, final Type<A> aType, final Type<B> bType) {
        return TypeFamily.familyOptic(
            i -> {
                final OpticParts<A, B> optic = element.applyO(input, aType, bType).apply(i);
                final Set<TypeToken<? extends K1>> bounds = Sets.newHashSet(optic.bounds());
                bounds.add(TraversalP.Mu.TYPE_TOKEN);
                return new OpticParts<>(bounds, cap(optic.optic()));
            }
        );
    }

    private <S, T, A, B> Optic<?, ?, ?, A, B> cap(final Optic<?, S, T, A, B> concreteOptic) {
        return new ListTraversal<Pair<String, S>, Pair<String, T>>().compose(Optics.proj2()).composeUnchecked(concreteOptic);
    }

    @Override
    public <FT, FR> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(final int index, @Nullable final String name, final Type<FT> type, final Type<FR> resultType) {
        return element.findFieldOrType(index, name, type, resultType).mapLeft(element1 -> new CompoundList(key, element1));
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(final TypeFamily family, final IntFunction<RewriteResult<?, ?>> function) {
        return i -> {
            final RewriteResult<?, ?> f1 = key.hmap(family, function).apply(i);
            final RewriteResult<?, ?> f2 = element.hmap(family, function).apply(i);
            return cap(apply(family).apply(i), f1, f2);
        };
    }

    private <L, R> RewriteResult<?, ?> cap(final Type<?> type, final RewriteResult<L, ?> f1, final RewriteResult<R, ?> f2) {
        return ((CompoundListType<L, R>) type).mergeViews(f1, f2);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof CompoundList && Objects.equals(element, ((CompoundList) obj).element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element);
    }

    @Override
    public String toString() {
        return "CompoundList[" + element + "]";
    }

    public static final class CompoundListType<K, V> extends Type<List<Pair<K, V>>> {
        protected final Type<K> key;
        protected final Type<V> element;

        public CompoundListType(final Type<K> key, final Type<V> element) {
            this.key = key;
            this.element = element;
        }

        @Override
        public RewriteResult<List<Pair<K, V>>, ?> all(final TypeRewriteRule rule, final boolean recurse, final boolean checkIndex) {
            return mergeViews(key.rewriteOrNop(rule), element.rewriteOrNop(rule));
        }

        public <K2, V2> RewriteResult<List<Pair<K, V>>, ?> mergeViews(final RewriteResult<K, K2> leftView, final RewriteResult<V, V2> rightView) {
            final RewriteResult<List<Pair<K, V>>, List<Pair<K2, V>>> v1 = fixKeys(this, key, element, leftView);
            final RewriteResult<List<Pair<K2, V>>, List<Pair<K2, V2>>> v2 = fixValues(v1.view().newType(), leftView.view().newType(), element, rightView);
            return v2.compose(v1);
        }

        @Override
        public Optional<RewriteResult<List<Pair<K, V>>, ?>> one(final TypeRewriteRule rule) {
            return DataFixUtils.or(
                rule.rewrite(key).map(v -> fixKeys(this, key, element, v)),
                () -> rule.rewrite(element).map(v -> fixValues(this, key, element, v))
            );
        }

        private static <K, V, K2> RewriteResult<List<Pair<K, V>>, List<Pair<K2, V>>> fixKeys(final Type<List<Pair<K, V>>> type, final Type<K> first, final Type<V> second, final RewriteResult<K, K2> view) {
            return opticView(type, view, TypedOptic.compoundListKeys(first, view.view().newType(), second));
        }

        private static <K, V, V2> RewriteResult<List<Pair<K, V>>, List<Pair<K, V2>>> fixValues(final Type<List<Pair<K, V>>> type, final Type<K> first, final Type<V> second, final RewriteResult<V, V2> view) {
            return opticView(type, view, TypedOptic.compoundListElements(first, second, view.view().newType()));
        }

        @Override
        public Type<?> updateMu(final RecursiveTypeFamily newFamily) {
            return DSL.compoundList(key.updateMu(newFamily), element.updateMu(newFamily));
        }

        @Override
        public TypeTemplate buildTemplate() {
            return new CompoundList(key.template(), element.template());
        }

        @Override
        public Optional<List<Pair<K, V>>> point(final DynamicOps<?> ops) {
            return Optional.of(ImmutableList.of());
        }

        @Override
        public <FT, FR> Either<TypedOptic<List<Pair<K, V>>, ?, FT, FR>, FieldNotFoundException> findTypeInChildren(final Type<FT> type, final Type<FR> resultType, final TypeMatcher<FT, FR> matcher, final boolean recurse) {
            final Either<TypedOptic<K, ?, FT, FR>, FieldNotFoundException> firstFieldLens = key.findType(type, resultType, matcher, recurse);
            return firstFieldLens.map(
                this::capLeft,
                r -> {
                    final Either<TypedOptic<V, ?, FT, FR>, FieldNotFoundException> secondFieldLens = element.findType(type, resultType, matcher, recurse);
                    return secondFieldLens.mapLeft(this::capRight);
                }
            );
        }

        private <FT, K2, FR> Either<TypedOptic<List<Pair<K, V>>, ?, FT, FR>, FieldNotFoundException> capLeft(final TypedOptic<K, K2, FT, FR> optic) {
            return Either.left(TypedOptic.compoundListKeys(optic.sType(), optic.tType(), element).compose(optic));
        }

        private <FT, V2, FR> TypedOptic<List<Pair<K, V>>, ?, FT, FR> capRight(final TypedOptic<V, V2, FT, FR> optic) {
            return TypedOptic.compoundListElements(key, optic.sType(), optic.tType()).compose(optic);
        }

        @Override
        public <T> DataResult<Pair<List<Pair<K, V>>, T>> read(final DynamicOps<T> ops, final T input) {
            return ops.getMapValues(input).flatMap(map -> {
                final AtomicReference<DataResult<Pair<ImmutableList.Builder<Pair<K, V>>, ImmutableMap.Builder<T, T>>>> result =
                    new AtomicReference<>(DataResult.success(Pair.of(ImmutableList.builder(), ImmutableMap.builder())));

                map.forEach(entry -> {
                    result.set(result.get().flatMap(pair -> {
                        final DataResult<Pair<K, V>> readEntry = key.read(ops, entry.getFirst()).flatMap(keyValue ->
                            element.read(ops, entry.getSecond()).map(elementValue ->
                                Pair.of(keyValue.getFirst(), elementValue.getFirst())
                            )
                        );
                        readEntry.error().ifPresent(e -> {
                            pair.getSecond().put(entry.getFirst(), entry.getSecond());
                        });
                        return readEntry.map(r -> {
                            pair.getFirst().add(r);
                            return pair;
                        });
                    }));
                });

                return result.get().map(pair -> Pair.of((List<Pair<K, V>>) pair.getFirst().build(), ops.createMap(pair.getSecond().build())));
            });
        }

        @Override
        public <T> DataResult<T> write(final DynamicOps<T> ops, final T rest, final List<Pair<K, V>> value) {
            final Map<T, T> map = Maps.newHashMap();

            DataResult<Map<T, T>> result = DataResult.success(map);

            for (final Pair<K, V> pair : value) {
                result = result.flatMap(m -> {
                    final DataResult<T> element = this.element.write(ops, ops.empty(), pair.getSecond());
                    final DataResult<Pair<T, T>> entry = element.flatMap(e -> key.write(ops, ops.empty(), pair.getFirst()).map(k -> Pair.of(k, e)));
                    return entry.flatMap(e -> {
                        final T key = e.getFirst();
                        if (m.containsKey(key)) {
                            return DataResult.error("Duplicate key: " + key, m);
                        }
                        m.put(key, e.getSecond());
                        return DataResult.success(m);
                    });
                });
            }

            return result.flatMap(m -> ops.mergeInto(rest, m));
        }

        @Override
        public String toString() {
            return "CompoundList[" + key + " -> " + element + "]";
        }

        @Override
        public boolean equals(final Object obj, final boolean ignoreRecursionPoints, final boolean checkIndex) {
            if (!(obj instanceof CompoundListType<?, ?>)) {
                return false;
            }
            final CompoundListType<?, ?> that = (CompoundListType<?, ?>) obj;
            return key.equals(that.key, ignoreRecursionPoints, checkIndex) && element.equals(that.element, ignoreRecursionPoints, checkIndex);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, element);
        }

        public Type<K> getKey() {
            return key;
        }

        public Type<V> getElement() {
            return element;
        }
    }
}
