// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CodecTests {
    private static final Codec<String> TO_LOWER_CASE = Codec.STRING.xmap(s -> s.toLowerCase(Locale.ROOT), s -> s.toLowerCase(Locale.ROOT));

    private static <T> Object toJava(final Codec<T> codec, final T value) {
        return codec.encodeStart(JavaOps.INSTANCE, value).getOrThrow(AssertionError::new);
    }

    private static <T> T fromJava(final Codec<T> codec, final Object value) {
        return codec.parse(JavaOps.INSTANCE, value).getOrThrow(AssertionError::new);
    }

    private static <T> T fromJavaOrPartial(final Codec<T> codec, final Object value) {
        return codec.parse(JavaOps.INSTANCE, value).getPartialOrThrow(AssertionError::new);
    }

    private static String fromJavaErrorMessage(final Codec<String> codec, final Object value) {
        return codec.parse(JavaOps.INSTANCE, value).error().orElseThrow(AssertionError::new).message();
    }

    private static void assertFromJavaFails(final Codec<?> codec, final Object value) {
        final DataResult<?> result = codec.parse(JavaOps.INSTANCE, value);
        assertTrue("Expected data result error, but got: " + result.result(), result.isError());
    }

    private static void assertFromJavaFailsPartial(final Codec<?> codec, final Object value) {
        final DataResult<?> result = codec.parse(JavaOps.INSTANCE, value);
        assertTrue("Expected data result error, but got: " + result.resultOrPartial(), result.resultOrPartial().isEmpty());
    }

    private static <T> void assertToJavaFails(final Codec<T> codec, final T value) {
        final DataResult<Object> result = codec.encodeStart(JavaOps.INSTANCE, value);
        assertTrue("Expected data result error, but got: " + result.result(), result.isError());
    }

    private static <T> void assertRoundTrip(final Codec<T> codec, final T value, final Object java) {
        assertEquals(
            java,
            toJava(codec, value)
        );
        assertEquals(
            value,
            fromJava(codec, java)
        );
    }

    private static <T> void assertRoundTrips(final List<Codec<T>> codecs, final T value, final Object java) {
        for (final Codec<T> codec : codecs) {
            assertRoundTrip(codec, value, java);
        }
    }

    @Test
    public void unboundedMap_simple() {
        assertRoundTrip(
            Codec.unboundedMap(Codec.STRING, Codec.INT),
            Map.of(
                "foo", 1,
                "bar", 2
            ),
            Map.of(
                "foo", 1,
                "bar", 2
            )
        );
    }

    @Test
    public void unboundedMap_invalidEntry() {
        final Codec<Map<String, Integer>> codec = Codec.unboundedMap(Codec.STRING, Codec.INT);
        assertFromJavaFails(codec, Map.of(
            "foo", 1,
            "bar", "garbage",
            "baz", 3
        ));
    }

    @Test
    public void unboundedMap_invalidEntryPartial() {
        final Codec<Map<String, Integer>> codec = Codec.unboundedMap(Codec.STRING, Codec.INT);
        assertEquals(
            Map.of(
                "foo", 1,
                "baz", 3
            ),
            fromJavaOrPartial(codec, ImmutableMap.of(
                "foo", 1,
                "bar", "garbage",
                "baz", 3
            ))
        );
    }

    @Test
    public void unboundedMap_invalidEntryNestedPartial() {
        final Codec<Map<String, Map<String, Integer>>> codec = Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, Codec.INT));
        assertEquals(
            Map.of(
                "foo", Map.of(
                    "foo", 1
                ),
                "bar", Map.of(
                    "foo", 1,
                    "baz", 3
                )
            ),
            fromJavaOrPartial(codec, ImmutableMap.of(
                "foo", Map.of(
                    "foo", 1
                ),
                "bar", Map.of(
                    "foo", 1,
                    "bar", "garbage",
                    "baz", 3
                )
            ))
        );
    }

    @Test
    public void unboundedMap_repeatedKeys() {
        final Codec<Map<String, Integer>> codec = Codec.unboundedMap(TO_LOWER_CASE, Codec.INT);
        assertFromJavaFails(codec, Map.of(
            "foo", 1,
            "FOO", 2
        ));
    }

    @Test
    public void unboundedMap_repeatedKeysPartial() {
        final Codec<Map<String, Integer>> codec = Codec.unboundedMap(TO_LOWER_CASE, Codec.INT);
        assertEquals(
            Map.of(
                // The first entry is picked for the partial result
                "foo", 1,
                "bar", 2
            ),
            fromJavaOrPartial(codec, ImmutableMap.of(
                "foo", 1,
                "bar", 2,
                "FOO", 3
            ))
        );
    }

    @Test
    public void list_roundTrip() {
        assertRoundTrip(
            Codec.STRING.listOf(),
            List.of("foo", "bar", "baz"),
            List.of("foo", "bar", "baz")
        );
    }

    @Test
    public void list_invalidValues() {
        final Codec<List<String>> codec = Codec.STRING.listOf();
        assertFromJavaFails(codec, List.of("foo", 2, "baz", false));

        assertEquals(
            List.of("foo", "bar"),
            fromJavaOrPartial(codec, List.of("foo", "bar", 2, false))
        );

        assertEquals(
            List.of("foo", "baz"),
            fromJavaOrPartial(codec, List.of("foo", 2, "baz", false))
        );
    }

    @Test
    public void sizeLimitedList_roundTrip() {
        assertRoundTrip(
            Codec.STRING.sizeLimitedListOf(2),
            List.of("foo", "bar"),
            List.of("foo", "bar")
        );
    }

    @Test
    public void sizeLimitedList_tooLong() {
        final Codec<List<String>> codec = Codec.STRING.sizeLimitedListOf(2);
        assertFromJavaFails(codec, List.of("foo", "bar", "baz"));
        assertToJavaFails(codec, List.of("foo", "bar", "baz"));

        // Input is clipped in partial result
        assertEquals(
            List.of("foo", "bar"),
            fromJavaOrPartial(codec, List.of("foo", "bar", "baz"))
        );
    }

    @Test
    public void sizeLimitedList_tooLongWithInvalid() {
        final Codec<List<String>> codec = Codec.STRING.sizeLimitedListOf(2);

        // Input is clipped only by valid entries
        assertEquals(
            List.of("foo", "bar"),
            fromJavaOrPartial(codec, List.of("foo", 2, "bar", "baz", false))
        );
    }

    @Test
    public void sizeLimitedList_tooShort() {
        final Codec<List<String>> codec = Codec.STRING.listOf(2, 3);
        assertToJavaFails(codec, List.of("foo"));
        // We can't get any partial result if the data is too short
        assertFromJavaFailsPartial(codec, List.of("foo"));

        assertRoundTrip(codec, List.of("foo", "bar"), List.of("foo", "bar"));
        assertRoundTrip(codec, List.of("foo", "bar", "baz"), List.of("foo", "bar", "baz"));
    }

    @Test
    public void sizeLimitedList_tooShortWithInvalid() {
        final Codec<List<String>> codec = Codec.STRING.listOf(2, 3);
        assertFromJavaFailsPartial(codec, List.of("foo", 1, 2));

        assertEquals(
            List.of("foo", "bar"),
            fromJavaOrPartial(codec, List.of("foo", 2, "bar", 3))
        );
    }

    @Test
    public void withAlternative_simple() {
        final Codec<String> codec = Codec.withAlternative(Codec.STRING, Codec.INT, integer -> "integer:" + integer);
        assertRoundTrip(codec, "string", "string");
        assertEquals("integer:23", fromJava(codec, 23));
        // Alternative is only used for reads
        assertEquals("integer:4", toJava(codec, "integer:4"));

        assertFromJavaFails(codec, Map.of());
        assertFromJavaFails(codec, false);
    }

    public static final Codec<String> NEVER_PRIMARY = Codec.STRING.validate(s -> DataResult.error(() -> "Failed Primary"));
    public static final Codec<String> NEVER_ALTERNATIVE = Codec.STRING.validate(s -> DataResult.error(() -> "Failed Alternative"));
    public static final Codec<String> NEVER_WITH_PARTIAL_PRIMARY = Codec.STRING.validate(s -> DataResult.error(() -> "Failed Primary with partial", "Partial Primary: " + s));
    public static final Codec<String> NEVER_WITH_PARTIAL_ALTERNATIVE = Codec.STRING.validate(s -> DataResult.error(() -> "Failed Alternative with partial", "Partial Alternative: " + s));

    @Test
    public void withAlternative_primaryPartialAlternativeFails() {
        final Codec<String> codec = Codec.withAlternative(
            NEVER_WITH_PARTIAL_PRIMARY,
            NEVER_ALTERNATIVE
        );
        assertEquals(
            "Partial Primary: value",
            fromJavaOrPartial(codec, "value")
        );

        assertEquals(
            "Failed Primary with partial",
            fromJavaErrorMessage(codec, "value")
        );
    }

    @Test
    public void withAlternative_primaryFailsAlternativePartial() {
        final Codec<String> codec = Codec.withAlternative(
            NEVER_PRIMARY,
            NEVER_WITH_PARTIAL_ALTERNATIVE
        );
        assertEquals(
            "Partial Alternative: value",
            fromJavaOrPartial(codec, "value")
        );

        assertEquals(
            "Failed Alternative with partial",
            fromJavaErrorMessage(codec, "value")
        );
    }

    @Test
    public void withAlternative_bothPartialPrefersPrimary() {
        final Codec<String> codec = Codec.withAlternative(
            NEVER_WITH_PARTIAL_PRIMARY,
            NEVER_WITH_PARTIAL_ALTERNATIVE
        );
        assertEquals(
            "Partial Primary: value",
            fromJavaOrPartial(codec, "value")
        );
    }

    @Test
    public void withAlternative_bothFail() {
        final Codec<String> codec = Codec.withAlternative(
            NEVER_PRIMARY,
            NEVER_ALTERNATIVE
        );
        assertEquals(
            "Failed to parse either. First: Failed Primary; Second: Failed Alternative",
            fromJavaErrorMessage(codec, "value")
        );
    }

    @Test
    public void withAlternative_bothSuccessful() {
        final Codec<String> codec = Codec.withAlternative(Codec.STRING, TO_LOWER_CASE);
        assertRoundTrip(codec, "string", "string");

        // Primary codec is chosen over alternative
        assertRoundTrip(codec, "STRING", "STRING");
    }

    private record Node(String value, Optional<Node> next) {
        public static final Codec<Node> CODEC = Codec.recursive("Node", self ->
            RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("value").forGetter(o -> o.value),
                self.optionalFieldOf("next").forGetter(o -> o.next)
            ).apply(i, Node::new))
        );

        public void toList(final List<String> output) {
            output.add(value);
            next.ifPresent(l -> l.toList(output));
        }

        public List<String> toList() {
            final List<String> result = new ArrayList<>();
            toList(result);
            return result;
        }

        private static Optional<Node> create(final Iterator<String> values) {
            if (values.hasNext()) {
                final String value = values.next();
                final Optional<Node> next = create(values);
                return Optional.of(new Node(value, next));
            }
            return Optional.empty();
        }

        public static void assertParsingEquals(final List<String> asList, final Object asData) {
            testDecode(asList, asData);
            testEncode(asList, asData);
        }

        private static void testDecode(final List<String> expected, final Object asData) {
            assertEquals(expected, fromJava(CODEC, asData).toList());
        }

        private static void testEncode(final List<String> asList, final Object expected) {
            final Node fromList = create(asList.iterator()).orElseThrow(AssertionError::new);
            assertEquals(expected, toJava(CODEC, fromList));
        }
    }

    @Test
    public void selfRecursive() {
        Node.assertParsingEquals(List.of("a"), Map.of("value", "a"));
        Node.assertParsingEquals(List.of("a", "b"), Map.of("value", "a", "next", Map.of("value", "b")));
        Node.assertParsingEquals(List.of("a", "b", "c"), Map.of("value", "a", "next", Map.of("value", "b", "next", Map.of("value", "c"))));
    }

    private record Left(Optional<Right> next) {
        private static final Codec<Left> CODEC = RecordCodecBuilder.create(i -> i.group(
            Right.CODEC.optionalFieldOf("next").forGetter(o -> o.next)
        ).apply(i, Left::new));

        public int count() {
            return 1 + next.map(Right::depth).orElse(0);
        }

        public static Optional<Left> create(final int length) {
            return length == 0 ? Optional.empty() : Optional.of(new Left(Right.create(length - 1)));
        }
    }

    private record Right(Optional<Left> next) {
        private static final Codec<Right> CODEC = Codec.recursive("Right", self ->
            RecordCodecBuilder.create(i -> i.group(
                Left.CODEC.optionalFieldOf("next").forGetter(o -> o.next)
            ).apply(i, Right::new))
        );

        public int depth() {
            return 1 + next.map(Left::count).orElse(0);
        }

        public static Optional<Right> create(final int depth) {
            return depth == 0 ? Optional.empty() : Optional.of(new Right(Left.create(depth - 1)));
        }

        public static Map<String, Object> createChain(final int depth) {
            return depth == 1 ? Map.of() : Map.of("next", createChain(depth - 1));
        }

        public static void assertParsingAtDepth(final int depth) {
            final Map<String, Object> asData = createChain(depth);
            testDecode(depth, asData);
            testEncode(depth, asData);
        }

        private static void testDecode(final int depth, final Map<String, Object> asData) {
            final Right parsed = fromJava(CODEC, asData);
            assertEquals(depth, parsed.depth());
        }

        private static void testEncode(final int depth, final Map<String, Object> asData) {
            final Right fresh = create(depth).orElseThrow(AssertionError::new);
            assertEquals(asData, toJava(CODEC, fresh));
        }
    }

    @Test
    public void mutuallyRecursiveCodecTest() {
        Right.assertParsingAtDepth(1);
        Right.assertParsingAtDepth(2);
        Right.assertParsingAtDepth(3);
    }

    private enum Variant {
        FOO,
        BAR,
        ;

        public static final Codec<Variant> CODEC = Codec.stringResolver(
            variant -> switch (variant) {
                case FOO -> "foo";
                case BAR -> "bar";
            },
            string -> switch (string) {
                case "foo" -> FOO;
                case "bar" -> BAR;
                default -> null;
            }
        );
    }

    @Test
    public void stringResolver_simple() {
        assertRoundTrip(Variant.CODEC, Variant.FOO, "foo");
        assertRoundTrip(Variant.CODEC, Variant.BAR, "bar");
        assertFromJavaFails(Variant.CODEC, "baz");
    }

    private enum MapDispatchType {
        ANY("any", Codec.STRING),
        LOWER_CASE("lower_case", Codec.STRING.validate(s -> s.toLowerCase(Locale.ROOT).equals(s) ? DataResult.success(s) : DataResult.error(() -> "Not lower case: " + s))),
        UPPER_CASE("upper_case", Codec.STRING.validate(s -> s.toUpperCase(Locale.ROOT).equals(s) ? DataResult.success(s) : DataResult.error(() -> "Not upper case: " + s))),
        NEVER("never", Codec.STRING.validate(s -> DataResult.error(() -> "No"))),
        NEVER_WITH_PARTIAL("never_with_partial", Codec.STRING.validate(s -> DataResult.error(() -> "No", s)))
        ;

        public static final Codec<MapDispatchType> CODEC = Codec.stringResolver(MapDispatchType::getSerializedName, MapDispatchType::lookup);
        public static final Codec<MapDispatchType> CASE_INSENSITIVE_CODEC = Codec.stringResolver(MapDispatchType::getSerializedName, string -> lookup(string.toLowerCase(Locale.ROOT)));

        private final String name;
        private final Codec<String> codec;

        MapDispatchType(final String name, final Codec<String> codec) {
            this.name = name;
            this.codec = codec;
        }

        @Nullable
        private static MapDispatchType lookup(final String name) {
            for (final MapDispatchType type : values()) {
                if (type.getSerializedName().equals(name)) {
                    return type;
                }
            }
            return null;
        }

        public String getSerializedName() {
            return name;
        }
    }

    private static final Codec<Map<MapDispatchType, String>> DISPATCHED_MAP_CODEC = Codec.dispatchedMap(MapDispatchType.CODEC, t -> t.codec);

    @Test
    public void dispatchedMap_encode() {
        assertEquals(
            Map.of(
                "any", "Some text",
                "lower_case", "very quietly",
                "upper_case", "NOT SHOUTING"
            ),
            toJava(DISPATCHED_MAP_CODEC, Map.of(
                MapDispatchType.ANY, "Some text",
                MapDispatchType.LOWER_CASE, "very quietly",
                MapDispatchType.UPPER_CASE, "NOT SHOUTING"
            ))
        );
    }

    @Test
    public void dispatchedMap_decode() {
        assertEquals(
            Map.of(
                MapDispatchType.ANY, "Some text",
                MapDispatchType.LOWER_CASE, "very quietly",
                MapDispatchType.UPPER_CASE, "NOT SHOUTING"
            ),
            fromJava(DISPATCHED_MAP_CODEC, Map.of(
                "any", "Some text",
                "lower_case", "very quietly",
                "upper_case", "NOT SHOUTING"
            ))
        );
    }

    @Test
    public void dispatchedMap_decodeInvalidType() {
        assertFromJavaFails(DISPATCHED_MAP_CODEC, Map.of(
            "invalid", "Some text"
        ));
    }

    @Test
    public void dispatchedMap_decodeInvalidValue() {
        assertFromJavaFails(DISPATCHED_MAP_CODEC, Map.of(
            "lower_case", "SHOUTING"
        ));
    }

    @Test
    public void dispatchedMap_decodePartialResult() {
        assertEquals(
            Map.of(
                MapDispatchType.ANY, "Some text",
                MapDispatchType.UPPER_CASE, "NOT SHOUTING"
            ),
            fromJavaOrPartial(DISPATCHED_MAP_CODEC, Map.of(
                "any", "Some text",
                "invalid", "",
                "lower_case", "SHOUTING",
                "upper_case", "NOT SHOUTING"
            ))
        );

        assertEquals(
            Map.of(
                MapDispatchType.ANY, "Some text",
                MapDispatchType.UPPER_CASE, "NOT SHOUTING"
            ),
            fromJavaOrPartial(DISPATCHED_MAP_CODEC, Map.of(
                "invalid", "",
                "any", "Some text",
                "upper_case", "NOT SHOUTING"
            ))
        );
    }

    @Test
    public void dispatchedMap_decodeNestedPartialResult() {
        assertEquals(
            Map.of(
                MapDispatchType.NEVER_WITH_PARTIAL, "Fails with partial result",
                MapDispatchType.ANY, "Something else"
            ),
            fromJavaOrPartial(DISPATCHED_MAP_CODEC, Map.of(
                "never_with_partial", "Fails with partial result",
                "any", "Something else"
            ))
        );
    }

    @Test
    public void dispatchedMap_decodeRepeatedEntries() {
        final Codec<Map<MapDispatchType, String>> dispatchedMapCodec = Codec.dispatchedMap(MapDispatchType.CASE_INSENSITIVE_CODEC, t -> t.codec);

        assertFromJavaFails(dispatchedMapCodec, Map.of(
            "lower_case", "first",
            "LOWER_CASE", "second"
        ));

        assertEquals(
            Map.of(
                MapDispatchType.LOWER_CASE, "first"
            ),
            fromJavaOrPartial(dispatchedMapCodec, ImmutableMap.of(
                "lower_case", "first",
                "LOWER_CASE", "second"
            ))
        );
    }

    private record SimpleOptionals(
        Optional<String> string,
        Optional<Integer> integer
    ) {
        public static final Codec<SimpleOptionals> STRICT_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("string").forGetter(SimpleOptionals::string),
            Codec.INT.optionalFieldOf("integer").forGetter(SimpleOptionals::integer)
        ).apply(i, SimpleOptionals::new));

        public static final Codec<SimpleOptionals> LENIENT_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.lenientOptionalFieldOf("string").forGetter(SimpleOptionals::string),
            Codec.INT.lenientOptionalFieldOf("integer").forGetter(SimpleOptionals::integer)
        ).apply(i, SimpleOptionals::new));
    }

    @Test
    public void optionalField_roundTrip() {
        assertRoundTrips(
            List.of(SimpleOptionals.STRICT_CODEC, SimpleOptionals.LENIENT_CODEC),
            new SimpleOptionals(Optional.of("foo"), Optional.of(1)),
            Map.of(
                "string", "foo",
                "integer", 1
            )
        );
        assertRoundTrips(
            List.of(SimpleOptionals.STRICT_CODEC, SimpleOptionals.LENIENT_CODEC),
            new SimpleOptionals(Optional.empty(), Optional.of(1)),
            Map.of(
                "integer", 1
            )
        );
    }

    @Test
    public void optionalField_strictInvalidValues() {
        assertFromJavaFails(
            SimpleOptionals.STRICT_CODEC,
            Map.of("string", 54)
        );
        assertFromJavaFails(
            SimpleOptionals.STRICT_CODEC,
            Map.of("integer", "not an int")
        );
    }

    @Test
    public void optionalField_strictInvalidValuesPartial() {
        assertEquals(
            new SimpleOptionals(Optional.empty(), Optional.of(23)),
            fromJavaOrPartial(SimpleOptionals.STRICT_CODEC, Map.of(
                "string", false,
                "integer", 23
            ))
        );
    }

    @Test
    public void optionalField_lenientInvalidValues() {
        assertEquals(
            new SimpleOptionals(Optional.empty(), Optional.of(23)),
            fromJava(SimpleOptionals.LENIENT_CODEC, Map.of(
                "string", false,
                "integer", 23
            ))
        );
    }

    private record NestedStrictOptionals(
        Optional<SimpleOptionals> nested
    ) {
        public static final Codec<NestedStrictOptionals> TOP_LEVEL_STRICT_CODEC = RecordCodecBuilder.create(i -> i.group(
            SimpleOptionals.STRICT_CODEC.optionalFieldOf("nested").forGetter(NestedStrictOptionals::nested)
        ).apply(i, NestedStrictOptionals::new));

        public static final Codec<NestedStrictOptionals> TOP_LEVEL_LENIENT_CODEC = RecordCodecBuilder.create(i -> i.group(
            SimpleOptionals.STRICT_CODEC.lenientOptionalFieldOf("nested").forGetter(NestedStrictOptionals::nested)
        ).apply(i, NestedStrictOptionals::new));
    }

    @Test
    public void optionalField_nestedStrictOptionals() {
        assertEquals(
            new NestedStrictOptionals(
                Optional.of(new SimpleOptionals(
                    Optional.of("foo"),
                    Optional.of(1)
                ))
            ),
            fromJava(NestedStrictOptionals.TOP_LEVEL_STRICT_CODEC, Map.of(
                "nested", Map.of(
                    "string", "foo",
                    "integer", 1
                )
            ))
        );
    }

    @Test
    public void optionalField_nestedStrictOptionalsPartialResult() {
        assertEquals(
            new NestedStrictOptionals(
                Optional.of(new SimpleOptionals(
                    Optional.of("foo"),
                    Optional.empty()
                ))
            ),
            fromJavaOrPartial(NestedStrictOptionals.TOP_LEVEL_STRICT_CODEC, Map.of(
                "nested", Map.of(
                    "string", "foo",
                    "integer", "not an int"
                )
            ))
        );

        assertEquals(
            new NestedStrictOptionals(
                Optional.empty()
            ),
            fromJava(NestedStrictOptionals.TOP_LEVEL_LENIENT_CODEC, Map.of(
                "nested", Map.of(
                    "string", "foo",
                    "integer", "not an int"
                )
            ))
        );
    }

    private record Simple(String string, int integer) {
        public static final Codec<Simple> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("string").forGetter(Simple::string),
            Codec.INT.fieldOf("integer").forGetter(Simple::integer)
        ).apply(i, Simple::new));
    }

    @Test
    public void assumeMap_recordCodec() {
        assertRoundTrips(
            List.of(
                Simple.CODEC,
                // Wrapped directly
                MapCodec.assumeMapUnsafe(Simple.CODEC).codec(),
                // From a non-MapCodecCodec
                MapCodec.assumeMapUnsafe(obfuscateCodecType(Simple.CODEC)).codec()
            ),
            new Simple("hello", 1),
            Map.of(
                "string", "hello",
                "integer", 1
            )
        );

        assertFromJavaFails(
            MapCodec.assumeMapUnsafe(Simple.CODEC).codec(),
            "not a map"
        );
    }

    private static <A> Codec<A> obfuscateCodecType(final Codec<A> codec) {
        return Codec.of(codec, codec);
    }

    @Test
    public void assumeMap_primitiveCodec() {
        final Codec<Integer> codec = MapCodec.assumeMapUnsafe(Codec.INT).codec();
        assertFromJavaFails(codec, 123);
        assertToJavaFails(codec, 123);
    }
}
