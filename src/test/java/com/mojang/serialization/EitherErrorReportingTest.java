// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import java.util.stream.Stream;

import com.google.gson.JsonElement;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

public class EitherErrorReportingTest {
    private static class TestData {
        private static final Codec<TestData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(c -> c.x)
        ).apply(instance, TestData::new));

        private final int x;

        private TestData(final int x) {
            this.x = x;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final TestData testData = (TestData) o;
            return x == testData.x;
        }

        @Override
        public int hashCode() {
            return x;
        }
    }

    private static final Codec<Either<Integer, TestData>> CODEC = Codec.either(Codec.intRange(0, 10), TestData.CODEC);
    private static final Codec<Either<TestData, Integer>> CODEC_SWAPPED = Codec.either(TestData.CODEC, Codec.intRange(0, 10));
    private static final Codec<Either<Integer, TestData>> MAP_CODEC = Codec.mapEither(Codec.intRange(0, 10).fieldOf("value"), TestData.CODEC.fieldOf("data")).codec();
    private static final Codec<Either<TestData, Integer>> MAP_CODEC_SWAPPED = Codec.mapEither(TestData.CODEC.fieldOf("data"), Codec.intRange(0, 10).fieldOf("value")).codec();

    @Test
    public void testEitherNormal() {
        final JsonElement invalidData = JsonOps.INSTANCE.createInt(15);
        final DataResult<Either<Integer, TestData>> result = CODEC.parse(JsonOps.INSTANCE, invalidData);

        assertTrue(result.error().isPresent());
        System.out.println(result.error().get().message());
    }

    @Test
    public void testEitherSwappedNormal() {
        final JsonElement invalidData = JsonOps.INSTANCE.createInt(15);
        final DataResult<Either<TestData, Integer>> result = CODEC_SWAPPED.parse(JsonOps.INSTANCE, invalidData);

        assertTrue(result.error().isPresent());
        System.out.println(result.error().get().message());
    }

    @Test
    public void testMapEitherNormal() {
        final DynamicOps<JsonElement> ops = JsonOps.INSTANCE;
        final JsonElement invalidData = ops.createMap(Stream.of(Pair.of(ops.createString("value"), ops.createInt(15))));
        final DataResult<Either<Integer, TestData>> result = MAP_CODEC.parse(ops, invalidData);

        assertTrue(result.error().isPresent());
        System.out.println(result.error().get().message());
    }

    @Test
    public void testMapEitherNormal2() {
        final DynamicOps<JsonElement> ops = JsonOps.INSTANCE;
        final JsonElement invalidData = ops.createMap(Stream.of(Pair.of(ops.createString("data"), ops.createString("oops"))));
        final DataResult<Either<Integer, TestData>> result = MAP_CODEC.parse(ops, invalidData);

        assertTrue(result.error().isPresent());
        System.out.println(result.error().get().message());
    }

    @Test
    public void testMapEitherSwappedNormal() {
        final DynamicOps<JsonElement> ops = JsonOps.INSTANCE;
        final JsonElement invalidData = ops.createMap(Stream.of(Pair.of(ops.createString("value"), ops.createInt(15))));
        final DataResult<Either<TestData, Integer>> result = MAP_CODEC_SWAPPED.parse(ops, invalidData);

        assertTrue(result.error().isPresent());
        System.out.println(result.error().get().message());
    }

    @Test
    public void testMapEitherSwappedNormal2() {
        final DynamicOps<JsonElement> ops = JsonOps.INSTANCE;
        final JsonElement invalidData = ops.createMap(Stream.of(Pair.of(ops.createString("data"), ops.createString("oops"))));
        final DataResult<Either<TestData, Integer>> result = MAP_CODEC_SWAPPED.parse(ops, invalidData);

        assertTrue(result.error().isPresent());
        System.out.println(result.error().get().message());
    }
}
