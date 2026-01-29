package com.mojang.serialization;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.mojang.serialization.TestUtils.*;
import static org.junit.Assert.assertEquals;

public class NullableCodecTests {

    private record SimpleNullables(
            @Nullable String string,
            @Nullable Integer integer
    ) {
        public static final Codec<SimpleNullables> STRICT_CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.nullableFieldOf("string").forGetter(SimpleNullables::string),
                Codec.INT.nullableFieldOf("integer").forGetter(SimpleNullables::integer)
        ).apply(i, SimpleNullables::new));

        public static final Codec<SimpleNullables> LENIENT_CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.lenientNullableFieldOf("string").forGetter(SimpleNullables::string),
                Codec.INT.lenientNullableFieldOf("integer").forGetter(SimpleNullables::integer)
        ).apply(i, SimpleNullables::new));
    }

    @Test
    public void nullableField_roundTrip() {
        assertRoundTrips(
                List.of(SimpleNullables.STRICT_CODEC, SimpleNullables.LENIENT_CODEC),
                new SimpleNullables("foo", 1),
                Map.of(
                        "string", "foo",
                        "integer", 1
                )
        );
        assertRoundTrips(
                List.of(SimpleNullables.STRICT_CODEC, SimpleNullables.LENIENT_CODEC),
                new SimpleNullables(null, 1),
                Map.of(
                        "integer", 1
                )
        );
    }

    @Test
    public void nullableField_strictInvalidValues() {
        assertFromJavaFails(
                SimpleNullables.STRICT_CODEC,
                Map.of("string", 54)
        );
        assertFromJavaFails(
                SimpleNullables.STRICT_CODEC,
                Map.of("integer", "not an int")
        );
    }

    @Test
    public void nullableField_strictInvalidValuesPartial() {
        assertEquals(
                new SimpleNullables(null, 23),
                fromJavaOrPartial(SimpleNullables.STRICT_CODEC, Map.of(
                        "string", false,
                        "integer", 23
                ))
        );
    }

    @Test
    public void nullableField_lenientInvalidValues() {
        assertEquals(
                new SimpleNullables(null, 23),
                fromJava(SimpleNullables.LENIENT_CODEC, Map.of(
                        "string", false,
                        "integer", 23
                ))
        );
    }

    private record NestedStrictNullables(
            @Nullable SimpleNullables nested
    ) {
        public static final Codec<NestedStrictNullables> TOP_LEVEL_STRICT_CODEC = RecordCodecBuilder.create(i -> i.group(
                SimpleNullables.STRICT_CODEC.nullableFieldOf("nested").forGetter(NestedStrictNullables::nested)
        ).apply(i, NestedStrictNullables::new));

        public static final Codec<NestedStrictNullables> TOP_LEVEL_LENIENT_CODEC = RecordCodecBuilder.create(i -> i.group(
                SimpleNullables.STRICT_CODEC.lenientNullableFieldOf("nested").forGetter(NestedStrictNullables::nested)
        ).apply(i, NestedStrictNullables::new));
    }

    @Test
    public void nullableField_nestedStrictOptionals() {
        assertEquals(
                new NestedStrictNullables(
                        new SimpleNullables(
                                "foo",
                                1
                        )
                ),
                fromJava(NestedStrictNullables.TOP_LEVEL_STRICT_CODEC, Map.of(
                        "nested", Map.of(
                                "string", "foo",
                                "integer", 1
                        )
                ))
        );
    }

    @Test
    public void nullableField_nestedStrictOptionalsPartialResult() {
        assertEquals(
                new NestedStrictNullables(
                        new SimpleNullables(
                                "foo",
                                null
                        )
                ),
                fromJavaOrPartial(NestedStrictNullables.TOP_LEVEL_STRICT_CODEC, Map.of(
                        "nested", Map.of(
                                "string", "foo",
                                "integer", "not an int"
                        )
                ))
        );

        assertEquals(
                new NestedStrictNullables(
                        null
                ),
                fromJava(NestedStrictNullables.TOP_LEVEL_LENIENT_CODEC, Map.of(
                        "nested", Map.of(
                                "string", "foo",
                                "integer", "not an int"
                        )
                ))
        );
    }
}
