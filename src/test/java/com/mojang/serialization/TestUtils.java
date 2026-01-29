package com.mojang.serialization;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Utility methods for testing codecs.
 */
public class TestUtils {

    public static <T> Object toJava(final Codec<T> codec, final T value) {
        return codec.encodeStart(JavaOps.INSTANCE, value).getOrThrow(AssertionError::new);
    }

    public static <T> T fromJava(final Codec<T> codec, final Object value) {
        return codec.parse(JavaOps.INSTANCE, value).getOrThrow(AssertionError::new);
    }

    public static <T> T fromJavaOrPartial(final Codec<T> codec, final Object value) {
        return codec.parse(JavaOps.INSTANCE, value).getPartialOrThrow(AssertionError::new);
    }

    public static String fromJavaErrorMessage(final Codec<String> codec, final Object value) {
        return codec.parse(JavaOps.INSTANCE, value).error().orElseThrow(AssertionError::new).message();
    }

    public static void assertFromJavaFails(final Codec<?> codec, final Object value) {
        final DataResult<?> result = codec.parse(JavaOps.INSTANCE, value);
        assertTrue("Expected data result error, but got: " + result.result(), result.isError());
    }

    public static void assertFromJavaFailsPartial(final Codec<?> codec, final Object value) {
        final DataResult<?> result = codec.parse(JavaOps.INSTANCE, value);
        assertTrue("Expected data result error, but got: " + result.resultOrPartial(), result.resultOrPartial().isEmpty());
    }

    public static <T> void assertToJavaFails(final Codec<T> codec, final T value) {
        final DataResult<Object> result = codec.encodeStart(JavaOps.INSTANCE, value);
        assertTrue("Expected data result error, but got: " + result.result(), result.isError());
    }

    public static <T> void assertRoundTrip(final Codec<T> codec, final T value, final Object java) {
        assertEquals(
                java,
                toJava(codec, value)
        );
        assertEquals(
                value,
                fromJava(codec, java)
        );
    }

    public static <T> void assertRoundTrips(final List<Codec<T>> codecs, final T value, final Object java) {
        for (final Codec<T> codec : codecs) {
            assertRoundTrip(codec, value, java);
        }
    }
}
