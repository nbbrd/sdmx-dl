package sdmxdl.format.time;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sdmxdl.format.time.TimeRange.DateRange;
import sdmxdl.format.time.TimeRange.DateTimeRange;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.format.time.TimeFormatsTest.*;

@SuppressWarnings("DataFlowIssue")
public class TimeRangeTest {

    @Nested
    class DateRangeTest {

        @Test
        public void testOf() {
            assertThatNullPointerException()
                    .isThrownBy(() -> DateRange.of(null, P2D));

            assertThatNullPointerException()
                    .isThrownBy(() -> DateRange.of(START_DATE, null));

            assertThat(DateRange.of(START_DATE, P2D))
                    .isEqualTo(DateRange.of(START_DATE, P2D))
                    .extracting(DateRange::getStart, DateRange::getDuration)
                    .contains(START_DATE, P2D);

            assertThat(DateRange.of(START_DATE, P2M))
                    .isEqualTo(DateRange.of(START_DATE, P2M))
                    .extracting(DateRange::getStart, DateRange::getDuration)
                    .contains(START_DATE, P2M);
        }

        @Test
        public void testParse() {
            assertThatNullPointerException()
                    .isThrownBy(() -> DateRange.parse(null));

            assertThat(DateRange.parse("2010-02-15/P2D"))
                    .isEqualTo(DateRange.of(START_DATE, P2D));

            assertThat(generateInvalids("2010-02-15/P2D"))
                    .describedAs("Invalid format")
                    .are(throwingDateTimeParseExceptionOn(DateRange::parse));

            assertThat(asList("2010-02-29/P2D"))
                    .describedAs("Out-of-bounds")
                    .are(throwingDateTimeParseExceptionOn(DateTimeRange::parse));

            assertThat(DateRange.parse("2010-02-15/P2M"))
                    .isEqualTo(DateRange.of(START_DATE, P2M));

            assertThat(generateInvalids("2010-02-15/P2M"))
                    .describedAs("Invalid format")
                    .are(throwingDateTimeParseExceptionOn(DateRange::parse));

            assertThat(asList("2010-02-29/P2M"))
                    .describedAs("Out-of-bounds")
                    .are(throwingDateTimeParseExceptionOn(DateTimeRange::parse));

            assertThat(asList("2010-02-15T00:00/P2D", "20100215/P2D", "2010-046/P2D", "2010-W07-1/P2D"))
                    .describedAs("Invalid format")
                    .are(throwingDateTimeParseExceptionOn(DateRange::parse));
        }

        @Test
        public void testIsParsable() {
            assertThat(DateRange.isParsable(null)).isFalse();

            assertThat(DateRange.isParsable("2010-02-15/P2D")).isTrue();

            assertThat(generateInvalids("2010-02-15/P2D"))
                    .filteredOn(not(DateRange::isParsable))
                    .are(throwingDateTimeParseExceptionOn(DateRange::parse));

            assertThat(DateRange.isParsable("2010-02-15/P2M")).isTrue();

            assertThat(generateInvalids("2010-02-15/P2M"))
                    .filteredOn(not(DateRange::isParsable))
                    .are(throwingDateTimeParseExceptionOn(DateRange::parse));

            assertThat(asList("2010-02-15T00:00/P2D", "20100215/P2D", "2010-046/P2D", "2010-W07-1/P2D"))
                    .filteredOn(not(DateRange::isParsable))
                    .are(throwingDateTimeParseExceptionOn(DateRange::parse));
        }

        @Test
        public void testToString() {
            assertThat(DateRange.of(START_DATE, P2D))
                    .hasToString("2010-02-15/P2D");

            assertThat(DateRange.of(START_DATE, P2M))
                    .hasToString("2010-02-15/P2M");
        }

        @Test
        public void testToStartTime() {
            assertThat(DateRange.of(START_DATE, P2D).toStartTime(null))
                    .isEqualTo(LocalDateTime.of(2010, 2, 15, 0, 0));

            assertThat(DateRange.of(START_DATE, P2M).toStartTime(null))
                    .isEqualTo(LocalDateTime.of(2010, 2, 15, 0, 0));
        }
    }

    @Nested
    class DateTimeRangeTest {

        @Test
        public void testOf() {
            assertThatNullPointerException()
                    .isThrownBy(() -> DateTimeRange.of(null, P2D));

            assertThatNullPointerException()
                    .isThrownBy(() -> DateTimeRange.of(START_DATE_TIME, null));

            assertThat(DateTimeRange.of(START_DATE_TIME, P2D))
                    .isEqualTo(DateTimeRange.of(START_DATE_TIME, P2D))
                    .extracting(DateTimeRange::getStart, DateTimeRange::getDuration)
                    .contains(START_DATE_TIME, P2D);

            assertThat(DateTimeRange.of(START_DATE_TIME, P2M))
                    .isEqualTo(DateTimeRange.of(START_DATE_TIME, P2M))
                    .extracting(DateTimeRange::getStart, DateTimeRange::getDuration)
                    .contains(START_DATE_TIME, P2M);
        }

        @Test
        public void testParse() {
            assertThatNullPointerException()
                    .isThrownBy(() -> DateTimeRange.parse(null));

            assertThat(DateTimeRange.parse("2010-02-15T00:00/P2D"))
                    .isEqualTo(DateTimeRange.of(START_DATE_TIME, P2D));

            assertThat(generateInvalids("2010-02-15T00:00/P2D"))
                    .describedAs("Invalid format")
                    .are(throwingDateTimeParseExceptionOn(DateTimeRange::parse));

            assertThat(asList("2010-02-29T00:00/P2D"))
                    .describedAs("Out-of-bounds")
                    .are(throwingDateTimeParseExceptionOn(DateTimeRange::parse));

            assertThat(DateTimeRange.parse("2010-02-15T00:00/P2M"))
                    .isEqualTo(DateTimeRange.of(START_DATE_TIME, P2M));

            assertThat(generateInvalids("2010-02-15T00:00/P2M"))
                    .describedAs("Invalid format")
                    .are(throwingDateTimeParseExceptionOn(DateTimeRange::parse));

            assertThat(asList("2010-02-29T00:00/P2M"))
                    .describedAs("Out-of-bounds")
                    .are(throwingDateTimeParseExceptionOn(DateTimeRange::parse));

            assertThat(asList("2010-02-15/P2D", "20100215/P2D", "2010-046/P2D", "2010-W07-1/P2D"))
                    .describedAs("Invalid format")
                    .are(throwingDateTimeParseExceptionOn(DateTimeRange::parse));
        }

        @Test
        public void testIsParsable() {
            assertThat(DateTimeRange.isParsable(null)).isFalse();

            assertThat(DateTimeRange.isParsable("2010-02-15T00:00/P2D")).isTrue();

            assertThat(generateInvalids("2010-02-15T00:00/P2D"))
                    .filteredOn(not(DateTimeRange::isParsable))
                    .are(throwingDateTimeParseExceptionOn(DateTimeRange::parse));

            assertThat(DateTimeRange.isParsable("2010-02-15T00:00/P2M")).isTrue();

            assertThat(generateInvalids("2010-02-15T00:00/P2M"))
                    .filteredOn(not(DateTimeRange::isParsable))
                    .are(throwingDateTimeParseExceptionOn(DateTimeRange::parse));

            assertThat(asList("2010-02-15/P2D", "20100215/P2D", "2010-046/P2D", "2010-W07-1/P2D"))
                    .filteredOn(not(DateTimeRange::isParsable))
                    .are(throwingDateTimeParseExceptionOn(DateTimeRange::parse));
        }

        @Test
        public void testToString() {
            assertThat(DateTimeRange.of(START_DATE_TIME, P2D))
                    .hasToString("2010-02-15T00:00:00/P2D");

            assertThat(DateTimeRange.of(START_DATE_TIME, P2M))
                    .hasToString("2010-02-15T00:00:00/P2M");
        }

        @Test
        public void testToStartTime() {
            assertThat(DateTimeRange.of(START_DATE_TIME, P2D).toStartTime(null))
                    .isEqualTo(LocalDateTime.of(2010, 2, 15, 0, 0));

            assertThat(DateTimeRange.of(START_DATE_TIME, P2M).toStartTime(null))
                    .isEqualTo(LocalDateTime.of(2010, 2, 15, 0, 0));
        }
    }

    private static final Period P2D = Period.ofDays(2);
    private static final Period P2M = Period.ofMonths(2);
    private static final LocalDate START_DATE = LocalDate.of(2010, 2, 15);
    private static final LocalDateTime START_DATE_TIME = LocalDate.of(2010, 2, 15).atStartOfDay();
}
