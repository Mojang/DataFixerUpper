// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Optic<Proof extends K1, S, T, A, B> {
    <P extends K2> Function<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends Proof, P> proof);

    record CompositionOptic<Proof extends K1, S, T, A, B>(List<? extends Optic<? super Proof, ?, ?, ?, ?>> optics) implements Optic<Proof, S, T, A, B> {
        @Override
        @SuppressWarnings("unchecked")
        public <P extends K2> Function<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends Proof, P> proof) {
            final List<Function<? extends App2<P, ?, ?>, ? extends App2<P, ?, ?>>> functions = new ArrayList<>(optics.size());
            for (int i = optics.size() - 1; i >= 0; i--) {
                functions.add(optics.get(i).eval(proof));
            }
            return input -> {
                App2<P, ?, ?> result = input;
                for (final Function<? extends App2<P, ?, ?>, ? extends App2<P, ?, ?>> function : functions) {
                    result = applyUnchecked(function, result);
                }
                return (App2<P, S, T>) result;
            };
        }

        @SuppressWarnings("unchecked")
        private static <P extends K2, T extends App2<P, ?, ?>> App2<P, ?, ?> applyUnchecked(final Function<T, ? extends App2<P, ?, ?>> function, final App2<P, ?, ?> input) {
            return function.apply((T) input);
        }

        @Override
        public String toString() {
            return "(" + optics.stream().map(Object::toString).collect(Collectors.joining(" \u25E6 ")) + ")";
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
