package com.mojang.datafixers.functions;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.Optic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.Algebra;
import com.mojang.datafixers.types.templates.RecursivePoint;

import java.util.Objects;
import java.util.function.Function;

public abstract class Functions {
    private static final Id<?> ID = new Id<>();

    @SuppressWarnings("unchecked")
    public static <A, B, C> PointFree<App2<FunctionType.Mu, A, C>> comp(final Type<B> middleType, final PointFree<App2<FunctionType.Mu, B, C>> f1, final PointFree<App2<FunctionType.Mu, A, B>> f2) {
        if (Objects.equals(f1, id())) {
            return (PointFree<App2<FunctionType.Mu, A, C>>) (PointFree<?>) f2;
        }
        if (Objects.equals(f2, id())) {
            return (PointFree<App2<FunctionType.Mu, A, C>>) (PointFree<?>) f1;
        }
        return new Comp<>(middleType, f1, f2);
    }

    public static <A, B> PointFree<App2<FunctionType.Mu, A, B>> fun(final String name, final Function<DynamicOps<?>, FunctionType<A, B>> fun) {
        return new FunctionWrapper<>(name, fun);
    }

    public static <A, B> PointFree<B> app(final PointFree<App2<FunctionType.Mu, A, B>> fun, final PointFree<A> arg, final Type<A> argType) {
        return new Apply<>(fun, arg, argType);
    }

    public static <P extends K2, Proof extends K1, S, T, A, B> PointFree<App2<FunctionType.Mu, App2<P, A, B>, App2<P, S, T>>> profunctorTransformer(final Optic<? super Proof, S, T, A, B> lens, final App<? extends Proof, P> proof) {
        return new ProfunctorTransformer<>(lens, proof);
    }

    public static <S, T, A, B> PointFree<App2<FunctionType.Mu, App2<FunctionType.Mu, A, B>, App2<FunctionType.Mu, S, T>>> profunctorTransformer(final Optic<? super FunctionType.Instance.Mu, S, T, A, B> lens) {
        return new ProfunctorTransformer<>(lens, FunctionType.Instance.INSTANCE);
    }

    public static <A> Bang<A> bang() {
        return new Bang<>();
    }

    public static <A> PointFree<App2<FunctionType.Mu, A, A>> in(final RecursivePoint.RecursivePointType<A> type) {
        return new In<>(type);
    }

    public static <A> PointFree<App2<FunctionType.Mu, A, A>> out(final RecursivePoint.RecursivePointType<A> type) {
        return new Out<>(type);
    }

    public static <A, B> PointFree<App2<FunctionType.Mu, A, B>> fold(final RecursivePoint.RecursivePointType<A> aType, final RewriteResult<?, B> function, final Algebra algebra, final int index) {
        return new Fold<>(aType, function, algebra, index);
    }

    @SuppressWarnings("unchecked")
    public static <A> PointFree<App2<FunctionType.Mu, A, A>> id() {
        return (Id<A>) ID;
    }
}
