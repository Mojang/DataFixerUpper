package com.mojang.datafixers.functions;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.Optic;
import com.mojang.datafixers.types.DynamicOps;

import java.util.Objects;
import java.util.function.Function;

final class ProfunctorTransformer<P extends K2, Proof extends K1, S, T, A, B> extends PointFree<App2<FunctionType.Mu, App2<P, A, B>, App2<P, S, T>>> {
    protected final Optic<? super Proof, S, T, A, B> optic;
    protected final App<? extends Proof, P> proof;
    protected final FunctionType<App2<P, A, B>, App2<P, S, T>> func;

    public ProfunctorTransformer(final Optic<? super Proof, S, T, A, B> optic, final App<? extends Proof, P> proof) {
        this.optic = optic;
        this.proof = proof;
        func = optic.eval(proof);
    }

    @Override
    public String toString(final int level) {
        return "Optic[" + optic + "]";
    }

    @Override
    public Function<DynamicOps<?>, App2<FunctionType.Mu, App2<P, A, B>, App2<P, S, T>>> eval() {
        return ops -> func;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ProfunctorTransformer<?, ?, ?, ?, ?, ?> that = (ProfunctorTransformer<?, ?, ?, ?, ?, ?>) o;
        return Objects.equals(optic, that.optic) && Objects.equals(proof, that.proof);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optic, proof);
    }
}
