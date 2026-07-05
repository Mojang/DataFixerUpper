package com.mojang.datafixers;

import com.mojang.datafixers.functions.Functions;
import com.mojang.datafixers.functions.PointFree;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.optics.profunctors.Cocartesian;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class PointFreeRuleTests {
    @Test
    public void projRules_outputTypesCorrectly() {
        final PointFree<Function<String, List<String>>> firstFunc = Functions.fun("firstTerm", ops -> List::of, DSL.string(), DSL.list(DSL.string()));
        final PointFree<Function<Integer, Float>> secondFunc = Functions.fun("secondTerm", ops -> num -> num.floatValue() + 0.5F, DSL.intType(), DSL.floatType());
        final PointFree<Function<Pair<String, Integer>, Pair<List<String>, Float>>> twoThenOne =
            Functions.comp(
                Functions.app(
                    Functions.profunctorTransformer(new TypedOptic<>(
                        Cartesian.Mu.TYPE_TOKEN,
                        DSL.and(DSL.string(), DSL.floatType()),
                        DSL.and(DSL.list(DSL.string()), DSL.floatType()),
                        DSL.string(),
                        DSL.list(DSL.string()),
                        Optics.proj1()
                    )),
                    firstFunc
                ),
                Functions.app(
                    Functions.profunctorTransformer(new TypedOptic<>(
                        Cartesian.Mu.TYPE_TOKEN,
                        DSL.and(DSL.string(), DSL.intType()),
                        DSL.and(DSL.string(), DSL.floatType()),
                        DSL.intType(),
                        DSL.floatType(),
                        Optics.proj2()
                    )),
                    secondFunc
                )
            );
        final PointFree<Function<Pair<String, Integer>, Pair<List<String>, Float>>> oneThenTwo =
            Functions.comp(
                Functions.app(
                    Functions.profunctorTransformer(new TypedOptic<>(
                        Cartesian.Mu.TYPE_TOKEN,
                        DSL.and(DSL.list(DSL.string()), DSL.intType()),
                        DSL.and(DSL.list(DSL.string()), DSL.floatType()),
                        DSL.intType(),
                        DSL.floatType(),
                        Optics.proj2()
                    )),
                    secondFunc
                ),
                Functions.app(
                    Functions.profunctorTransformer(new TypedOptic<>(
                        Cartesian.Mu.TYPE_TOKEN,
                        DSL.and(DSL.string(), DSL.intType()),
                        DSL.and(DSL.list(DSL.string()), DSL.intType()),
                        DSL.string(),
                        DSL.list(DSL.string()),
                        Optics.proj1()
                    )),
                    firstFunc
                )
            );
        final PointFree<Function<Pair<String, Integer>, Pair<List<String>, Float>>> output1 = PointFreeRule.SortProj.INSTANCE.rewriteOrNop(twoThenOne);
        assertEquals(output1, twoThenOne);
        // Proj2 is sorted to be applied before Proj1, to be able to merge the applicatives in a future step
        final PointFree<Function<Pair<String, Integer>, Pair<List<String>, Float>>> output2 = PointFreeRule.SortProj.INSTANCE.rewriteOrNop(oneThenTwo);
        assertEquals(output2, twoThenOne);
    }

    @Test
    public void injRules_outputTypesCorrectly() {
        final PointFree<Function<String, List<String>>> firstFunc = Functions.fun("firstTerm", ops -> List::of, DSL.string(), DSL.list(DSL.string()));
        final PointFree<Function<Integer, Float>> secondFunc = Functions.fun("secondTerm", ops -> num -> num.floatValue() + 0.5F, DSL.intType(), DSL.floatType());
        final PointFree<Function<Either<String, Integer>, Either<List<String>, Float>>> twoThenOne =
            Functions.comp(
                Functions.app(
                    Functions.profunctorTransformer(new TypedOptic<>(
                        Cocartesian.Mu.TYPE_TOKEN,
                        DSL.or(DSL.string(), DSL.floatType()),
                        DSL.or(DSL.list(DSL.string()), DSL.floatType()),
                        DSL.string(),
                        DSL.list(DSL.string()),
                        Optics.inj1()
                    )),
                    firstFunc
                ),
                Functions.app(
                    Functions.profunctorTransformer(new TypedOptic<>(
                        Cocartesian.Mu.TYPE_TOKEN,
                        DSL.or(DSL.string(), DSL.intType()),
                        DSL.or(DSL.string(), DSL.floatType()),
                        DSL.intType(),
                        DSL.floatType(),
                        Optics.inj2()
                    )),
                    secondFunc
                )
            );
        final PointFree<Function<Either<String, Integer>, Either<List<String>, Float>>> oneThenTwo =
            Functions.comp(
                Functions.app(
                    Functions.profunctorTransformer(new TypedOptic<>(
                        Cocartesian.Mu.TYPE_TOKEN,
                        DSL.or(DSL.list(DSL.string()), DSL.intType()),
                        DSL.or(DSL.list(DSL.string()), DSL.floatType()),
                        DSL.intType(),
                        DSL.floatType(),
                        Optics.inj2()
                    )),
                    secondFunc
                ),
                Functions.app(
                    Functions.profunctorTransformer(new TypedOptic<>(
                        Cocartesian.Mu.TYPE_TOKEN,
                        DSL.or(DSL.string(), DSL.intType()),
                        DSL.or(DSL.list(DSL.string()), DSL.intType()),
                        DSL.string(),
                        DSL.list(DSL.string()),
                        Optics.inj1()
                    )),
                    firstFunc
                )
            );
        final PointFree<Function<Either<String, Integer>, Either<List<String>, Float>>> output1 = PointFreeRule.SortInj.INSTANCE.rewriteOrNop(twoThenOne);
        assertEquals(output1, twoThenOne);
        // Inj2 is sorted to be applied before Inj1, to be able to merge the applicatives in a future step
        final PointFree<Function<Either<String, Integer>, Either<List<String>, Float>>> output2 = PointFreeRule.SortInj.INSTANCE.rewriteOrNop(oneThenTwo);
        assertEquals(output2, twoThenOne);
    }

    @Test
    public void lensComp_inputTypesCorrectly() {

        final PointFree<Function<Float, String>> firstFunc = Functions.fun("firstTerm", ops -> Object::toString, DSL.floatType(), DSL.string());
        final PointFree<Function<Integer, Float>> secondFunc = Functions.fun("secondTerm", ops -> num -> num.floatValue() + 0.5F, DSL.intType(), DSL.floatType());
        final PointFree<Function<Pair<Pair<Integer, Integer>, String>, Pair<Pair<Integer, String>, String>>> input =
            Functions.comp(
                Functions.app(
                    Functions.profunctorTransformer(new TypedOptic<>(
                        Set.of(Cartesian.Mu.TYPE_TOKEN),
                        List.of(
                            new TypedOptic.Element<>(
                                DSL.and(DSL.and(DSL.intType(), DSL.floatType()), DSL.string()),
                                DSL.and(DSL.and(DSL.intType(), DSL.string()), DSL.string()),
                                DSL.and(DSL.intType(), DSL.floatType()),
                                DSL.and(DSL.intType(), DSL.string()),
                                Optics.proj1()
                            ),
                            new TypedOptic.Element<>(
                                DSL.and(DSL.intType(), DSL.floatType()),
                                DSL.and(DSL.intType(), DSL.string()),
                                DSL.floatType(),
                                DSL.string(),
                                Optics.proj2()
                            )
                        )
                    )),
                    firstFunc
                ),
                Functions.app(
                    Functions.profunctorTransformer(new TypedOptic<>(
                        Set.of(Cartesian.Mu.TYPE_TOKEN),
                        List.of(
                            new TypedOptic.Element<>(
                                DSL.and(DSL.and(DSL.intType(), DSL.intType()), DSL.string()),
                                DSL.and(DSL.and(DSL.intType(), DSL.floatType()), DSL.string()),
                                DSL.and(DSL.intType(), DSL.intType()),
                                DSL.and(DSL.intType(), DSL.floatType()),
                                Optics.proj1()
                            ),
                            new TypedOptic.Element<>(
                                DSL.and(DSL.intType(), DSL.intType()),
                                DSL.and(DSL.intType(), DSL.floatType()),
                                DSL.intType(),
                                DSL.floatType(),
                                Optics.proj2()
                            )
                        )
                    )),
                    secondFunc
                )
            );
        final PointFree<Function<Pair<Pair<Integer, Integer>, String>, Pair<Pair<Integer, String>, String>>> output = PointFreeRule.LensComp.INSTANCE.rewriteOrNop(input);
        final PointFree<Function<Pair<Pair<Integer, Integer>, String>, Pair<Pair<Integer, String>, String>>> expected =
            Functions.app(
                Functions.profunctorTransformer(new TypedOptic<>(
                    Set.of(Cartesian.Mu.TYPE_TOKEN),
                    List.of(
                        new TypedOptic.Element<>(
                            DSL.and(DSL.and(DSL.intType(), DSL.intType()), DSL.string()),
                            DSL.and(DSL.and(DSL.intType(), DSL.string()), DSL.string()),
                            DSL.and(DSL.intType(), DSL.intType()),
                            DSL.and(DSL.intType(), DSL.string()),
                            Optics.proj1()
                        ),
                        new TypedOptic.Element<>(
                            DSL.and(DSL.intType(), DSL.intType()),
                            DSL.and(DSL.intType(), DSL.string()),
                            DSL.intType(),
                            DSL.string(),
                            Optics.proj2()
                        )
                    )
                )),
                Functions.comp(firstFunc, secondFunc)
            );
        assertEquals(output, expected);
    }
}
