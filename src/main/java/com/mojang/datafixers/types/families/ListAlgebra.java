package com.mojang.datafixers.types.families;

import com.mojang.datafixers.RewriteResult;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ListAlgebra implements Algebra {
    private final String name;
    private final List<RewriteResult<?, ?>> views;
    private int hashCode;

    public ListAlgebra(final String name, final List<RewriteResult<?, ?>> views) {
        this.name = name;
        this.views = views;
    }

    @Override
    public RewriteResult<?, ?> apply(final int index) {
        return views.get(index);
    }

    @Override
    public String toString() {
        return "Algebra[" + name + ", " + views.stream().map(view -> view.view().function().toString()).collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ListAlgebra)) {
            return false;
        }
        final ListAlgebra that = (ListAlgebra) o;
        return Objects.equals(views, that.views);
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = views.hashCode();
        }
        return hashCode;
    }
}
