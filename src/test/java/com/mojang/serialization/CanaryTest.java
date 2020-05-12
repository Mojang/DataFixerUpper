package com.mojang.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CanaryTest {
    private static final Codec<InnerTestClass> INNER_CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.fieldOf("a").forGetter(o -> o.a),
        Codec.INT.fieldOf("b").forGetter(o -> o.b)
    ).apply(i, InnerTestClass::new));

    private static final Codec<OuterTestClass> OUTER_CODEC = RecordCodecBuilder.create(i -> i.group(
        INNER_CODEC.fieldOf("composite").forGetter(o -> o.composite),
        Codec.STRING.listOf().fieldOf("list").forGetter(o -> o.list)
    ).apply(i, OuterTestClass::new));

    public static class InnerTestClass {
        public final int a;
        public final int b;

        public InnerTestClass(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    public static class OuterTestClass {
        public final InnerTestClass composite;
        public final List<String> list;

        public OuterTestClass(final InnerTestClass composite, final List<String> list) {
            this.composite = composite;
            this.list = list;
        }
    }

    @Test
    public void sanitySerializationTest() {
        final Function<OuterTestClass, DataResult<JsonElement>> f = JsonOps.INSTANCE.withEncoder(OUTER_CODEC);
        final DataResult<JsonElement> result = f.apply(new OuterTestClass(new InnerTestClass(1, 2), Arrays.asList("a", "b", "c")));
        final Optional<JsonElement> contents = result.result();
        Assert.assertTrue(contents.isPresent());

        final JsonObject expected = createExampleJson();
        Assert.assertEquals(expected, contents.get());
    }

    @Test
    public void sanityDeserializationTest() {
        final Function<JsonElement, DataResult<OuterTestClass>> f = JsonOps.INSTANCE.withParser(OUTER_CODEC);
        final DataResult<OuterTestClass> result = f.apply(createExampleJson());
        final Optional<OuterTestClass> contents = result.get().left();
        Assert.assertTrue(contents.isPresent());

        final OuterTestClass outer = contents.get();
        Assert.assertEquals(Arrays.asList("a", "b", "c"), outer.list);
        final InnerTestClass inner = outer.composite;
        Assert.assertEquals(1, inner.a);
        Assert.assertEquals(2, inner.b);
    }

    private static JsonObject createExampleJson() {
        final JsonObject expected = new JsonObject();
        final JsonArray expectedList = new JsonArray();
        expectedList.add("a");
        expectedList.add("b");
        expectedList.add("c");
        expected.add("list", expectedList);

        final JsonObject expectedInner = new JsonObject();
        expectedInner.addProperty("a", 1);
        expectedInner.addProperty("b", 2);
        expected.add("composite", expectedInner);
        return expected;
    }

}