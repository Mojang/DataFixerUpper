package com.mojang.datafixers.functions;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Func;
import com.mojang.datafixers.types.Type;

import java.util.Objects;
import java.util.Optional;

final class Comp<A, B, C> extends PointFreeFunction<A, C> {
    protected final Type<B> middleType;
    protected final PointFree<App2<FunctionType.Mu, B, C>> first;
    protected final PointFree<App2<FunctionType.Mu, A, B>> second;

    public Comp(final Type<B> middleType, final PointFree<App2<FunctionType.Mu, B, C>> first, final PointFree<App2<FunctionType.Mu, A, B>> second) {
        this.middleType = middleType;
        this.first = first;
        this.second = second;
    }

    @Override
    public C eval(final DynamicOps<?> ops, final A input) {
        return Optics.getFunc(first.eval().apply(ops)).apply(Optics.getFunc(second.eval().apply(ops)).apply(input));
    }

    @Override
    public String toString(final int level) {
        return "(\n" + indent(level + 1) + first.toString(level + 1) + "\n" + indent(level + 1) + "◦\n" + indent(level + 1) + second.toString(level + 1) + "\n" + indent(level) + ")";
    }

    @Override
    public Optional<? extends PointFree<App2<FunctionType.Mu, A, C>>> all(final PointFreeRule rule, final Type<App2<FunctionType.Mu, A, C>> type) {
        final Func<A, C> funcType = (Func<A, C>) type;
        return Optional.of(Functions.comp(
            middleType,
            rule.rewrite(DSL.func(middleType, funcType.second()), first).map(f -> (PointFree<App2<FunctionType.Mu, B, C>>) f).orElse(first),
            rule.rewrite(DSL.func(funcType.first(), middleType), second).map(f1 -> (PointFree<App2<FunctionType.Mu, A, B>>) f1).orElse(second)
        ));
    }

    @Override
    public Optional<? extends PointFree<App2<FunctionType.Mu, A, C>>> one(final PointFreeRule rule, final Type<App2<FunctionType.Mu, A, C>> type) {
        final Func<A, C> funcType = (Func<A, C>) type;
        return rule.rewrite(DSL.func(middleType, funcType.second()), first).map(f -> Optional.of(Functions.comp(middleType, f, second)))
            .orElseGet(() -> rule.rewrite(DSL.func(funcType.first(), middleType), second).map(s -> Functions.comp(middleType, first, s)));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Comp<?, ?, ?> comp = (Comp<?, ?, ?>) o;
        return Objects.equals(first, comp.first) && Objects.equals(second, comp.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
