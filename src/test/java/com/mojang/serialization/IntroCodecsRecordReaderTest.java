package com.mojang.serialization;

// Potential points for comment:
// Introduce here a reference to scodec ?
// Re-iterate on the dependencies between DFU s + Dynamics + Codecs here?

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

record PlaceholderRecord(String name, int age) {}

public class IntroCodecsRecordReaderTest {
        public IntroCodecsRecordReaderTest() {}

        // Define the Codec around here

        @Test
        public void FailOnPurpose() {
                assertThat("Works", false, is(true));
        }
}
