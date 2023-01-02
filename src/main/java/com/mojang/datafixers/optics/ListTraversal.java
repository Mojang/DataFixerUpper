// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;

import java.util.List;

public final class ListTraversal<A, B> implements Traversal<List<A>, List<B>, A, B> {
    static final ListTraversal<?, ?> INSTANCE = new ListTraversal<>();

    private ListTraversal() {
    }

    @Override
    public <F extends K1> FunctionType<List<A>, App<F, List<B>>> wander(final Applicative<F, ?> applicative, final FunctionType<A, App<F, B>> input) {
        return as -> {
            App<F, ImmutableList.Builder<B>> result = applicative.point(ImmutableList.builder());
            for (final A a : as) {
                result = applicative.ap2(applicative.point(ImmutableList.Builder::add), result, input.apply(a));
            }
            return applicative.map(ImmutableList.Builder::build, result);
        };
    }

    @Override
    public String toString() {
        return "ListTraversal";
    }
}
