package sdmxdl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeParseException;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.*;
import static org.assertj.core.api.Assertions.*;
import static sdmxdl.Duration.from;
import static sdmxdl.Duration.*;

@SuppressWarnings("DataFlowIssue")
public class DurationTest {

    @Test
    public void testZERO() {
        assertThat(ZERO)
                .hasToString(P0D)
                .isEqualTo(ZERO)
                .extracting("years", "months", "days", "hours", "minutes", "seconds", "weeks")
                .containsExactly(0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void testParse() {
        assertThatNullPointerException()
                .isThrownBy(() -> parse(null));

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> parse(""));

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> parse("P"));

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> parse("P1X"));

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> parse("P1MX"));

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> parse("PT"));

        String[] fields = {"years", "months", "days", "hours", "minutes", "seconds", "weeks"};

        assertThat(parse(P0D))
                .extracting(fields)
                .containsExactly(0, 0, 0, 0, 0, 0, 0);

        assertThat(parse(P1W))
                .extracting(fields)
                .containsExactly(0, 0, 0, 0, 0, 0, 1);

        assertThat(parse(P1Y))
                .extracting(fields)
                .containsExactly(1, 0, 0, 0, 0, 0, 0);

        assertThat(parse(P6M))
                .extracting(fields)
                .containsExactly(0, 6, 0, 0, 0, 0, 0);

        assertThat(parse(P4M))
                .extracting(fields)
                .containsExactly(0, 4, 0, 0, 0, 0, 0);

        assertThat(parse(P3M))
                .extracting(fields)
                .containsExactly(0, 3, 0, 0, 0, 0, 0);

        assertThat(parse(P1M))
                .extracting(fields)
                .containsExactly(0, 1, 0, 0, 0, 0, 0);

        assertThat(parse(P7D))
                .extracting(fields)
                .containsExactly(0, 0, 7, 0, 0, 0, 0);

        assertThat(parse(P1D))
                .extracting(fields)
                .containsExactly(0, 0, 1, 0, 0, 0, 0);

        assertThat(parse(P3Y6M4DT12H30M5S))
                .extracting(fields)
                .containsExactly(3, 6, 4, 12, 30, 5, 0);
    }

    @Test
    public void testFrom() {
        assertThatNullPointerException()
                .isThrownBy(() -> from(null));

        assertThat(from(parse(P1D)))
                .isEqualTo(parse(P1D));

        assertThatExceptionOfType(DateTimeException.class)
                .isThrownBy(() -> from(java.time.Duration.ZERO));

        assertThat(from(java.time.Period.parse("P1D")))
                .isEqualTo(parse(P1D));
    }

    @Test
    public void testToString() {
        assertThat(parse(P0D))
                .hasToString(P0D);

        assertThat(parse(P1W))
                .hasToString(P1W);

        assertThat(parse(P1Y))
                .hasToString(P1Y);

        assertThat(parse(P6M))
                .hasToString(P6M);

        assertThat(parse(P4M))
                .hasToString(P4M);

        assertThat(parse(P3M))
                .hasToString(P3M);

        assertThat(parse(P1M))
                .hasToString(P1M);

        assertThat(parse(P7D))
                .hasToString(P7D);

        assertThat(parse(P1D))
                .hasToString(P1D);

        assertThat(parse(P3Y6M4DT12H30M5S))
                .hasToString("P3Y6M4DT12H30M5S");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testGet() {
        assertThatNullPointerException()
                .isThrownBy(() -> parse(P1D).get(null));

        assertThatExceptionOfType(UnsupportedTemporalTypeException.class)
                .isThrownBy(() -> parse(P1D).get(DECADES));

        Function[] extractors = Stream.of(YEARS, MONTHS, DAYS, HOURS, MINUTES, SECONDS, WEEKS)
                .map(unit -> (Function<Duration, Integer>) duration -> (int) duration.get(unit))
                .toArray(Function[]::new);

        assertThat(parse(P0D))
                .extracting(extractors)
                .containsExactly(0, 0, 0, 0, 0, 0, 0);

        assertThat(parse(P1W))
                .extracting(extractors)
                .containsExactly(0, 0, 0, 0, 0, 0, 1);

        assertThat(parse(P1Y))
                .extracting(extractors)
                .containsExactly(1, 0, 0, 0, 0, 0, 0);

        assertThat(parse(P6M))
                .extracting(extractors)
                .containsExactly(0, 6, 0, 0, 0, 0, 0);

        assertThat(parse(P4M))
                .extracting(extractors)
                .containsExactly(0, 4, 0, 0, 0, 0, 0);

        assertThat(parse(P3M))
                .extracting(extractors)
                .containsExactly(0, 3, 0, 0, 0, 0, 0);

        assertThat(parse(P1M))
                .extracting(extractors)
                .containsExactly(0, 1, 0, 0, 0, 0, 0);

        assertThat(parse(P7D))
                .extracting(extractors)
                .containsExactly(0, 0, 7, 0, 0, 0, 0);

        assertThat(parse(P1D))
                .extracting(extractors)
                .containsExactly(0, 0, 1, 0, 0, 0, 0);

        assertThat(parse(P3Y6M4DT12H30M5S))
                .extracting(extractors)
                .containsExactly(3, 6, 4, 12, 30, 5, 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {P1D, P0D, P1W, P1Y, P6M, P4M, P3M, P1M, P7D, P3Y6M4DT12H30M5S})
    public void testGetUnits(String text) {
        assertThat(parse(text).getUnits())
                .containsExactly(YEARS, MONTHS, DAYS, HOURS, MINUTES, SECONDS, WEEKS);
    }

    @Test
    public void testAddTo() {
        assertThat(parse(P0D).addTo(dateTime))
                .isEqualTo(dateTime);

        assertThat(parse(P1W).addTo(dateTime))
                .isEqualTo(dateTime.plus(1, WEEKS));

        assertThat(parse(P1Y).addTo(dateTime))
                .isEqualTo(dateTime.plus(1, YEARS));

        assertThat(parse(P6M).addTo(dateTime))
                .isEqualTo(dateTime.plus(6, MONTHS));

        assertThat(parse(P4M).addTo(dateTime))
                .isEqualTo(dateTime.plus(4, MONTHS));

        assertThat(parse(P3M).addTo(dateTime))
                .isEqualTo(dateTime.plus(3, MONTHS));

        assertThat(parse(P1M).addTo(dateTime))
                .isEqualTo(dateTime.plus(1, MONTHS));

        assertThat(parse(P7D).addTo(dateTime))
                .isEqualTo(dateTime.plus(7, DAYS));

        assertThat(parse(P1D).addTo(dateTime))
                .isEqualTo(dateTime.plus(1, DAYS));

        assertThat(parse(P3Y6M4DT12H30M5S).addTo(dateTime))
                .isEqualTo(dateTime
                        .plus(3, YEARS)
                        .plus(6, MONTHS)
                        .plus(4, DAYS)
                        .plus(12, HOURS)
                        .plus(30, MINUTES)
                        .plus(5, SECONDS)
                );
    }

    @Test
    public void testSubtractFrom() {
        assertThat(parse(P0D).subtractFrom(dateTime))
                .isEqualTo(dateTime);

        assertThat(parse(P1W).subtractFrom(dateTime))
                .isEqualTo(dateTime.minus(1, WEEKS));

        assertThat(parse(P1Y).subtractFrom(dateTime))
                .isEqualTo(dateTime.minus(1, YEARS));

        assertThat(parse(P6M).subtractFrom(dateTime))
                .isEqualTo(dateTime.minus(6, MONTHS));

        assertThat(parse(P4M).subtractFrom(dateTime))
                .isEqualTo(dateTime.minus(4, MONTHS));

        assertThat(parse(P3M).subtractFrom(dateTime))
                .isEqualTo(dateTime.minus(3, MONTHS));

        assertThat(parse(P1M).subtractFrom(dateTime))
                .isEqualTo(dateTime.minus(1, MONTHS));

        assertThat(parse(P7D).subtractFrom(dateTime))
                .isEqualTo(dateTime.minus(7, DAYS));

        assertThat(parse(P1D).subtractFrom(dateTime))
                .isEqualTo(dateTime.minus(1, DAYS));

        assertThat(parse(P3Y6M4DT12H30M5S).subtractFrom(dateTime))
                .isEqualTo(dateTime
                        .minus(3, YEARS)
                        .minus(6, MONTHS)
                        .minus(4, DAYS)
                        .minus(12, HOURS)
                        .minus(30, MINUTES)
                        .minus(5, SECONDS)
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {P1D, P0D, P1W, P1Y, P6M, P4M, P3M, P1M, P7D, P3Y6M4DT12H30M5S})
    public void testGetChronology(String text) {
        assertThat(parse(text).getChronology())
                .isEqualTo(IsoChronology.INSTANCE);
    }

    @Test
    public void testPlus() {
        assertThatNullPointerException()
                .isThrownBy(() -> ZERO.plus(null));

        assertThat(ZERO.plus(ZERO))
                .isEqualTo(ZERO);

        assertThat(parse(P1W).plus(parse(P1W)))
                .isEqualTo(parse("P2W"));

        assertThat(parse(P1M).plus(ZERO))
                .isEqualTo(parse(P1M));

        assertThat(parse(P1M).plus(parse(P3M)))
                .isEqualTo(parse(P4M));
    }

    @Test
    public void testMinus() {
        assertThatNullPointerException()
                .isThrownBy(() -> ZERO.minus(null));

        assertThat(ZERO.minus(ZERO))
                .isEqualTo(ZERO);

        assertThat(parse(P1W).minus(parse(P1W)))
                .isEqualTo(ZERO);

        assertThat(parse(P1M).minus(ZERO))
                .isEqualTo(parse(P1M));

        assertThat(parse(P4M).minus(parse(P3M)))
                .isEqualTo(parse(P1M));
    }

    @Test
    public void testMultipliedBy() {
        assertThat(parse(P1M).multipliedBy(0))
                .isEqualTo(ZERO);

        assertThat(parse(P1M).multipliedBy(1))
                .isEqualTo(parse(P1M));

        assertThat(parse(P1M).multipliedBy(3))
                .isEqualTo(parse(P3M));
    }

    @Test
    public void testNormalized() {
        assertThat(ZERO.normalized())
                .isEqualTo(ZERO);

        assertThat(parse(P3M).normalized())
                .isEqualTo(parse(P3M));

        assertThat(parse(P1M).multipliedBy(12).normalized())
                .isEqualTo(parse(P1Y));
    }

    private final LocalDateTime dateTime = LocalDateTime.of(2010, 1, 1, 0, 0);

    private static final String P1D = "P1D";
    private static final String P0D = "P0D";
    private static final String P1W = "P1W";
    private static final String P1Y = "P1Y";
    private static final String P6M = "P6M";
    private static final String P4M = "P4M";
    private static final String P3M = "P3M";
    private static final String P1M = "P1M";
    private static final String P7D = "P7D";
    private static final String P3Y6M4DT12H30M5S = "P3Y6M4DT12H30M5S";
}
