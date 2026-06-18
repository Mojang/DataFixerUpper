package com.mojang.datafixers;

import com.mojang.datafixers.functions.Functions;
import com.mojang.datafixers.functions.PointFree;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.optics.profunctors.Cocartesian;
import com.mojang.datafixers.optics.profunctors.Profunctor;
import com.mojang.datafixers.optics.profunctors.TraversalP;
import com.mojang.datafixers.types.families.ListAlgebra;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.templates.Check;
import com.mojang.datafixers.types.templates.RecursivePoint;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class RuleOptimisationTest {
    @Test
    public void pointFreeRules_optimiseNestedApps() {
        final RecursiveTypeFamily typeFamily = new RecursiveTypeFamily("nestedTest", DSL.or(DSL.check("first", 0, DSL.constType(DSL.intType())), DSL.check("second", 1, DSL.constType(DSL.string()))));
        // Dummy types here, as we are not actually going to apply the rule to something, some corners are cut to make the type not too long
        final RecursivePoint.RecursivePointType<Either<Integer, String>> firstNested = new RecursivePoint.RecursivePointType<>(typeFamily, 0, () -> DSL.or(new Check.CheckType<>("first", 0, 0, DSL.intType()), new Check.CheckType<>("second", 0, 1, DSL.string())));
        final RecursivePoint.RecursivePointType<Either<Integer, String>> secondNested = new RecursivePoint.RecursivePointType<>(typeFamily, 1, () -> DSL.or(new Check.CheckType<>("first", 1, 0, DSL.intType()), new Check.CheckType<>("second", 1, 1, DSL.string())));
        final ListAlgebra dummyAlgebra = new ListAlgebra("everywhere", List.of(RewriteResult.nop(DSL.intType()), RewriteResult.nop(DSL.string())));
        final PointFree<Function<Integer, String>> converter = Functions.fun("converter", ops -> Object::toString, DSL.intType(), DSL.string());
        final PointFree<Function<
                    Either<
                        Pair<String, List<Either<Either<Integer, String>, Integer>>>,
                        Pair<String, Pair<Either<Either<Integer, String>, Unit>, Either<Integer, String>>>
                        >,
                    Either<
                        Pair<String, List<Either<Either<Integer, String>, String>>>,
                        Pair<String, Pair<Either<Either<Integer, String>, Unit>, Either<Integer, String>>>
                        >
                    >> input = Functions.comp(
            Functions.comp(
                Functions.app(
                    Functions.profunctorTransformer(
                        new TypedOptic<>(
                            Set.of(Profunctor.Mu.TYPE_TOKEN, TraversalP.Mu.TYPE_TOKEN, Cartesian.Mu.TYPE_TOKEN, Cocartesian.Mu.TYPE_TOKEN),
                            List.of(
                                new TypedOptic.Element<>(
                                    DSL.or(DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))), DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested))),
                                    DSL.or(DSL.named("first", DSL.list(DSL.or(firstNested, DSL.string()))), DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested))),
                                    DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                                    DSL.named("first", DSL.list(DSL.or(firstNested, DSL.string()))),
                                    Optics.inj1()
                                ),
                                new TypedOptic.Element<>(
                                    DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                                    DSL.named("first", DSL.list(DSL.or(firstNested, DSL.string()))),
                                    DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                                    DSL.named("first", DSL.list(DSL.or(firstNested, DSL.string()))),
                                    Optics.id()
                                ),
                                new TypedOptic.Element<>(
                                    DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                                    DSL.named("first", DSL.list(DSL.or(firstNested, DSL.string()))),
                                    DSL.list(DSL.or(firstNested, DSL.intType())),
                                    DSL.list(DSL.or(firstNested, DSL.string())),
                                    Optics.proj2()
                                ),
                                new TypedOptic.Element<>(
                                    DSL.list(DSL.or(firstNested, DSL.intType())),
                                    DSL.list(DSL.or(firstNested, DSL.string())),
                                    DSL.list(DSL.or(firstNested, DSL.intType())),
                                    DSL.list(DSL.or(firstNested, DSL.string())),
                                    Optics.id()
                                ),
                                new TypedOptic.Element<>(
                                    DSL.list(DSL.or(firstNested, DSL.intType())),
                                    DSL.list(DSL.or(firstNested, DSL.string())),
                                    DSL.or(firstNested, DSL.intType()),
                                    DSL.or(firstNested, DSL.string()),
                                    Optics.listTraversal()
                                ),
                                new TypedOptic.Element<>(
                                    DSL.or(firstNested, DSL.intType()),
                                    DSL.or(firstNested, DSL.string()),
                                    DSL.intType(),
                                    DSL.string(),
                                    Optics.inj2()
                                )
                            )
                        )
                    ),
                    converter
                ),
                Functions.app(
                    Functions.profunctorTransformer(
                        new TypedOptic<>(Cocartesian.Mu.TYPE_TOKEN,
                            DSL.or(DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))), DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested))),
                            DSL.or(DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))), DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested))),
                            DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                            DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                            Optics.inj2()
                        )
                    ),
                    Functions.app(
                        Functions.profunctorTransformer(
                            new TypedOptic<>(Profunctor.Mu.TYPE_TOKEN,
                                DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                                DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                                DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                                DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                                Optics.id()
                            )
                        ),
                        Functions.app(
                            Functions.profunctorTransformer(
                                new TypedOptic<>(Set.of(Profunctor.Mu.TYPE_TOKEN, Cartesian.Mu.TYPE_TOKEN),
                                    List.of(
                                        new TypedOptic.Element<>(
                                            DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                                            DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                                            DSL.and(DSL.optional(secondNested), firstNested),
                                            DSL.and(DSL.optional(secondNested), firstNested),
                                            Optics.proj2()
                                        ),
                                        new TypedOptic.Element<>(
                                            DSL.and(DSL.optional(secondNested), firstNested),
                                            DSL.and(DSL.optional(secondNested), firstNested),
                                            DSL.and(DSL.optional(secondNested), firstNested),
                                            DSL.and(DSL.optional(secondNested), firstNested),
                                            Optics.id()
                                        )
                                    )
                                )
                            ),
                            Functions.comp(
                                Functions.app(
                                    Functions.profunctorTransformer(
                                        new TypedOptic<>(Cartesian.Mu.TYPE_TOKEN,
                                            DSL.and(DSL.optional(secondNested), firstNested),
                                            DSL.and(DSL.optional(secondNested), firstNested),
                                            firstNested,
                                            firstNested,
                                            Optics.proj2()
                                        )
                                    ),
                                    Functions.fold(firstNested, firstNested, dummyAlgebra, 0)
                                ),
                                Functions.app(
                                    Functions.profunctorTransformer(
                                        new TypedOptic<>(Cartesian.Mu.TYPE_TOKEN,
                                            DSL.and(DSL.optional(secondNested), firstNested),
                                            DSL.and(DSL.optional(secondNested), firstNested),
                                            DSL.optional(secondNested),
                                            DSL.optional(secondNested),
                                            Optics.proj1()
                                        )
                                    ),
                                    Functions.app(
                                        Functions.profunctorTransformer(
                                            new TypedOptic<>(Cocartesian.Mu.TYPE_TOKEN,
                                                DSL.optional(secondNested),
                                                DSL.optional(secondNested),
                                                secondNested,
                                                secondNested,
                                                Optics.inj1()
                                            )
                                        ),
                                        Functions.fold(firstNested, firstNested, dummyAlgebra, 1)
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            Functions.app(
                Functions.profunctorTransformer(
                    new TypedOptic<
                        >(Cocartesian.Mu.TYPE_TOKEN,
                        DSL.or(DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))), DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested))),
                        DSL.or(DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))), DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested))),
                        DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                        DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                        Optics.inj1()
                    )
                ),
                Functions.app(
                    Functions.profunctorTransformer(
                        new TypedOptic<>(
                            Profunctor.Mu.TYPE_TOKEN,
                            DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                            DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                            DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                            DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                            Optics.id()
                        )
                    ),
                    Functions.app(
                        Functions.profunctorTransformer(
                            new TypedOptic<>(
                                Set.of(Profunctor.Mu.TYPE_TOKEN, Cartesian.Mu.TYPE_TOKEN),
                                List.of(
                                    new TypedOptic.Element<>(
                                        DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                                        DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                                        DSL.list(DSL.or(firstNested, DSL.intType())),
                                        DSL.list(DSL.or(firstNested, DSL.intType())),
                                        Optics.proj2()
                                    ),
                                    new TypedOptic.Element<>(
                                        DSL.list(DSL.or(firstNested, DSL.intType())),
                                        DSL.list(DSL.or(firstNested, DSL.intType())),
                                        DSL.list(DSL.or(firstNested, DSL.intType())),
                                        DSL.list(DSL.or(firstNested, DSL.intType())),
                                        Optics.id()
                                    )
                                )
                            )
                        ),
                        Functions.app(
                            Functions.profunctorTransformer(
                                new TypedOptic<>(
                                    TraversalP.Mu.TYPE_TOKEN,
                                    DSL.list(DSL.or(firstNested, DSL.intType())),
                                    DSL.list(DSL.or(firstNested, DSL.intType())),
                                    DSL.or(firstNested, DSL.intType()),
                                    DSL.or(firstNested, DSL.intType()),
                                    Optics.listTraversal()
                                )
                            ),
                            Functions.app(
                                Functions.profunctorTransformer(
                                    new TypedOptic<>(
                                        Cocartesian.Mu.TYPE_TOKEN,
                                        DSL.or(firstNested, DSL.intType()),
                                        DSL.or(firstNested, DSL.intType()),
                                        firstNested,
                                        firstNested,
                                        Optics.inj1()
                                    )
                                ),
                                Functions.fold(firstNested, firstNested, dummyAlgebra, 0)
                            )
                        )
                    )
                )
            )
        );

        final PointFree<Function<
            Either<
                Pair<String, List<Either<Either<Integer, String>, Integer>>>,
                Pair<String, Pair<Either<Either<Integer, String>, Unit>, Either<Integer, String>>>
            >,
            Either<
                Pair<String, List<Either<Either<Integer, String>, String>>>,
                Pair<String, Pair<Either<Either<Integer, String>, Unit>, Either<Integer, String>>>
            >
        >> output = DataFixerUpper.OPTIMIZATION_RULE.rewriteOrNop(input);

        final PointFree<Function<
            Either<
                Pair<String, List<Either<Either<Integer, String>, Integer>>>,
                Pair<String, Pair<Either<Either<Integer, String>, Unit>, Either<Integer, String>>>
            >,
            Either<
                Pair<String, List<Either<Either<Integer, String>, String>>>,
                Pair<String, Pair<Either<Either<Integer, String>, Unit>, Either<Integer, String>>>
            >
        >> expected = Functions.comp(
            Functions.app(
                Functions.profunctorTransformer(
                    new TypedOptic<>(Set.of(Profunctor.Mu.TYPE_TOKEN, TraversalP.Mu.TYPE_TOKEN, Cartesian.Mu.TYPE_TOKEN, Cocartesian.Mu.TYPE_TOKEN),
                        List.of(
                            new TypedOptic.Element<>(
                                DSL.or(DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))), DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested))),
                                DSL.or(DSL.named("first", DSL.list(DSL.or(firstNested, DSL.string()))), DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested))),
                                DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                                DSL.named("first", DSL.list(DSL.or(firstNested, DSL.string()))),
                                Optics.inj1()
                            ),
                            new TypedOptic.Element<>(
                                DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                                DSL.named("first", DSL.list(DSL.or(firstNested, DSL.string()))),
                                DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                                DSL.named("first", DSL.list(DSL.or(firstNested, DSL.string()))),
                                Optics.id()
                            ),
                            new TypedOptic.Element<>(
                                DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))),
                                DSL.named("first", DSL.list(DSL.or(firstNested, DSL.string()))),
                                DSL.list(DSL.or(firstNested, DSL.intType())),
                                DSL.list(DSL.or(firstNested, DSL.string())),
                                Optics.proj2()
                            ),
                            new TypedOptic.Element<>(
                                DSL.list(DSL.or(firstNested, DSL.intType())),
                                DSL.list(DSL.or(firstNested, DSL.string())),
                                DSL.list(DSL.or(firstNested, DSL.intType())),
                                DSL.list(DSL.or(firstNested, DSL.string())),
                                Optics.id()
                            ),
                            new TypedOptic.Element<>(
                                DSL.list(DSL.or(firstNested, DSL.intType())),
                                DSL.list(DSL.or(firstNested, DSL.string())),
                                DSL.or(firstNested, DSL.intType()),
                                DSL.or(firstNested, DSL.string()),
                                Optics.listTraversal()
                            )
                        )
                    )
                ),
                Functions.comp(
                    Functions.app(
                        Functions.profunctorTransformer(
                            new TypedOptic<>(
                                Cocartesian.Mu.TYPE_TOKEN,
                                DSL.or(firstNested, DSL.string()),
                                DSL.or(firstNested, DSL.string()),
                                firstNested,
                                firstNested,
                                Optics.inj1()
                            )
                        ),
                        Functions.fold(firstNested, firstNested, dummyAlgebra, 0)
                    ),
                    Functions.app(
                        Functions.profunctorTransformer(
                            new TypedOptic<>(
                                Set.of(Profunctor.Mu.TYPE_TOKEN, TraversalP.Mu.TYPE_TOKEN, Cartesian.Mu.TYPE_TOKEN, Cocartesian.Mu.TYPE_TOKEN),
                                List.of(
                                    new TypedOptic.Element<>(
                                        DSL.or(firstNested, DSL.intType()),
                                        DSL.or(firstNested, DSL.string()),
                                        DSL.intType(),
                                        DSL.string(),
                                        Optics.inj2()
                                    )
                                )
                            )
                        ),
                        converter
                    )
                )
            ),
            Functions.app(
                Functions.profunctorTransformer(
                    new TypedOptic<>(
                        Set.of(Cocartesian.Mu.TYPE_TOKEN, Profunctor.Mu.TYPE_TOKEN, Cartesian.Mu.TYPE_TOKEN),
                        List.of(
                            new TypedOptic.Element<>(
                                DSL.or(DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))), DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested))),
                                DSL.or(DSL.named("first", DSL.list(DSL.or(firstNested, DSL.intType()))), DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested))),
                                DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                                DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                                Optics.inj2()
                            ),
                            new TypedOptic.Element<>(
                                DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                                DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                                DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                                DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                                Optics.id()
                            ),
                            new TypedOptic.Element<>(
                                DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                                DSL.named("second", DSL.and(DSL.optional(secondNested), firstNested)),
                                DSL.and(DSL.optional(secondNested), firstNested),
                                DSL.and(DSL.optional(secondNested), firstNested),
                                Optics.proj2()
                            ),
                            new TypedOptic.Element<>(
                                DSL.and(DSL.optional(secondNested), firstNested),
                                DSL.and(DSL.optional(secondNested), firstNested),
                                DSL.and(DSL.optional(secondNested), firstNested),
                                DSL.and(DSL.optional(secondNested), firstNested),
                                Optics.id()
                            )
                        )
                    )
                ),
                Functions.comp(
                    Functions.app(
                        Functions.profunctorTransformer(
                            new TypedOptic<>(
                                Set.of(Cartesian.Mu.TYPE_TOKEN, Cocartesian.Mu.TYPE_TOKEN),
                                List.of(
                                    new TypedOptic.Element<>(
                                        DSL.and(DSL.optional(secondNested), firstNested),
                                        DSL.and(DSL.optional(secondNested), firstNested),
                                        DSL.optional(secondNested),
                                        DSL.optional(secondNested),
                                        Optics.proj1()
                                    ),
                                    new TypedOptic.Element<>(
                                        DSL.optional(secondNested),
                                        DSL.optional(secondNested),
                                        secondNested,
                                        secondNested,
                                        Optics.inj1()
                                    )
                                )
                            )
                        ),
                        Functions.fold(firstNested, firstNested, dummyAlgebra, 1)
                    ),
                    Functions.app(
                        Functions.profunctorTransformer(
                            new TypedOptic<>(Cartesian.Mu.TYPE_TOKEN,
                                DSL.and(DSL.optional(secondNested), firstNested),
                                DSL.and(DSL.optional(secondNested), firstNested),
                                firstNested,
                                firstNested,
                                Optics.proj2()
                            )
                        ),
                        Functions.fold(firstNested, firstNested, dummyAlgebra, 0)
                    )
                )
            )
        );
        assertEquals(output, expected);
    }
}
