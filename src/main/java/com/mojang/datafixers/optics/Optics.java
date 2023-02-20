// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.optics.profunctors.AffineP;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.optics.profunctors.Cocartesian;
import com.mojang.datafixers.optics.profunctors.GetterP;
import com.mojang.datafixers.optics.profunctors.Profunctor;
import com.mojang.datafixers.optics.profunctors.TraversalP;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Optics {
    public static <S, T, A, B> Adapter<S, T, A, B> toAdapter(final Optic<? super Profunctor.Mu, S, T, A, B> optic) {
        final Function<App2<Adapter.Mu<A, B>, A, B>, App2<Adapter.Mu<A, B>, S, T>> eval = optic.eval(new Adapter.Instance<>());
        return Adapter.unbox(eval.apply(adapter(Function.identity(), Function.identity())));
    }

    public static <S, T, A, B> Lens<S, T, A, B> toLens(final Optic<? super Cartesian.Mu, S, T, A, B> optic) {
        final Function<App2<Lens.Mu<A, B>, A, B>, App2<Lens.Mu<A, B>, S, T>> eval = optic.eval(new Lens.Instance<>());
        return Lens.unbox(eval.apply(lens(Function.identity(), (b, a) -> b)));
    }

    public static <S, T, A, B> Prism<S, T, A, B> toPrism(final Optic<? super Cocartesian.Mu, S, T, A, B> optic) {
        final Function<App2<Prism.Mu<A, B>, A, B>, App2<Prism.Mu<A, B>, S, T>> eval = optic.eval(new Prism.Instance<>());
        return Prism.unbox(eval.apply(prism(Either::right, Function.identity())));
    }

    public static <S, T, A, B> Affine<S, T, A, B> toAffine(final Optic<? super AffineP.Mu, S, T, A, B> optic) {
        final Function<App2<Affine.Mu<A, B>, A, B>, App2<Affine.Mu<A, B>, S, T>> eval = optic.eval(new Affine.Instance<>());
        return Affine.unbox(eval.apply(affine(Either::right, (b, a) -> b)));
    }

    public static <S, T, A, B> Getter<S, T, A, B> toGetter(final Optic<? super GetterP.Mu, S, T, A, B> optic) {
        final Function<App2<Getter.Mu<A, B>, A, B>, App2<Getter.Mu<A, B>, S, T>> eval = optic.eval(new Getter.Instance<>());
        return Getter.unbox(eval.apply(getter(Function.identity())));
    }

    public static <S, T, A, B> Traversal<S, T, A, B> toTraversal(final Optic<? super TraversalP.Mu, S, T, A, B> optic) {
        final Function<App2<Traversal.Mu<A, B>, A, B>, App2<Traversal.Mu<A, B>, S, T>> eval = optic.eval(new Traversal.Instance<>());
        return Traversal.unbox(eval.apply(new Traversal<A, B, A, B>() {
            @Override
            public <F extends K1> FunctionType<A, App<F, B>> wander(final Applicative<F, ?> applicative, final FunctionType<A, App<F, B>> input) {
                return input;
            }
        }));
    }

    static <S, T, A, B, F> Lens<S, T, Pair<F, A>, B> merge(final Lens<S, ?, F, ?> getter, final Lens<S, T, A, B> lens) {
        return lens(
            s -> Pair.of(getter.view(s), lens.view(s)),
            lens::update
        );
    }

    @SuppressWarnings("unchecked")
    public static <S, T> Adapter<S, T, S, T> id() {
        return (Adapter<S, T, S, T>) IdAdapter.INSTANCE;
    }

    public static boolean isId(final Optic<?, ?, ?, ?, ?> optic) {
        return optic == IdAdapter.INSTANCE;
    }

    public static <S, T, A, B> Adapter<S, T, A, B> adapter(final Function<S, A> from, final Function<B, T> to) {
        return new Adapter<S, T, A, B>() {
            @Override
            public A from(final S s) {
                return from.apply(s);
            }

            @Override
            public T to(final B b) {
                return to.apply(b);
            }
        };
    }

    public static <S, T, A, B> Lens<S, T, A, B> lens(final Function<S, A> view, final BiFunction<B, S, T> update) {
        return new Lens<S, T, A, B>() {
            @Override
            public A view(final S s) {
                return view.apply(s);
            }

            @Override
            public T update(final B b, final S s) {
                return update.apply(b, s);
            }
        };
    }

    public static <S, T, A, B> Prism<S, T, A, B> prism(final Function<S, Either<T, A>> match, final Function<B, T> build) {
        return new Prism<S, T, A, B>() {
            @Override
            public Either<T, A> match(final S s) {
                return match.apply(s);
            }

            @Override
            public T build(final B b) {
                return build.apply(b);
            }
        };
    }

    public static <S, T, A, B> Affine<S, T, A, B> affine(final Function<S, Either<T, A>> preview, final BiFunction<B, S, T> build) {
        return new Affine<S, T, A, B>() {
            @Override
            public Either<T, A> preview(final S s) {
                return preview.apply(s);
            }

            @Override
            public T set(final B b, final S s) {
                return build.apply(b, s);
            }
        };
    }

    public static <S, T, A, B> Getter<S, T, A, B> getter(final Function<S, A> get) {
        return get::apply;
    }

    public static <R, A, B> Forget<R, A, B> forget(final Function<A, R> function) {
        return function::apply;
    }

    public static <R, A, B> ForgetOpt<R, A, B> forgetOpt(final Function<A, Optional<R>> function) {
        return function::apply;
    }

    public static <R, A, B> ForgetE<R, A, B> forgetE(final Function<A, Either<B, R>> function) {
        return function::apply;
    }

    public static <R, A, B> ReForget<R, A, B> reForget(final Function<R, B> function) {
        return function::apply;
    }

    public static <S, T, A, B> Grate<S, T, A, B> grate(final FunctionType<FunctionType<FunctionType<S, A>, B>, T> grate) {
        return grate::apply;
    }

    public static <R, A, B> ReForgetEP<R, A, B> reForgetEP(final String name, final Function<Either<A, Pair<A, R>>, B> function) {
        return new ReForgetEP<R, A, B>() {
            @Override
            public B run(final Either<A, Pair<A, R>> e) {
                return function.apply(e);
            }

            @Override
            public String toString() {
                return "ReForgetEP_" + name;
            }
        };
    }

    public static <R, A, B> ReForgetE<R, A, B> reForgetE(final String name, final Function<Either<A, R>, B> function) {
        return new ReForgetE<R, A, B>() {
            @Override
            public B run(final Either<A, R> t) {
                return function.apply(t);
            }

            @Override
            public String toString() {
                return "ReForgetE_" + name;
            }
        };
    }

    public static <R, A, B> ReForgetP<R, A, B> reForgetP(final String name, final BiFunction<A, R, B> function) {
        return new ReForgetP<R, A, B>() {
            @Override
            public B run(final A a, final R r) {
                return function.apply(a, r);
            }

            @Override
            public String toString() {
                return "ReForgetP_" + name;
            }
        };
    }

    public static <R, A, B> ReForgetC<R, A, B> reForgetC(final String name, final Either<Function<R, B>, BiFunction<A, R, B>> either) {
        return new ReForgetC<R, A, B>() {
            @Override
            public Either<Function<R, B>, BiFunction<A, R, B>> impl() {
                return either;
            }

            @Override
            public String toString() {
                return "ReForgetC_" + name;
            }
        };
    }

    public static <I, J, X> PStore<I, J, X> pStore(final Function<J, X> peek, final Supplier<I> pos) {
        return new PStore<I, J, X>() {
            @Override
            public X peek(final J j) {
                return peek.apply(j);
            }

            @Override
            public I pos() {
                return pos.get();
            }
        };
    }

    public static <A, B> Function<A, B> getFunc(final App2<FunctionType.Mu, A, B> box) {
        return FunctionType.unbox(box);
    }

    @SuppressWarnings("unchecked")
    public static <F, G, F2> Proj1<F, G, F2> proj1() {
        return (Proj1<F, G, F2>) Proj1.INSTANCE;
    }

    public static boolean isProj1(final Optic<?, ?, ?, ?, ?> optic) {
        return optic == Proj1.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public static <F, G, G2> Proj2<F, G, G2> proj2() {
        return (Proj2<F, G, G2>) Proj2.INSTANCE;
    }

    public static boolean isProj2(final Optic<?, ?, ?, ?, ?> optic) {
        return optic == Proj2.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public static <F, G, F2> Inj1<F, G, F2> inj1() {
        return (Inj1<F, G, F2>) Inj1.INSTANCE;
    }

    public static boolean isInj1(final Optic<?, ?, ?, ?, ?> optic) {
        return optic == Inj1.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public static <F, G, G2> Inj2<F, G, G2> inj2() {
        return (Inj2<F, G, G2>) Inj2.INSTANCE;
    }

    public static boolean isInj2(final Optic<?, ?, ?, ?, ?> optic) {
        return optic == Inj2.INSTANCE;
    }

    /*public static <Proof extends Cartesian.Mu, S1, S2, T1, T2, A, B> Optic<Proof, Either<S1, S2>, Either<T1, T2>, A, B> choosing(final Optic<? super Profunctor.Mu, S1, T1, A, B> first, final Optic<? super Profunctor.Mu, S2, T2, A, B> second) {
        return new Optic<Proof, Either<S1, S2>, Either<T1, T2>, A, B>() {
            @Override
            public <P extends K2> FunctionType<App2<P, A, B>, App2<P, Either<S1, S2>, Either<T1, T2>>> eval(final App<? extends Proof, P> proof) {
                final Cartesian<? extends Proof> cartesian = Cartesian.unbox(proof);
                final ProfunctorFunctorWrapper.Instance<P, Either.Mu<A>, Either.Mu<B>> i1 = new ProfunctorFunctorWrapper.Instance<>(proof, new Either.Instance<>(), new Either.Instance<>());
                final ProfunctorFunctorWrapper.Instance<P, Either.Mu<S2>, Either.Mu<T2>> i2 = new ProfunctorFunctorWrapper.Instance<>(proof, new Either.Instance<>(), new Either.Instance<>());
                return pab -> {
                    final App2<P, Pair<Boolean, A>, Pair<Boolean, B>> withBool = cartesian.second(pab);
                    final App2<P, App<Either.Mu<A>, A>, App<Either.Mu<B>, B>> either = cartesian.dimap(
                        withBool,
                        e -> Either.unbox(e).map(l -> Pair.of(false, l), r -> Pair.of(true, r)),
                        pair -> pair.getFirst() ? Either.right(pair.getSecond()) : Either.left(pair.getSecond())
                    );
                    final ProfunctorFunctorWrapper<P, Either.Mu<A>, Either.Mu<B>, A, B> wrapper1 = new ProfunctorFunctorWrapper<>(either);
                    final App2<P, App<Either.Mu<A>, S2>, App<Either.Mu<B>, T2>> secondApplied = ProfunctorFunctorWrapper.unbox(second.eval(i1).apply(wrapper1)).value();
                    final App2<P, App<Either.Mu<S2>, A>, App<Either.Mu<T2>, B>> swapped = cartesian.dimap(secondApplied, e -> Either.unbox(e).swap(), e -> Either.unbox(e).swap());
                    final ProfunctorFunctorWrapper<P, Either.Mu<S2>, Either.Mu<T2>, A, B> wrapper2 = new ProfunctorFunctorWrapper<>(swapped);
                    final App2<P, App<Either.Mu<S2>, S1>, App<Either.Mu<T2>, T1>> firstApplied = ProfunctorFunctorWrapper.unbox(first.eval(i2).apply(wrapper2)).value();
                    return cartesian.dimap(firstApplied, e -> e, Either::unbox);
                };
            }
        };
    }*/

    public static <F, G, F2, G2, A, B> Lens<Either<F, G>, Either<F2, G2>, A, B> eitherLens(final Lens<F, F2, A, B> fLens, final Lens<G, G2, A, B> gLens) {
        return lens(
            either -> either.map(fLens::view, gLens::view),
            (b, either) -> either.mapBoth(f -> fLens.update(b, f), g -> gLens.update(b, g))
        );
    }

    public static <F, G, F2, G2, A, B> Affine<Either<F, G>, Either<F2, G2>, A, B> eitherAffine(final Affine<F, F2, A, B> fAffine, final Affine<G, G2, A, B> gAffine) {
        return affine(
            either -> either.map(
                f -> fAffine.preview(f).mapLeft(Either::left),
                g -> gAffine.preview(g).mapLeft(Either::right)
            ),
            (b, either) -> either.mapBoth(f -> fAffine.set(b, f), g -> gAffine.set(b, g))
        );
    }

    public static <F, G, F2, G2, A, B> Traversal<Either<F, G>, Either<F2, G2>, A, B> eitherTraversal(final Traversal<F, F2, A, B> fOptic, final Traversal<G, G2, A, B> gOptic) {
        return new Traversal<Either<F, G>, Either<F2, G2>, A, B>() {
            @Override
            public <FT extends K1> FunctionType<Either<F, G>, App<FT, Either<F2, G2>>> wander(final Applicative<FT, ?> applicative, final FunctionType<A, App<FT, B>> input) {
                return e -> e.map(
                    l -> {
                        return applicative.ap(Either::left, fOptic.wander(applicative, input).apply(l));
                    },
                    r -> {
                        return applicative.ap(Either::right, gOptic.wander(applicative, input).apply(r));
                    }
                );
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <A, B> ListTraversal<A, B> listTraversal() {
        return (ListTraversal<A, B>) ListTraversal.INSTANCE;
    }
}
