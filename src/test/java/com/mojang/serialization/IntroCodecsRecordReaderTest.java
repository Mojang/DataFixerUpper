package com.mojang.serialization;

import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

// Suggested reading material
// http://scodec.org

record PersonRecord(String name, int age) {}

public class IntroCodecsRecordReaderTest {
        public IntroCodecsRecordReaderTest() {}

        static final Codec<PersonRecord> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("name").forGetter(PersonRecord::name),
                Codec.INT.fieldOf("age").forGetter(PersonRecord::age)
        ).apply(i, PersonRecord::new));

        private Path GetResourcePath(String resource_name) {
                final ClassLoader classLoader = getClass().getClassLoader();
                return Path.of(Objects.requireNonNull(classLoader.getResource(resource_name)).getPath());
        }

        @Test
        public void CanDeserializeAPersonRecord() throws IOException {
                final Path path = GetResourcePath("IntroCodecsRecordReader_00.json");
                try (final JsonReader reader = new JsonReader(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
                        reader.setLenient(true);
                        final DataResult<PersonRecord> dr_pr = CODEC.parse(JsonOps.INSTANCE, Streams.parse(reader));
                        assertThat("We were expecting a PersonRecord here", dr_pr.result().isPresent());

                        final PersonRecord pr = dr_pr.result().get();
                        assertThat("Person age should be 77", pr.age(), is(77));
                }
        }

        @Test
        public void CanSerializeAPersonRecord() {
                final PersonRecord pr = new PersonRecord("name person", 66);
                final var enc_pr = CODEC.encodeStart(JsonOps.INSTANCE, pr);
                assertThat("Should be possible to encode a person record", enc_pr.result().isPresent());
        }
}
