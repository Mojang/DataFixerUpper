package com.mojang.datafixers;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JavaOps;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class DataFixerTests {
    enum Types implements DSL.TypeReference {
        NESTED("nested"),
        LISTS("lists"),
        VALUE("value");

        private final String typeName;
        Types(String typeName) {
            this.typeName = typeName;
        }

        @Override public String typeName() {
            return typeName;
        }
    }

    static class OldSchema extends Schema
    {
        public OldSchema() {
            super(10, null);
        }
        @Override
        public Map<String, Supplier<TypeTemplate>> registerEntities(Schema unused) {
            return Map.of();
        }
        @Override
        public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema unused) {
            return Map.of();
        }
        @Override
        public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entities, Map<String, Supplier<TypeTemplate>> blockEntities) {
            schema.registerType(true, Types.NESTED, () -> DSL.and(
                DSL.or(DSL.field("nested", Types.NESTED.in(schema)), DSL.emptyPart()),
                DSL.field("data", Types.LISTS.in(schema))
            ));
            schema.registerType(true, Types.LISTS, () -> DSL.list(DSL.or(Types.LISTS.in(schema), Types.VALUE.in(schema))));
            schema.registerType(false, Types.VALUE, () -> DSL.constType(DSL.intType()));
        }
    }

    static class NewSchema extends Schema {
        public NewSchema(OldSchema parent) {
            super(20, parent);
        }
        @Override
        public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entities, Map<String, Supplier<TypeTemplate>> blockEntities) {
            super.registerTypes(schema, entities, blockEntities);
            schema.registerType(false, Types.VALUE, () -> DSL.constType(DSL.string()));
        }
    }

    static class TestRule extends DataFix
    {
        private static final String[] CONVERSIONS = new String[] {
            "Zero",
            "One",
            "Two",
            "Three",
            "Four",
            "Five",
            "Six",
            "Seven",
            "Eight",
            "Nine",
            "Ten"
        };

        public TestRule(NewSchema schema) {
            super(schema, true);
        }
        @Override public TypeRewriteRule makeRule() {
            @SuppressWarnings("unchecked")
            final Type<Pair<String, Integer>> oldType = (Type<Pair<String, Integer>>) getInputSchema().getType(Types.VALUE);
            @SuppressWarnings("unchecked")
            final Type<Pair<String, String>> newType = (Type<Pair<String, String>>)getOutputSchema().getType(Types.VALUE);
            return this.fixTypeEverywhereTyped("TestRule", oldType, newType, typed -> {
                @SuppressWarnings("unchecked")
                final Pair<String, Integer> pair = (Pair<String, Integer>)typed.getValue();
                return new Typed<>(newType, typed.getOps(), pair.mapSecond(value -> value < 0 || value > 10 ? "Unknown" : CONVERSIONS[value]));
            });
        }
    }

    @Test
    public void test() {
        final DataFixerBuilder builder = new DataFixerBuilder(300);
        final OldSchema oldSchema = new OldSchema();
        final NewSchema newSchema = new NewSchema(oldSchema);
        builder.addSchema(oldSchema);
        builder.addSchema(newSchema);
        builder.addFixer(new TestRule(newSchema));

        final DataFixer dataFixer = builder.build().fixer();
        final Map<String, Object> input = Map.of(
            "data", List.of(1, 2, 3),
            "nested", Map.of(
                "data", List.of(List.of(4), List.of(5), List.of(6)),
                "nested", Map.of(
                    "data", List.of(7, 8, List.of(9, 10, List.of(11, 0, -1)))
                )
            )
        );
        final Object fixed = dataFixer.update(Types.NESTED, new Dynamic<>(JavaOps.INSTANCE, input), 1, 3).getValue();

        final Map<String, Object> expected = Map.of(
            "data", List.of("One", "Two", "Three"),
            "nested", Map.of(
                "data", List.of(List.of("Four"), List.of("Five"), List.of("Six")),
                "nested", Map.of(
                    "data", List.of("Seven", "Eight", List.of("Nine", "Ten", List.of("Unknown", "Zero", "Unknown")))
                )
            )
        );
        assertEquals(fixed, expected);
    }
}