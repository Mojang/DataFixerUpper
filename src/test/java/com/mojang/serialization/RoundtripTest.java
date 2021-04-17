package com.mojang.serialization;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class RoundtripTest {
    // Constructors, equals and hashcode are auto-generated
    // TODO: switch to records in java 14+

    private enum Day {
        TUESDAY("tuesday", TuesdayData.CODEC),
        WEDNESDAY("wednesday", WednesdayData.CODEC),
        SUNDAY("sunday", SundayData.CODEC),
        ;

        private static final Map<String, Day> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(v -> v.name, Function.identity()));
        public static final Codec<Day> CODEC = Codec.STRING.comapFlatMap(DataResult.partialGet(BY_NAME::get, () -> "unknown day"), d -> d.name);

        private final String name;
        private final Codec<? extends DayData> codec;

        Day(final String name, final Codec<? extends DayData> codec) {
            this.name = name;
            this.codec = codec;
        }

        public Codec<? extends DayData> codec() {
            return codec;
        }
    }

    interface DayData {
        Codec<DayData> CODEC = Day.CODEC.dispatch(DayData::type, Day::codec);
        Day type();
    }

    private static final class TuesdayData implements DayData {
        public static final Codec<TuesdayData> CODEC = Codec.INT.xmap(TuesdayData::new, d -> d.x);

        private final int x;

        private TuesdayData(final int x) {
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
            final TuesdayData that = (TuesdayData) o;
            return x == that.x;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x);
        }

        @Override
        public Day type() {
            return Day.TUESDAY;
        }
    }

    private static final class WednesdayData implements DayData {
        public static final Codec<WednesdayData> CODEC = Codec.STRING.xmap(WednesdayData::new, d -> d.y);

        private final String y;

        private WednesdayData(final String y) {
            this.y = y;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final WednesdayData that = (WednesdayData) o;
            return Objects.equals(y, that.y);
        }

        @Override
        public int hashCode() {
            return Objects.hash(y);
        }

        @Override
        public Day type() {
            return Day.WEDNESDAY;
        }
    }

    private static final class SundayData implements DayData {
        public static final Codec<SundayData> CODEC = Codec.FLOAT.xmap(SundayData::new, d -> d.z);

        private final float z;

        private SundayData(final float z) {
            this.z = z;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final SundayData that = (SundayData) o;
            return Float.compare(that.z, z) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(z);
        }

        @Override
        public Day type() {
            return Day.SUNDAY;
        }
    }

    private static final class TestData {
        public static final Codec<TestData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.fieldOf("a").forGetter(d -> d.a),
            Codec.DOUBLE.fieldOf("b").forGetter(d -> d.b),
            Codec.BYTE.fieldOf("c").forGetter(d -> d.c),
            Codec.SHORT.fieldOf("d").forGetter(d -> d.d),
            Codec.INT.fieldOf("e").forGetter(d -> d.e),
            Codec.LONG.fieldOf("f").forGetter(d -> d.f),
            Codec.BOOL.fieldOf("g").forGetter(d -> d.g),
            Codec.STRING.fieldOf("h").forGetter(d -> d.h),
            Codec.STRING.listOf().fieldOf("i").forGetter(d -> d.i),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("j").forGetter(d -> d.j),
            Codec.compoundList(Codec.STRING, Codec.STRING).fieldOf("k").forGetter(d -> d.k),
            DayData.CODEC.fieldOf("day_data").forGetter(d -> d.dayData)
        ).apply(i, TestData::new));

        private final float a;
        private final double b;
        private final byte c;
        private final short d;
        private final int e;
        private final long f;
        private final boolean g;
        private final String h;
        private final List<String> i;
        private final Map<String, String> j;
        private final List<Pair<String, String>> k;

        private final DayData dayData;

        private TestData(final float a, final double b, final byte c, final short d, final int e, final long f, final boolean g, final String h, final List<String> i, final Map<String, String> j, final List<Pair<String, String>> k, final DayData dayData) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.e = e;
            this.f = f;
            this.g = g;
            this.h = h;
            this.i = i;
            this.j = j;
            this.k = k;
            this.dayData = dayData;
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
            return Float.compare(testData.a, a) == 0 &&
                Double.compare(testData.b, b) == 0 &&
                c == testData.c &&
                d == testData.d &&
                e == testData.e &&
                f == testData.f &&
                g == testData.g &&
                h.equals(testData.h) &&
                i.equals(testData.i) &&
                j.equals(testData.j) &&
                k.equals(testData.k) &&
                dayData.equals(testData.dayData);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b, c, d, e, f, g, h, i, j, k, dayData);
        }
    }

    private static TestData makeRandomTestData() {
        final Random random = new Random(4);
        return new TestData(
            random.nextFloat(),
            random.nextDouble(),
            (byte) random.nextInt(),
            (short) random.nextInt(),
            random.nextInt(),
            random.nextLong(),
            random.nextBoolean(),
            Float.toString(random.nextFloat()),
            IntStream.range(0, random.nextInt(100))
                .mapToObj(i -> Float.toString(random.nextFloat()))
                .collect(Collectors.toList()),
            IntStream.range(0, random.nextInt(100))
                .boxed()
                .collect(Collectors.toMap(
                    i -> Float.toString(random.nextFloat()),
                    i -> Float.toString(random.nextFloat()))
                ),
            IntStream.range(0, random.nextInt(100))
                .mapToObj(i -> Pair.of(Float.toString(random.nextFloat()), Float.toString(random.nextFloat())))
                .collect(Collectors.toList()
                ),
            new WednesdayData("meetings lol"));
    }

    private <T> void testWriteRead(final DynamicOps<T> ops) {
        final TestData data = makeRandomTestData();

        final DataResult<T> encoded = TestData.CODEC.encodeStart(ops, data);
        final DataResult<TestData> decoded = encoded.flatMap(r -> TestData.CODEC.parse(ops, r));

        assertEquals("read(write(x)) == x", DataResult.success(data), decoded);
    }

    private <T> void testReadWrite(final DynamicOps<T> ops) {
        final TestData data = makeRandomTestData();

        final DataResult<T> encoded = TestData.CODEC.encodeStart(ops, data);
        final DataResult<TestData> decoded = encoded.flatMap(r -> TestData.CODEC.parse(ops, r));
        final DataResult<T> reEncoded = decoded.flatMap(r -> TestData.CODEC.encodeStart(ops, r));

        assertEquals("write(read(x)) == x", encoded, reEncoded);
    }

    @Test
    public void testWriteReadNormal() {
        testWriteRead(JsonOps.INSTANCE);
    }

    @Test
    public void testReadWriteNormal() {
        testReadWrite(JsonOps.INSTANCE);
    }

    @Test
    public void testWriteReadCompressed() {
        testWriteRead(JsonOps.COMPRESSED);
    }

    @Test
    public void testReadWriteCompressed() {
        testReadWrite(JsonOps.COMPRESSED);
    }
}