// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.Objects;
import java.util.function.Function;

public abstract class DataFix {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Schema outputSchema;
    private final boolean changesType;
    @Nullable
    private TypeRewriteRule rule;

    public DataFix(final Schema outputSchema, final boolean changesType) {
        this.outputSchema = outputSchema;
        this.changesType = changesType;
    }

    protected <A> TypeRewriteRule fixTypeEverywhere(final String name, final Type<A> type, final Function<DynamicOps<?>, Function<A, A>> function) {
        return fixTypeEverywhere(name, type, type, function, new BitSet());
    }

    @SuppressWarnings("unchecked")
    protected <A, B> TypeRewriteRule convertUnchecked(final String name, final Type<A> type, final Type<B> newType) {
        return fixTypeEverywhere(name, type, newType, ops -> (Function<A, B>) Function.identity(), new BitSet());
    }

    protected TypeRewriteRule writeAndRead(final String name, final Type<?> type, final Type<?> newType) {
        return writeFixAndRead(name, type, newType, Function.identity());
    }

    protected <A, B> TypeRewriteRule writeFixAndRead(final String name, final Type<A> type, final Type<B> newType, final Function<Dynamic<?>, Dynamic<?>> fix) {
        return fixTypeEverywhere(name, type, newType, ops -> input ->
            newType.readTyped(fix.apply(type.writeDynamic(ops, input))).getSecond().orElseThrow(() -> new IllegalStateException("Could not read new type in \"" + name + "\"")).getValue()
        );
    }

    protected <A, B> TypeRewriteRule fixTypeEverywhere(final String name, final Type<A> type, final Type<B> newType, final Function<DynamicOps<?>, Function<A, B>> function) {
        return fixTypeEverywhere(name, type, newType, function, new BitSet());
    }

    protected <A, B> TypeRewriteRule fixTypeEverywhere(final String name, final Type<A> type, final Type<B> newType, final Function<DynamicOps<?>, Function<A, B>> function, final BitSet bitSet) {
        return fixTypeEverywhere(type, RewriteResult.create(View.create(name, type, newType, new NamedFunctionWrapper<>(name, function)), bitSet));
    }

    protected <A> TypeRewriteRule fixTypeEverywhereTyped(final String name, final Type<A> type, final Function<Typed<?>, Typed<?>> function) {
        return fixTypeEverywhereTyped(name, type, function, new BitSet());
    }

    protected <A> TypeRewriteRule fixTypeEverywhereTyped(final String name, final Type<A> type, final Function<Typed<?>, Typed<?>> function, final BitSet bitSet) {
        return fixTypeEverywhereTyped(name, type, type, function, bitSet);
    }

    protected <A, B> TypeRewriteRule fixTypeEverywhereTyped(final String name, final Type<A> type, final Type<B> newType, final Function<Typed<?>, Typed<?>> function) {
        return fixTypeEverywhereTyped(name, type, newType, function, new BitSet());
    }

    protected <A, B> TypeRewriteRule fixTypeEverywhereTyped(final String name, final Type<A> type, final Type<B> newType, final Function<Typed<?>, Typed<?>> function, final BitSet bitSet) {
        return fixTypeEverywhere(type, checked(name, type, newType, function, bitSet));
    }

    @SuppressWarnings("unchecked")
    public static <A, B> RewriteResult<A, B> checked(final String name, final Type<A> type, final Type<B> newType, final Function<Typed<?>, Typed<?>> function, final BitSet bitSet) {
        return RewriteResult.create(View.create(name, type, newType, new NamedFunctionWrapper<>(name, ops -> a -> {
            final Typed<?> result = function.apply(new Typed<>(type, ops, a));
            if (!newType.equals(result.type, true, false)) {
                throw new IllegalStateException(String.format("Dynamic type check failed: %s not equal to %s", newType, result.type));
            }
            return (B) result.value;
        })), bitSet);
    }

    protected <A, B> TypeRewriteRule fixTypeEverywhere(final Type<A> type, final RewriteResult<A, B> view) {
        return TypeRewriteRule.checkOnce(TypeRewriteRule.everywhere(TypeRewriteRule.ifSame(type, view), DataFixerUpper.OPTIMIZATION_RULE, true, true), this::onFail);
    }

    protected void onFail(final Type<?> type) {
        LOGGER.info("Not matched: " + this + " " + type);
    }

    public final int getVersionKey() {
        return getOutputSchema().getVersionKey();
    }

    public TypeRewriteRule getRule() {
        if (rule == null) {
            rule = makeRule();
        }
        return rule;
    }

    protected abstract TypeRewriteRule makeRule();

    protected Schema getInputSchema() {
        if (changesType) {
            return outputSchema.getParent();
        }
        return getOutputSchema();
    }

    protected Schema getOutputSchema() {
        return outputSchema;
    }

    private static final class NamedFunctionWrapper<A, B> implements Function<DynamicOps<?>, Function<A, B>> {
        private final String name;
        private final Function<DynamicOps<?>, Function<A, B>> delegate;

        public NamedFunctionWrapper(final String name, final Function<DynamicOps<?>, Function<A, B>> delegate) {
            this.name = name;
            this.delegate = delegate;
        }

        @Override
        public Function<A, B> apply(final DynamicOps<?> ops) {
            return delegate.apply(ops);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final NamedFunctionWrapper<?, ?> that = (NamedFunctionWrapper<?, ?>) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
