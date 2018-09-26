// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types.templates;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.functions.Functions;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;

public final class Hook implements TypeTemplate {
    private final TypeTemplate element;
    private final HookFunction preRead;
    private final HookFunction postWrite;

    public Hook(final TypeTemplate element, final HookFunction preRead, final HookFunction postWrite) {
        this.element = element;
        this.preRead = preRead;
        this.postWrite = postWrite;
    }

    public interface HookFunction {
        HookFunction IDENTITY = new HookFunction() {
            @Override
            public <T> T apply(final DynamicOps<T> ops, final T value) {
                return value;
            }
        };

        <T> T apply(final DynamicOps<T> ops, final T value);
    }

    @Override
    public int size() {
        return element.size();
    }

    @Override
    public TypeFamily apply(final TypeFamily family) {
        return index -> DSL.hook(element.apply(family).apply(index), preRead, postWrite);
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(final FamilyOptic<A, B> input, final Type<A> aType, final Type<B> bType) {
        return TypeFamily.familyOptic(i -> element.applyO(input, aType, bType).apply(i));
    }

    @Override
    public <FT, FR> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(final int index, @Nullable final String name, final Type<FT> type, final Type<FR> resultType) {
        return element.findFieldOrType(index, name, type, resultType);
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(final TypeFamily family, final IntFunction<RewriteResult<?, ?>> function) {
        return index -> {
            final RewriteResult<?, ?> elementResult = element.hmap(family, function).apply(index);
            return cap(family, index, elementResult);
        };
    }

    private <A> RewriteResult<A, ?> cap(final TypeFamily family, final int index, final RewriteResult<A, ?> elementResult) {
        return HookType.fix((HookType<A>) apply(family).apply(index), elementResult);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Hook)) {
            return false;
        }
        final Hook that = (Hook) obj;
        return Objects.equals(element, that.element) && Objects.equals(preRead, that.preRead) && Objects.equals(postWrite, that.postWrite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element, preRead, postWrite);
    }

    @Override
    public String toString() {
        return "Hook[" + element + ", " + preRead + ", " + postWrite + "]";
    }

    public static final class HookType<A> extends Type<A> {
        private final Type<A> delegate;
        private final HookFunction preRead;
        private final HookFunction postWrite;

        public HookType(final Type<A> delegate, final HookFunction preRead, final HookFunction postWrite) {
            this.delegate = delegate;
            this.preRead = preRead;
            this.postWrite = postWrite;
        }

        @Override
        public <T> Pair<T, Optional<A>> read(final DynamicOps<T> ops, final T input) {
            return delegate.read(ops, preRead.apply(ops, input));
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final T rest, final A value) {
            return postWrite.apply(ops, delegate.write(ops, rest, value));
        }

        @Override
        public RewriteResult<A, ?> all(final TypeRewriteRule rule, final boolean recurse, final boolean checkIndex) {
            return fix(this, delegate.rewriteOrNop(rule));
        }

        @Override
        public Optional<RewriteResult<A, ?>> one(final TypeRewriteRule rule) {
            return rule.rewrite(delegate).map(view -> fix(this, view));
        }

        @Override
        public Type<?> updateMu(final RecursiveTypeFamily newFamily) {
            return new HookType<>(delegate.updateMu(newFamily), preRead, postWrite);
        }

        @Override
        public TypeTemplate buildTemplate() {
            return DSL.hook(delegate.template(), preRead, postWrite);
        }

        @Override
        public Optional<TaggedChoice.TaggedChoiceType<?>> findChoiceType(final String name, final int index) {
            return delegate.findChoiceType(name, index);
        }

        @Override
        public Optional<Type<?>> findCheckedType(final int index) {
            return delegate.findCheckedType(index);
        }

        @Override
        public Optional<Type<?>> findFieldTypeOpt(final String name) {
            return delegate.findFieldTypeOpt(name);
        }

        @Override
        public Optional<A> point(final DynamicOps<?> ops) {
            return delegate.point(ops);
        }

        @Override
        public <FT, FR> Either<TypedOptic<A, ?, FT, FR>, FieldNotFoundException> findTypeInChildren(final Type<FT> type, final Type<FR> resultType, final TypeMatcher<FT, FR> matcher, final boolean recurse) {
            return delegate.findType(type, resultType, matcher, recurse).mapLeft(optic -> wrapOptic(optic, preRead, postWrite));
        }

        public static <A, B> RewriteResult<A, ?> fix(final HookType<A> type, final RewriteResult<A, B> instance) {
            if (Objects.equals(instance.view().function(), Functions.id())) {
                return RewriteResult.nop(type);
            }
            return opticView(type, instance, wrapOptic(TypedOptic.adapter(instance.view().type(), instance.view().newType()), type.preRead, type.postWrite));
        }

        protected static <A, B, FT, FR> TypedOptic<A, B, FT, FR> wrapOptic(final TypedOptic<A, B, FT, FR> optic, final HookFunction preRead, final HookFunction postWrite) {
            return new TypedOptic<>(
                optic.bounds(),
                DSL.hook(optic.sType(), preRead, postWrite),
                DSL.hook(optic.tType(), preRead, postWrite),
                optic.aType(),
                optic.bType(),
                optic.optic()
            );
        }

        @Override
        public String toString() {
            return "HookType[" + delegate + ", " + preRead + ", " + postWrite + "]";
        }

        @Override
        public boolean equals(final Object obj, final boolean ignoreRecursionPoints, final boolean checkIndex) {
            if (!(obj instanceof HookType<?>)) {
                return false;
            }
            final HookType<?> type = (HookType<?>) obj;
            return delegate.equals(type.delegate, ignoreRecursionPoints, checkIndex) && Objects.equals(preRead, type.preRead) && Objects.equals(postWrite, type.postWrite);
        }

        @Override
        public int hashCode() {
            return Objects.hash(delegate, preRead, postWrite);
        }
    }
}
