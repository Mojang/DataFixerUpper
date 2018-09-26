// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.FieldFinder;
import com.mojang.datafixers.FunctionType;
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
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class Type<A> implements App<Type.Mu, A> {
    private static final Map<Triple<Type<?>, TypeRewriteRule, PointFreeRule>, Optional<? extends RewriteResult<?, ?>>> REWRITE_CACHE = Maps.newConcurrentMap();

    public static class Mu implements K1 {}

    public static <A> Type<A> unbox(final App<Mu, A> box) {
        return (Type<A>) box;
    }

    @Nullable
    private TypeTemplate template;

    public RewriteResult<A, ?> rewriteOrNop(final TypeRewriteRule rule) {
        return DataFixUtils.orElseGet(rule.rewrite(this), () -> RewriteResult.nop(this));
    }

    @SuppressWarnings("unchecked")
    public static <S, T, A, B> RewriteResult<S, T> opticView(final Type<S> type, final RewriteResult<A, B> view, final TypedOptic<S, T, A, B> optic) {
        if (Objects.equals(view.view().function(), Functions.id())) {
            return (RewriteResult<S, T>) RewriteResult.nop(type);
        }
        // copy the recData, since optic doesn't touch more than the nested view
        return RewriteResult.create(View.create(
            optic.sType(),
            optic.tType(),
            Functions.app(
                Functions.profunctorTransformer(optic.upCast(FunctionType.Instance.Mu.TYPE_TOKEN).orElseThrow(IllegalArgumentException::new)),
                view.view().function(),
                DSL.func(optic.aType(), view.view().newType())
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

    public final <T> Pair<Dynamic<T>, Optional<A>> read(final Dynamic<T> input) {
        return read(input.getOps(), input.getValue()).mapFirst(v -> new Dynamic<>(input.getOps(), v));
    }

    public abstract <T> Pair<T, Optional<A>> read(final DynamicOps<T> ops, final T input);

    public abstract <T> T write(final DynamicOps<T> ops, final T rest, final A value);

    public final <T> T write(final DynamicOps<T> ops, final A value) {
        return write(ops, ops.empty(), value);
    }

    public final <T> Dynamic<T> writeDynamic(final DynamicOps<T> ops, final T rest, final A value) {
        return new Dynamic<>(ops, write(ops, rest, value));
    }

    public final <T> Dynamic<T> writeDynamic(final DynamicOps<T> ops, final A value) {
        return new Dynamic<>(ops, write(ops, value));
    }

    public <T> Pair<T, Optional<Typed<A>>> readTyped(final Dynamic<T> input) {
        return readTyped(input.getOps(), input.getValue());
    }

    public <T> Pair<T, Optional<Typed<A>>> readTyped(final DynamicOps<T> ops, final T input) {
        return read(ops, input).mapSecond(vo -> vo.map(v -> new Typed<>(this, ops, v)));
    }

    public <T> Pair<T, Optional<?>> read(final DynamicOps<T> ops, final TypeRewriteRule rule, final PointFreeRule fRule, final T input) {
        return read(ops, input).mapSecond(vo -> vo.map(v ->
            rewrite(rule, fRule).map(r -> r.view().function().evalCached().apply(ops).apply(v)
            )
        ));
    }

    public <T> Optional<T> readAndWrite(final DynamicOps<T> ops, final Type<?> expectedType, final TypeRewriteRule rule, final PointFreeRule fRule, final T input) {
        final Pair<T, Optional<A>> po = read(ops, input);
        return po.getSecond().flatMap(v ->
            rewrite(rule, fRule).map(r ->
                capWrite(ops, expectedType, po.getFirst(), v, r.view())
            )
        );
    }

    public <T, B> T capWrite(final DynamicOps<T> ops, final Type<?> expectedType, final T rest, final A value, final View<A, B> f) {
        if (!expectedType.equals(f.newType(), true, true)) {
            throw new IllegalStateException("Rewritten type doesn't match.");
        }
        return f.newType().write(ops, rest, f.function().evalCached().apply(ops).apply(value));
    }

    @SuppressWarnings("unchecked")
    public Optional<RewriteResult<A, ?>> rewrite(final TypeRewriteRule rule, final PointFreeRule fRule) {
        final Triple<Type<?>, TypeRewriteRule, PointFreeRule> key = Triple.of(this, rule, fRule);
        if (!REWRITE_CACHE.containsKey(key)) {
            final Optional<? extends RewriteResult<?, ?>> result = rule.rewrite(this).flatMap(r -> r.view().rewrite(fRule).map(view -> RewriteResult.create(view, r.recData())));
            REWRITE_CACHE.put(key, result);
        }
        return (Optional<RewriteResult<A, ?>>) REWRITE_CACHE.get(key);
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
