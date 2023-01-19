package sdmxdl.format;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sdmxdl.format.time.StandardReportingFormat;
import sdmxdl.format.time.TimeRange;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.format.ObsTimeParser.*;

public class ObsTimeParserTest {

    private final LocalDateTime dateTime = LocalDateTime.parse("2001-01-01T00:00:00");
    private final MonthDay reportingYearStartDay = MonthDay.of(1, 2);

    @Test
    public void testOnStandardReporting() {
        assertThatNullPointerException().isThrownBy(() -> onStandardReportingFormat(null, IGNORE_ERROR));
        assertThatNullPointerException().isThrownBy(() -> onStandardReportingFormat(StandardReportingFormat.REPORTING_DAY, null));

        assertThat(onStandardReportingFormat(StandardReportingFormat.REPORTING_DAY, IGNORE_ERROR)).satisfies(x -> {
            assertThat(x.parseStartTime(null, null)).isNull();
            assertThat(x.parseStartTime("abc", null)).isNull();
            assertThat(x.parseStartTime("2001-D001", null)).isEqualTo(dateTime);
            assertThat(x.parseStartTime("2001-D001", reportingYearStartDay)).isEqualTo(dateTime.plusDays(1));
        });
    }

    @Test
    public void testOnObservationalTimePeriod() {
        assertThatNullPointerException().isThrownBy(() -> onObservationalTimePeriodParser(IGNORE_FILTER, null, IGNORE_ERROR));
        assertThatNullPointerException().isThrownBy(() -> onObservationalTimePeriodParser(IGNORE_FILTER, o -> null, null));

        assertThat(onObservationalTimePeriodParser(IGNORE_FILTER, o -> null, IGNORE_ERROR)).satisfies(x -> {
            assertThat(x.parseStartTime(null, null)).isNull();
            assertThat(x.parseStartTime("abc", null)).isNull();
            assertThat(x.parseStartTime("2001-01-01T00:00:00/P1D", null)).isNull();
            assertThat(x.parseStartTime("2001-01-01T00:00:00/P1D", reportingYearStartDay)).isNull();
        });

        assertThat(onObservationalTimePeriodParser(IGNORE_FILTER, o -> TimeRange.DateTimeRange.of(dateTime, Period.parse("P1D")), IGNORE_ERROR)).satisfies(x -> {
            assertThat(x.parseStartTime(null, null)).isEqualTo(dateTime);
            assertThat(x.parseStartTime("abc", null)).isEqualTo(dateTime);
            assertThat(x.parseStartTime("2001-01-01T00:00:00/P1D", null)).isEqualTo(dateTime);
            assertThat(x.parseStartTime("2001-01-01T00:00:00/P1D", reportingYearStartDay)).isEqualTo(dateTime);
        });

        assertThat(onObservationalTimePeriodParser(IGNORE_FILTER, TimeRange.DateTimeRange::parse, IGNORE_ERROR)).satisfies(x -> {
            assertThat(x.parseStartTime(null, null)).isNull();
            assertThat(x.parseStartTime("abc", null)).isNull();
            assertThat(x.parseStartTime("2001-01-01T00:00:00/P1D", null)).isEqualTo(dateTime);
            assertThat(x.parseStartTime("2001-01-01T00:00:00/P1D", reportingYearStartDay)).isEqualTo(dateTime);
        });
    }

    final MonthDay ref = MonthDay.of(7, 1);

    @ParameterizedTest
    @ValueSource(strings = {
            "2001", "2001-02", "2001-02-03", "2001-02-03T04:05", "2001-02-03T04:05:06",
            "2000-A1", "2000-S2", "2000-T3", "2000-Q4", "2000-M12", "2000-W53", "2000-D366",
            "2001-02-03/P2D", "2001-02-03T04:05/P2D", "2001-02-03T04:05:06/P2D"
    })
    public void testValidInputNeverRaiseException(String text) {
        List<Throwable> list = new ArrayList<>();
        ObsTimeParser x = getObservationalTimePeriod(list::add);
        assertThat(x.parseStartTime(text, null)).isNotNull();
        assertThat(x.parseStartTime(text, ref)).isNotNull();
        assertThat(list).hasSize(0);
    }

    @Nested
    class StandardTimePeriodTest {

        @Nested
        class BasicTimePeriodTest {

            @Nested
            class GregorianTimePeriodTest {

                @Test
                public void testGregorianYear() {
                    assertThat(getObservationalTimePeriod().parseStartTime("2001", null))
                            .describedAs("Gregorian Year")
                            .isEqualTo("2001-01-01T00:00:00");

                    assertThat(generateInvalids("2001"))
                            .describedAs("Gregorian Year format")
                            .are(notValidWith(getObservationalTimePeriod()));
                }

                @Test
                public void testGregorianYearMonth() {
                    assertThat(getObservationalTimePeriod().parseStartTime("2001-02", null))
                            .describedAs("Gregorian Year Month")
                            .isEqualTo("2001-02-01T00:00:00");

                    assertThat(generateInvalids("2001-02"))
                            .describedAs("Gregorian Year Month format")
                            .are(notValidWith(getObservationalTimePeriod()));
                }

                @Test
                public void testGregorianDay() {
                    assertThat(getObservationalTimePeriod().parseStartTime("2001-02-03", null))
                            .describedAs("Gregorian Day")
                            .isEqualTo("2001-02-03T00:00:00");

                    assertThat(generateInvalids("2001-02-03"))
                            .describedAs("Gregorian Day format")
                            .are(notValidWith(getObservationalTimePeriod()));

                    assertThat("2019-02-29")
                            .describedAs("Gregorian Day strict")
                            .is(notValidWith(getObservationalTimePeriod()));
                }
            }

            @Test
            public void testDateTime() {
                assertThat(getObservationalTimePeriod().parseStartTime("2001-02-03T04:05:06", null))
                        .describedAs("Date Time")
                        .isEqualTo("2001-02-03T04:05:06");

                assertThat(generateInvalids("2001-02-03T04:05:06"))
                        .describedAs("Date Time format")
                        .are(notValidWith(getObservationalTimePeriod()));

                assertThat("2019-02-29T11:23:56")
                        .describedAs("Date Time strict")
                        .is(notValidWith(getObservationalTimePeriod()));
            }
        }

        @Nested
        class ReportingTimePeriodTest {

            @Test
            public void testReportingYear() {
                assertThat(getObservationalTimePeriod().parseStartTime("2000-A1", null))
                        .describedAs("Reporting Year")
                        .isEqualTo("2000-01-01T00:00:00");

                assertThat(generateInvalids("2000-A1"))
                        .describedAs("Reporting Year format")
                        .are(notValidWith(getObservationalTimePeriod()));

                assertThat(asList("2000-A0", "2000-A2"))
                        .describedAs("Reporting Year out-of-bounds")
                        .are(notValidWith(getObservationalTimePeriod()));
            }

            @Test
            public void testReportingSemester() {
                assertThat(getObservationalTimePeriod().parseStartTime("2000-S2", null))
                        .describedAs("Reporting Semester")
                        .isEqualTo("2000-07-01T00:00:00");

                assertThat(generateInvalids("2000-S2"))
                        .describedAs("Reporting Semester format")
                        .are(notValidWith(getObservationalTimePeriod()));

                assertThat(asList("2000-S0", "2000-S3"))
                        .describedAs("Reporting Semester out-of-bounds")
                        .are(notValidWith(getObservationalTimePeriod()));
            }

            @Test
            public void testReportingTrimester() {
                assertThat(getObservationalTimePeriod().parseStartTime("2000-T3", null))
                        .describedAs("Reporting Trimester")
                        .isEqualTo("2000-09-01T00:00:00");

                assertThat(generateInvalids("2000-T3"))
                        .describedAs("Reporting Trimester format")
                        .are(notValidWith(getObservationalTimePeriod()));

                assertThat(asList("2000-T0", "2000-T4"))
                        .describedAs("Reporting Trimester out-of-bounds")
                        .are(notValidWith(getObservationalTimePeriod()));
            }

            @Test
            public void testReportingQuarter() {
                assertThat(getObservationalTimePeriod().parseStartTime("2000-Q4", null))
                        .describedAs("Reporting Quarter")
                        .isEqualTo("2000-10-01T00:00:00");

                assertThat(generateInvalids("2000-Q4"))
                        .describedAs("Reporting Quarter format")
                        .are(notValidWith(getObservationalTimePeriod()));

                assertThat(asList("2000-Q0", "2000-Q5"))
                        .describedAs("Reporting Quarter out-of-bounds")
                        .are(notValidWith(getObservationalTimePeriod()));

                assertThat(getObservationalTimePeriod().parseStartTime("2010-Q2", ref))
                        .describedAs("Reporting Quarter with start day")
                        .isEqualTo("2010-10-01T00:00:00");
            }

            @Test
            public void testReportingMonth() {
                assertThat(getObservationalTimePeriod().parseStartTime("2000-M12", null))
                        .describedAs("Reporting Month")
                        .isEqualTo("2000-12-01T00:00:00");

                assertThat(generateInvalids("2000-M12"))
                        .describedAs("Reporting Month format")
                        .are(notValidWith(getObservationalTimePeriod()));

                assertThat(asList("2000-M00", "2000-M13"))
                        .describedAs("Reporting Month out-of-bounds")
                        .are(notValidWith(getObservationalTimePeriod()));

                assertThat(asList("2000-M1", "2000-M001"))
                        .describedAs("Reporting Month padding")
                        .are(notValidWith(getObservationalTimePeriod()));
            }

            @Test
            public void testReportingWeek() {
                assertThat(getObservationalTimePeriod().parseStartTime("2000-W53", null))
                        .describedAs("Reporting Week")
                        .isEqualTo("2001-01-01T00:00:00");

                assertThat(generateInvalids("2000-W53"))
                        .describedAs("Reporting Week format")
                        .are(notValidWith(getObservationalTimePeriod()));

                assertThat(asList("2000-W00", "2000-W54"))
                        .describedAs("Reporting Week out-of-bounds")
                        .are(notValidWith(getObservationalTimePeriod()));

                assertThat(asList("2000-W1", "2000-W001"))
                        .describedAs("Reporting Week padding")
                        .are(notValidWith(getObservationalTimePeriod()));

                assertThat(getObservationalTimePeriod().parseStartTime("2011-W36", ref))
                        .describedAs("Reporting Week with start day")
                        .isEqualTo("2012-03-05T00:00:00");
            }

            @Test
            public void testReportingDay() {
                assertThat(getObservationalTimePeriod().parseStartTime("2000-D366", null))
                        .describedAs("Reporting Day")
                        .isEqualTo("2000-12-31T00:00:00");

                assertThat(generateInvalids("2000-D366"))
                        .describedAs("Reporting Day format")
                        .are(notValidWith(getObservationalTimePeriod()));

                assertThat(asList("2000-D000", "2000-D367"))
                        .describedAs("Reporting Day out-of-bounds")
                        .are(notValidWith(getObservationalTimePeriod()));

                assertThat(asList("2000-D1", "2000-D0001"))
                        .describedAs("Reporting Day padding")
                        .are(notValidWith(getObservationalTimePeriod()));
            }
        }

        @Test
        public void testTimeRange() {
            assertThat(getObservationalTimePeriod().parseStartTime("2010-02-15/P2D", null))
                    .describedAs("Time Range")
                    .isEqualTo("2010-02-15T00:00:00");

            assertThat(getObservationalTimePeriod().parseStartTime("2010-02-15T00:00/P2D", null))
                    .describedAs("Time Range")
                    .isEqualTo("2010-02-15T00:00:00");
        }
    }

    private static Condition<String> notValidWith(ObsTimeParser parser) {
        return new Condition<>(text -> parser.parseStartTime(text, null) == null, "not valid");
    }

    public static Condition<String> parsableUsing(Predicate<? super String> consumer) {
        return new Condition<>(text -> consumer.test(text), "parsable");
    }

    public static Condition<String> parsingToNull(Function<? super String, ?> consumer) {
        return new Condition<>(text -> consumer.apply(text) == null, "not valid");
    }

    public static Condition<String> throwingDateTimeParseExceptionOn(Consumer<? super String> consumer) {
        return new Condition<>(text -> {
            try {
                consumer.accept(text);
                return false;
            } catch (RuntimeException ex) {
                return ex instanceof DateTimeParseException;
            }
        }, "not valid");
    }

    public static List<String> generateInvalids(String source) {
        char invalid = 'X';
        List<String> result = new ArrayList<>();
        for (int i = 0; i < source.length(); i++) {
            char[] chars = source.toCharArray();
            chars[i] = invalid;
            result.add(String.valueOf(chars));
        }
        result.add(invalid + source);
        result.add(source + invalid);
        result.add(source.substring(1));
        result.add(source.substring(0, source.length() - 1));
        return result;
    }
}
