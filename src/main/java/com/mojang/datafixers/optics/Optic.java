// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public interface Optic<Proof extends K1, S, T, A, B> {
    <P extends K2> Function<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends Proof, P> proof);

    default <Proof2 extends Proof, A1, B1> Optic<Proof2, S, T, A1, B1> compose(final Optic<? super Proof2, A, B, A1, B1> optic) {
        return new CompositionOptic<>(this, optic);
    }

    @SuppressWarnings("unchecked")
    default <Proof2 extends K1, A1, B1> Optic<?, S, T, A1, B1> composeUnchecked(final Optic<?, A, B, A1, B1> optic) {
        return new CompositionOptic<Proof2, S, T, A, B, A1, B1>((Optic<? super Proof2, S, T, A, B>) this, (Optic<? super Proof2, A, B, A1, B1>) optic);
    }

    final class CompositionOptic<Proof extends K1, S, T, A, B, A1, B1> implements Optic<Proof, S, T, A1, B1> {
        protected final Optic<? super Proof, S, T, A, B> outer;
        protected final Optic<? super Proof, A, B, A1, B1> inner;

        public CompositionOptic(final Optic<? super Proof, S, T, A, B> outer, final Optic<? super Proof, A, B, A1, B1> inner) {
            this.outer = outer;
            this.inner = inner;
        }

        @Override
        public <P extends K2> Function<App2<P, A1, B1>, App2<P, S, T>> eval(final App<? extends Proof, P> proof) {
            return outer.eval(proof).compose(inner.eval(proof));
        }

        @Override
        public String toString() {
            return "(" + outer + " ◦ " + inner + ")";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final CompositionOptic<?, ?, ?, ?, ?, ?, ?> that = (CompositionOptic<?, ?, ?, ?, ?, ?, ?>) o;
            return Objects.equals(outer, that.outer) && Objects.equals(inner, that.inner);
        }

        @Override
        public int hashCode() {
            return Objects.hash(outer, inner);
        }

        public Optic<? super Proof, S, T, A, B> outer() {
            return outer;
        }

        public Optic<? super Proof, A, B, A1, B1> inner() {
            return inner;
        }
    }

    @SuppressWarnings("unchecked")
    default <Proof2 extends K1> Optional<Optic<? super Proof2, S, T, A, B>> upCast(final Set<TypeToken<? extends K1>> proofBounds, final TypeToken<Proof2> proof) {
        if (proofBounds.stream().allMatch(bound -> bound.isSupertypeOf(proof))) {
            return Optional.of((Optic<? super Proof2, S, T, A, B>) this);
        }
        return Optional.empty();
    }
}
