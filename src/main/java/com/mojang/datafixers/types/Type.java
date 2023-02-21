// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FieldFinder;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.Functions;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class Type<A> implements App<Type.Mu, A> {
    private static final Map<Triple<Type<?>, TypeRewriteRule, PointFreeRule>, CompletableFuture<Optional<? extends RewriteResult<?, ?>>>> PENDING_REWRITE_CACHE = Maps.newConcurrentMap();
    private static final Map<Triple<Type<?>, TypeRewriteRule, PointFreeRule>, Optional<? extends RewriteResult<?, ?>>> REWRITE_CACHE = Maps.newConcurrentMap();

    public static class Mu implements K1 {}

    public static <A> Type<A> unbox(final App<Mu, A> box) {
        return (Type<A>) box;
    }

    @Nullable
    private TypeTemplate template;

    @Nullable
    private Codec<A> codec;

    public RewriteResult<A, ?> rewriteOrNop(final TypeRewriteRule rule) {
        return DataFixUtils.orElseGet(rule.rewrite(this), () -> RewriteResult.nop(this));
    }

    @SuppressWarnings("unchecked")
    public static <S, T, A, B> RewriteResult<S, T> opticView(final Type<S> type, final RewriteResult<A, B> view, final TypedOptic<S, T, A, B> optic) {
        if (view.view().isNop()) {
            return (RewriteResult<S, T>) RewriteResult.nop(type);
        }
        // copy the recData, since optic doesn't touch more than the nested view
        return RewriteResult.create(View.create(
            Functions.app(
                Functions.profunctorTransformer(optic),
                view.view().function()
            )), view.recData());
    }

    /**
     * gmapT
     * run rule on all direct children and combine results
     */
    public RewriteResult<A, ?> all(final TypeRewriteRule rule, final boolean recurse, final boolean checkIndex) {
        return RewriteResult.nop(this);
    }

    /**
     * run rule on exactly one child
     */
    public Optional<RewriteResult<A, ?>> one(final TypeRewriteRule rule) {
        return Optional.empty();
    }

    public Optional<RewriteResult<A, ?>> everywhere(final TypeRewriteRule rule, final PointFreeRule optimizationRule, final boolean recurse, final boolean checkIndex) {
        final TypeRewriteRule rule2 = TypeRewriteRule.seq(TypeRewriteRule.orElse(rule, TypeRewriteRule::nop), TypeRewriteRule.all(TypeRewriteRule.everywhere(rule, optimizationRule, recurse, checkIndex), recurse, checkIndex));
        return rewrite(rule2, optimizationRule);
    }

    public Type<?> updateMu(final RecursiveTypeFamily newFamily) {
        return this;
    }

    public TypeTemplate template() {
        if (template == null) {
            template = buildTemplate();
        }
        return template;
    }

    public abstract TypeTemplate buildTemplate();

    public Optional<TaggedChoice.TaggedChoiceType<?>> findChoiceType(final String name, final int index) {
        return Optional.empty();
    }

    public Optional<Type<?>> findCheckedType(final int index) {
        return Optional.empty();
    }

    public final <T> DataResult<Pair<A, Dynamic<T>>> read(final Dynamic<T> input) {
        return codec().decode(input.getOps(), input.getValue()).map(v -> v.mapSecond(t -> new Dynamic<>(input.getOps(), t)));
    }

    public final Codec<A> codec() {
        if (codec == null) {
            codec = buildCodec();
        }
        return codec;
    }

    protected abstract Codec<A> buildCodec();

    public final <T> DataResult<T> write(final DynamicOps<T> ops, final A value) {
        return codec().encode(value, ops, ops.empty());
    }

    public final <T> DataResult<Dynamic<T>> writeDynamic(final DynamicOps<T> ops, final A value) {
        return write(ops, value).map(result -> new Dynamic<>(ops, result));
    }

    public <T> DataResult<Pair<Typed<A>, T>> readTyped(final Dynamic<T> input) {
        return readTyped(input.getOps(), input.getValue());
    }

    public <T> DataResult<Pair<Typed<A>, T>> readTyped(final DynamicOps<T> ops, final T input) {
        return codec().decode(ops, input).map(vo -> vo.mapFirst(v -> new Typed<>(this, ops, v)));
    }

    public <T> DataResult<Pair<Optional<?>, T>> read(final DynamicOps<T> ops, final TypeRewriteRule rule, final PointFreeRule fRule, final T input) {
        return codec().decode(ops, input).map(vo -> vo.mapFirst(v ->
            rewrite(rule, fRule).map(r -> r.view().function().evalCached().apply(ops).apply(v)
            )
        ));
    }

    public <T> DataResult<T> readAndWrite(final DynamicOps<T> ops, final Type<?> expectedType, final TypeRewriteRule rule, final PointFreeRule fRule, final T input) {
        final Optional<RewriteResult<A, ?>> rewriteResult = rewrite(rule, fRule);
        if (!rewriteResult.isPresent()) {
            return DataResult.error(() -> "Could not build a rewrite rule: " + rule + " " + fRule, input);
        }
        final View<A, ?> view = rewriteResult.get().view();
        if (view.isNop()) {
            return DataResult.success(input);
        }

        return codec().decode(ops, input).flatMap(pair ->
            capWrite(ops, expectedType, pair.getSecond(), pair.getFirst(), view)
        );
    }

    private <T, B> DataResult<T> capWrite(final DynamicOps<T> ops, final Type<?> expectedType, final T rest, final A value, final View<A, B> f) {
        if (!expectedType.equals(f.newType(), true, true)) {
            return DataResult.error(() -> "Rewritten type doesn't match");
        }
        return f.newType().codec().encode(f.function().evalCached().apply(ops).apply(value), ops, rest);
    }

    @SuppressWarnings("unchecked")
    public Optional<RewriteResult<A, ?>> rewrite(final TypeRewriteRule rule, final PointFreeRule fRule) {
        final Triple<Type<?>, TypeRewriteRule, PointFreeRule> key = Triple.of(this, rule, fRule);
        // This code under contention would generate multiple rewrites, so we use CompletableFuture for pending rewrites.
        // We can not use computeIfAbsent because this is a recursive call that will block server startup
        // during the Bootstrap phrase that's trying to pre cache these rewrites.
        final Optional<? extends RewriteResult<?, ?>> rewrite = REWRITE_CACHE.get(key);
        if (rewrite != null) {
            return (Optional<RewriteResult<A, ?>>) rewrite;
        }
        // TODO: AtomicReference.getPlain/setPlain in java9+
        final MutableObject<CompletableFuture<Optional<? extends RewriteResult<?, ?>>>> ref = new MutableObject<>();

        final CompletableFuture<Optional<? extends RewriteResult<?, ?>>> pending = PENDING_REWRITE_CACHE.computeIfAbsent(key, k -> {
            final CompletableFuture<Optional<? extends RewriteResult<?, ?>>> value = new CompletableFuture<>();
            ref.setValue(value);
            return value;
        });

        if (ref.getValue() != null) {
            Optional<RewriteResult<A, ?>> result = rule.rewrite(this).flatMap(r -> r.view().rewrite(fRule).map(view -> RewriteResult.create(view, r.recData())));
            REWRITE_CACHE.put(key, result);
            pending.complete(result);
            PENDING_REWRITE_CACHE.remove(key);
            return result;
        }
        return (Optional<RewriteResult<A, ?>>) pending.join();
    }

    public <FT, FR> Type<?> getSetType(final OpticFinder<FT> optic, final Type<FR> newType) {
        return optic.findType(this, newType, false).orThrow().tType();
    }

    public interface TypeMatcher<FT, FR> {
        <S> Either<TypedOptic<S, ?, FT, FR>, FieldNotFoundException> match(final Type<S> targetType);
    }

    // TODO: unify with findType eventually
    public Optional<Type<?>> findFieldTypeOpt(final String name) {
        return Optional.empty();
    }

    public Type<?> findFieldType(final String name) {
        return findFieldTypeOpt(name).orElseThrow(() -> new IllegalArgumentException("Field not found: " + name));
    }

    public OpticFinder<?> findField(final String name) {
        return new FieldFinder<>(name, findFieldType(name));
    }

    /**
     * populate with the default value, if possible
     * only initializes empty things
     */
    public Optional<A> point(final DynamicOps<?> ops) {
        return Optional.empty();
    }

    public Optional<Typed<A>> pointTyped(final DynamicOps<?> ops) {
        return point(ops).map(value -> new Typed<>(this, ops, value));
    }

    public <FT, FR> Either<TypedOptic<A, ?, FT, FR>, FieldNotFoundException> findTypeCached(final Type<FT> type, final Type<FR> resultType, final TypeMatcher<FT, FR> matcher, final boolean recurse) {
        return findType(type, resultType, matcher, recurse);
    }

    public <FT, FR> Either<TypedOptic<A, ?, FT, FR>, FieldNotFoundException> findType(final Type<FT> type, final Type<FR> resultType, final TypeMatcher<FT, FR> matcher, final boolean recurse) {
        return matcher.match(this).map(Either::left, r -> {
            if (r instanceof Continue) {
                return findTypeInChildren(type, resultType, matcher, recurse);
            }
            return Either.right(r);
        });
    }

    public <FT, FR> Either<TypedOptic<A, ?, FT, FR>, FieldNotFoundException> findTypeInChildren(final Type<FT> type, final Type<FR> resultType, final TypeMatcher<FT, FR> matcher, final boolean recurse) {
        return Either.right(new FieldNotFoundException("No more children"));
    }

    public OpticFinder<A> finder() {
        return DSL.typeFinder(this);
    }

    public <B> Optional<A> ifSame(final Typed<B> value) {
        return ifSame(value.getType(), value.getValue());
    }

    @SuppressWarnings("unchecked")
    public <B> Optional<A> ifSame(final Type<B> type, final B value) {
        if (equals(type, true, true)) {
            return Optional.of((A) value);
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <B> Optional<RewriteResult<A, ?>> ifSame(final Type<B> type, final RewriteResult<B, ?> value) {
        if (equals(type, true, true)) {
            return Optional.of((RewriteResult<A, ?>) value);
        }
        return Optional.empty();
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        return equals(o, false, true);
    }

    public abstract boolean equals(final Object o, final boolean ignoreRecursionPoints, final boolean checkIndex);

    public abstract static class TypeError {
        private final String message;

        public TypeError(final String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }
    }

    public static class FieldNotFoundException extends TypeError {
        public FieldNotFoundException(final String message) {
            super(message);
        }
    }

    public static final class Continue extends FieldNotFoundException {
        public Continue() {
            super("Continue");
        }
    }
}
