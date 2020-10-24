package com.mojang.serialization;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

public class EitherErrorReportingTest
{
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

    @Test
    public void testEitherErrorNormal() {
        testEitherError(JsonOps.INSTANCE);
    }

    @Test
    public void testEitherErrorCompressed() {
        testEitherError(JsonOps.COMPRESSED);
    }

    @Test
    public void testEitherErrorSwappedNormal() {
        testEitherErrorSwapped(JsonOps.INSTANCE);
    }

    @Test
    public void testEitherErrorSwappedCompressed() {
        testEitherErrorSwapped(JsonOps.COMPRESSED);
    }

    private static <T> void testEitherError(final DynamicOps<T> ops) {
        final Codec<Either<Integer, TestData>> codec = Codec.either(Codec.intRange(0, 10), TestData.CODEC);
        final T invalidData = ops.createInt(15);
        final DataResult<Either<Integer, TestData>> result = codec.parse(ops, invalidData);

        assertTrue(result.error().isPresent());
        System.out.println(result.error().get().message());
    }

    private static <T> void testEitherErrorSwapped(final DynamicOps<T> ops) {
        final Codec<Either<TestData, Integer>> codec = Codec.either(TestData.CODEC, Codec.intRange(0, 10));
        final T invalidData = ops.createInt(15);
        final DataResult<Either<TestData, Integer>> result = codec.parse(ops, invalidData);

        assertTrue(result.error().isPresent());
        System.out.println(result.error().get().message());
    }
}
