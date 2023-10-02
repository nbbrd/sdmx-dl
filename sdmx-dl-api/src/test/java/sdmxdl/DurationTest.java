package sdmxdl;

import org.assertj.core.api.ThrowableTypeAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.*;
import static org.assertj.core.api.Assertions.*;
import static sdmxdl.Duration.from;
import static sdmxdl.Duration.*;

@SuppressWarnings("DataFlowIssue")
public class DurationTest {

    @Test
    public void testConstants() {
        assertThat(ZERO).isSameAs(parse("P0D"));
        assertThat(P1Y).isSameAs(parse("P1Y"));
        assertThat(P6M).isSameAs(parse("P6M"));
        assertThat(P4M).isSameAs(parse("P4M"));
        assertThat(P3M).isSameAs(parse("P3M"));
        assertThat(P1M).isSameAs(parse("P1M"));
        assertThat(P7D).isSameAs(parse("P7D"));
        assertThat(P1D).isSameAs(parse("P1D"));

        assertThat(_P1W_).isNotSameAs(parse("P1W"));
        assertThat(_P3Y6M4DT12H30M5S_).isNotSameAs(parse("P3Y6M4DT12H30M5S"));
    }

    @Test
    public void testParse() {
        assertThatNullPointerException()
                .isThrownBy(() -> parse(null));

        assertThatDateTimeParseException()
                .isThrownBy(() -> parse(""))
                .withMessageContaining("Cannot parse using duration pattern");

        assertThatDateTimeParseException()
                .isThrownBy(() -> parse("P"))
                .withMessageContaining("Cannot parse using duration pattern");

        assertThatDateTimeParseException()
                .isThrownBy(() -> parse("P1X"))
                .withMessageContaining("Cannot parse using duration pattern");

        assertThatDateTimeParseException()
                .isThrownBy(() -> parse("P M"))
                .withMessageContaining("Cannot parse using duration pattern");

        assertThatDateTimeParseException()
                .isThrownBy(() -> parse("   "))
                .withMessageContaining("Cannot parse using duration pattern");

        assertThatDateTimeParseException()
                .isThrownBy(() -> parse("P1MX"))
                .withMessageContaining("Cannot parse using duration pattern");

        assertThatDateTimeParseException()
                .isThrownBy(() -> parse(DECIMAL_FRACTION_JAVA_DURATION.toString()))
                .withMessageContaining("Cannot parse using duration pattern");

        assertThatDateTimeParseException()
                .isThrownBy(() -> parse(INFIX_NEGATIVE_JAVA_DURATION.toString()))
                .withMessageContaining("Cannot parse using duration pattern");

        assertThatDateTimeParseException()
                .isThrownBy(() -> parse(PREFIX_NEGATIVE_JAVA_DURATION.toString()))
                .withMessageContaining("Cannot parse using duration pattern");

        assertThatDateTimeParseException()
                .isThrownBy(() -> parse(INFIX_NEGATIVE_JAVA_PERIOD.toString()))
                .withMessageContaining("Cannot parse using duration pattern");

        assertThatDateTimeParseException()
                .isThrownBy(() -> parse(PREFIX_NEGATIVE_JAVA_PERIOD.toString()))
                .withMessageContaining("Cannot parse using duration pattern");

        assertThatDateTimeParseException()
                .isThrownBy(() -> parse("PT"));

        String[] fields = {"years", "months", "days", "hours", "minutes", "seconds", "weeks"};

        assertThat(parse("P0D"))
                .extracting(fields)
                .containsExactly(0, 0, 0, 0, 0, 0, 0);

        assertThat(parse("P1Y"))
                .extracting(fields)
                .containsExactly(1, 0, 0, 0, 0, 0, 0);

        assertThat(parse("P6M"))
                .extracting(fields)
                .containsExactly(0, 6, 0, 0, 0, 0, 0);

        assertThat(parse("P4M"))
                .extracting(fields)
                .containsExactly(0, 4, 0, 0, 0, 0, 0);

        assertThat(parse("P3M"))
                .extracting(fields)
                .containsExactly(0, 3, 0, 0, 0, 0, 0);

        assertThat(parse("P1M"))
                .extracting(fields)
                .containsExactly(0, 1, 0, 0, 0, 0, 0);

        assertThat(parse("P7D"))
                .extracting(fields)
                .containsExactly(0, 0, 7, 0, 0, 0, 0);

        assertThat(parse("P1D"))
                .extracting(fields)
                .containsExactly(0, 0, 1, 0, 0, 0, 0);

        assertThat(parse("P1W"))
                .extracting(fields)
                .containsExactly(0, 0, 0, 0, 0, 0, 1);

        assertThat(parse("P3Y6M4DT12H30M5S"))
                .extracting(fields)
                .containsExactly(3, 6, 4, 12, 30, 5, 0);

        assertThat(parse("P12W"))
                .extracting(fields)
                .containsExactly(0, 0, 0, 0, 0, 0, 12);

        assertThat(parse("P0Y")).isSameAs(ZERO);
        assertThat(parse("P0M")).isSameAs(ZERO);
        assertThat(parse("P0D")).isSameAs(ZERO);
        assertThat(parse("P0W")).isSameAs(ZERO);
        assertThat(parse("PT0H")).isSameAs(ZERO);
        assertThat(parse("PT0M")).isSameAs(ZERO);
        assertThat(parse("PT0S")).isSameAs(ZERO);
    }

    @ParameterizedTest
    @MethodSource("getConstants")
    public void testFromConstant(Duration duration) {
        assertThat(from(duration)).isSameAs(duration);
    }

    @Test
    public void testFromNonConstant() {
        assertThatNullPointerException().isThrownBy(() -> from(null));
        assertThat(from(_P1W_)).isSameAs(_P1W_);
        assertThat(from(_P3Y6M4DT12H30M5S_)).isSameAs(_P3Y6M4DT12H30M5S_);
    }

    @Test
    public void testFromJavaPeriod() {
        assertThat(from(java.time.Period.ZERO)).isSameAs(ZERO);
        assertThat(from(java.time.Period.parse(P1Y.toString()))).isSameAs(P1Y);
        assertThat(from(java.time.Period.parse(P1M.toString()))).isSameAs(P1M);
        assertThat(from(java.time.Period.parse(P1D.toString()))).isSameAs(P1D);

        assertThat(from(java.time.Period.parse(_P1W_.toString()))).isEqualTo(P7D);

        assertThatDateTimeException()
                .isThrownBy(() -> from(INFIX_NEGATIVE_JAVA_PERIOD))
                .withMessageContaining("Negative unit amounts are not supported: -1 Years");

        assertThatDateTimeException()
                .isThrownBy(() -> from(PREFIX_NEGATIVE_JAVA_PERIOD))
                .withMessageContaining("Negative unit amounts are not supported: -1 Years");
    }

    @Test
    public void testFromJavaDuration() {
        assertThat(from(java.time.Duration.ZERO)).isSameAs(ZERO);

        assertThat(from(java.time.Duration.parse("PT15M"))).isEqualTo(parse("PT900S"));
        assertThat(from(java.time.Duration.parse("PT10H"))).isEqualTo(parse("PT36000S"));
        assertThat(from(java.time.Duration.parse("P2D"))).isEqualTo(parse("PT172800S"));
        assertThat(from(java.time.Duration.parse("P2DT3H4M"))).isEqualTo(parse("PT183840S"));
        assertThat(from(java.time.Duration.parse("-PT-6H+3M"))).isEqualTo(parse("PT21420S"));

        assertThatDateTimeException()
                .isThrownBy(() -> from(INFIX_NEGATIVE_JAVA_DURATION));

        assertThatDateTimeException()
                .isThrownBy(() -> from(PREFIX_NEGATIVE_JAVA_DURATION));

        assertThatDateTimeException()
                .isThrownBy(() -> from(DECIMAL_FRACTION_JAVA_DURATION))
                .withMessageContaining("Unit must be Years, Months, Days, Hours, Minutes or Seconds but was Nanos");
    }

    @ParameterizedTest
    @ValueSource(strings = {"P1D", "P0D", "P1W", "P1Y", "P6M", "P4M", "P3M", "P1M", "P7D", "P3Y6M4DT12H30M5S"})
    public void testToString(String text) {
        assertThat(parse(text))
                .hasToString(text);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testGet() {
        assertThatNullPointerException()
                .isThrownBy(() -> P1D.get(null));

        assertThatExceptionOfType(UnsupportedTemporalTypeException.class)
                .isThrownBy(() -> P1D.get(DECADES));

        Function[] extractors = Stream.of(YEARS, MONTHS, DAYS, HOURS, MINUTES, SECONDS, WEEKS)
                .map(unit -> (Function<Duration, Integer>) duration -> (int) duration.get(unit))
                .toArray(Function[]::new);

        assertThat(ZERO)
                .extracting(extractors)
                .containsExactly(0, 0, 0, 0, 0, 0, 0);

        assertThat(_P1W_)
                .extracting(extractors)
                .containsExactly(0, 0, 0, 0, 0, 0, 1);

        assertThat(P1Y)
                .extracting(extractors)
                .containsExactly(1, 0, 0, 0, 0, 0, 0);

        assertThat(P6M)
                .extracting(extractors)
                .containsExactly(0, 6, 0, 0, 0, 0, 0);

        assertThat(P4M)
                .extracting(extractors)
                .containsExactly(0, 4, 0, 0, 0, 0, 0);

        assertThat(P3M)
                .extracting(extractors)
                .containsExactly(0, 3, 0, 0, 0, 0, 0);

        assertThat(P1M)
                .extracting(extractors)
                .containsExactly(0, 1, 0, 0, 0, 0, 0);

        assertThat(P7D)
                .extracting(extractors)
                .containsExactly(0, 0, 7, 0, 0, 0, 0);

        assertThat(P1D)
                .extracting(extractors)
                .containsExactly(0, 0, 1, 0, 0, 0, 0);

        assertThat(_P3Y6M4DT12H30M5S_)
                .extracting(extractors)
                .containsExactly(3, 6, 4, 12, 30, 5, 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"P1D", "P0D", "P1W", "P1Y", "P6M", "P4M", "P3M", "P1M", "P7D", "P3Y6M4DT12H30M5S"})
    public void testGetUnits(String text) {
        assertThat(parse(text).getUnits())
                .containsExactly(YEARS, MONTHS, DAYS, HOURS, MINUTES, SECONDS, WEEKS);
    }

    @Test
    public void testAddTo() {
        assertThat(ZERO.addTo(dateTime))
                .isEqualTo(dateTime);

        assertThat(_P1W_.addTo(dateTime))
                .isEqualTo(dateTime.plusWeeks(1));

        assertThat(P1Y.addTo(dateTime))
                .isEqualTo(dateTime.plusYears(1));

        assertThat(P6M.addTo(dateTime))
                .isEqualTo(dateTime.plusMonths(6));

        assertThat(P4M.addTo(dateTime))
                .isEqualTo(dateTime.plusMonths(4));

        assertThat(P3M.addTo(dateTime))
                .isEqualTo(dateTime.plusMonths(3));

        assertThat(P1M.addTo(dateTime))
                .isEqualTo(dateTime.plusMonths(1));

        assertThat(P7D.addTo(dateTime))
                .isEqualTo(dateTime.plusDays(7));

        assertThat(P1D.addTo(dateTime))
                .isEqualTo(dateTime.plusDays(1));

        assertThat(_P3Y6M4DT12H30M5S_.addTo(dateTime))
                .isEqualTo(dateTime
                        .plusYears(3)
                        .plusMonths(6)
                        .plusDays(4)
                        .plusHours(12)
                        .plusMinutes(30)
                        .plusSeconds(5)
                );
    }

    @Test
    public void testSubtractFrom() {
        assertThat(ZERO.subtractFrom(dateTime))
                .isEqualTo(dateTime);

        assertThat(_P1W_.subtractFrom(dateTime))
                .isEqualTo(dateTime.minusWeeks(1));

        assertThat(P1Y.subtractFrom(dateTime))
                .isEqualTo(dateTime.minusYears(1));

        assertThat(P6M.subtractFrom(dateTime))
                .isEqualTo(dateTime.minusMonths(6));

        assertThat(P4M.subtractFrom(dateTime))
                .isEqualTo(dateTime.minusMonths(4));

        assertThat(P3M.subtractFrom(dateTime))
                .isEqualTo(dateTime.minusMonths(3));

        assertThat(P1M.subtractFrom(dateTime))
                .isEqualTo(dateTime.minusMonths(1));

        assertThat(P7D.subtractFrom(dateTime))
                .isEqualTo(dateTime.minusDays(7));

        assertThat(P1D.subtractFrom(dateTime))
                .isEqualTo(dateTime.minusDays(1));

        assertThat(_P3Y6M4DT12H30M5S_.subtractFrom(dateTime))
                .isEqualTo(dateTime
                        .minusYears(3)
                        .minusMonths(6)
                        .minusDays(4)
                        .minusHours(12)
                        .minusMinutes(30)
                        .minusSeconds(5)
                );
    }

    @ParameterizedTest
    @MethodSource("getConstants")
    public void testConstantMultipliedBy(Duration x) {
        assertThat(x.multipliedBy(0)).isSameAs(ZERO);
        assertThat(x.multipliedBy(1)).isSameAs(x);
        if (x.equals(ZERO))
            assertThat(x.multipliedBy(2)).isSameAs(ZERO);
        else
            assertThat(x.multipliedBy(2)).isNotEqualTo(x);
    }

    @Test
    public void testMultipliedBy() {
        assertThat(_P1W_.multipliedBy(3)).isEqualTo(parse("P3W"));
        assertThat(_P3Y6M4DT12H30M5S_.multipliedBy(3)).isEqualTo(parse("P9Y18M12DT36H90M15S"));
        assertThat(P4M.multipliedBy(3)).isEqualTo(_P12M_);
    }

    private final LocalDateTime dateTime = LocalDateTime.of(2010, 1, 1, 0, 0);

    private static final Duration _P1W_ = parse("P1W");

    private static final Duration _P3Y6M4DT12H30M5S_ = parse("P3Y6M4DT12H30M5S");

    private static final Duration _P12M_ = parse("P12M");

    private static ThrowableTypeAssert<DateTimeParseException> assertThatDateTimeParseException() {
        return assertThatExceptionOfType(DateTimeParseException.class);
    }

    private static ThrowableTypeAssert<DateTimeException> assertThatDateTimeException() {
        return assertThatExceptionOfType(DateTimeException.class);
    }

    private static List<Duration> getConstants() {
        return Arrays.asList(ZERO, P1Y, P6M, P4M, P3M, P1M, P7D, P1D);
    }

    private static final java.time.Period INFIX_NEGATIVE_JAVA_PERIOD = java.time.Period.parse("P-1Y2M");
    private static final java.time.Period PREFIX_NEGATIVE_JAVA_PERIOD = java.time.Period.parse("-P1Y2M");
    private static final java.time.Duration DECIMAL_FRACTION_JAVA_DURATION = java.time.Duration.parse("PT20.345S");
    private static final java.time.Duration INFIX_NEGATIVE_JAVA_DURATION = java.time.Duration.parse("PT-6H3M");
    private static final java.time.Duration PREFIX_NEGATIVE_JAVA_DURATION = java.time.Duration.parse("-PT6H3M");
}
