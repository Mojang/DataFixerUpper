package com.mojang.datafixers.optics;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;

import java.util.List;

public final class ListTraversal<A, B> implements Traversal<List<A>, List<B>, A, B> {
    @Override
    public <F extends K1> FunctionType<List<A>, App<F, List<B>>> wander(final Applicative<F, ?> applicative, final FunctionType<A, App<F, B>> input) {
        return as -> {
            App<F, List<B>> result = applicative.point(ImmutableList.of());
            for (final A a : as) {
                result = applicative.ap2((bs, b) -> {
                    final ImmutableList.Builder<B> builder = ImmutableList.builder();
                    builder.addAll(bs);
                    builder.add(b);
                    return builder.build();
                }, result, input.apply(a));
            }
            return result;
        };
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ListTraversal<?, ?>;
    }

    @Override
    public String toString() {
        return "ListTraversal";
    }
}
