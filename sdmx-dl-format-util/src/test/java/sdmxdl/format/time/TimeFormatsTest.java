package sdmxdl.format.time;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.format.time.TimeFormats.OBSERVATIONAL_TIME_PERIOD;

public class TimeFormatsTest {

    final MonthDay ref = MonthDay.of(7, 1);

    @Test
    public void testOnObservationalTimePeriod() {
        assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime(null, ref)).isNull();
        assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("null", null)).isNull();
    }

    @Nested
    class StandardTimePeriodTest {

        @Nested
        class BasicTimePeriodTest {

            @Nested
            class GregorianTimePeriodTest {

                @Test
                public void testGregorianYear() {
                    assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("2001", null))
                            .describedAs("Gregorian Year")
                            .isEqualTo("2001-01-01T00:00:00");

                    assertThat(generateInvalids("2001"))
                            .describedAs("Gregorian Year format")
                            .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));
                }

                @Test
                public void testGregorianYearMonth() {
                    assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("2001-02", null))
                            .describedAs("Gregorian Year Month")
                            .isEqualTo("2001-02-01T00:00:00");

                    assertThat(generateInvalids("2001-02"))
                            .describedAs("Gregorian Year Month format")
                            .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));
                }

                @Test
                public void testGregorianDay() {
                    assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("2001-02-03", null))
                            .describedAs("Gregorian Day")
                            .isEqualTo("2001-02-03T00:00:00");

                    assertThat(generateInvalids("2001-02-03"))
                            .describedAs("Gregorian Day format")
                            .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));

                    assertThat("2019-02-29")
                            .describedAs("Gregorian Day strict")
                            .is(notValidWith(OBSERVATIONAL_TIME_PERIOD));
                }
            }

            @Test
            public void testDateTime() {
                assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("2001-02-03T04:05:06", null))
                        .describedAs("Date Time")
                        .isEqualTo("2001-02-03T04:05:06");

                assertThat(generateInvalids("2001-02-03T04:05:06"))
                        .describedAs("Date Time format")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));

                assertThat("2019-02-29T11:23:56")
                        .describedAs("Date Time strict")
                        .is(notValidWith(OBSERVATIONAL_TIME_PERIOD));
            }
        }

        @Nested
        class ReportingTimePeriodTest {

            @Test
            public void testReportingYear() {
                assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("2000-A1", null))
                        .describedAs("Reporting Year")
                        .isEqualTo("2000-01-01T00:00:00");

                assertThat(generateInvalids("2000-A1"))
                        .describedAs("Reporting Year format")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));

                assertThat(asList("2000-A0", "2000-A2"))
                        .describedAs("Reporting Year out-of-bounds")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));
            }

            @Test
            public void testReportingSemester() {
                assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("2000-S2", null))
                        .describedAs("Reporting Semester")
                        .isEqualTo("2000-07-01T00:00:00");

                assertThat(generateInvalids("2000-S2"))
                        .describedAs("Reporting Semester format")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));

                assertThat(asList("2000-S0", "2000-S3"))
                        .describedAs("Reporting Semester out-of-bounds")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));
            }

            @Test
            public void testReportingTrimester() {
                assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("2000-T3", null))
                        .describedAs("Reporting Trimester")
                        .isEqualTo("2000-09-01T00:00:00");

                assertThat(generateInvalids("2000-T3"))
                        .describedAs("Reporting Trimester format")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));

                assertThat(asList("2000-T0", "2000-T4"))
                        .describedAs("Reporting Trimester out-of-bounds")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));
            }

            @Test
            public void testReportingQuarter() {
                assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("2000-Q4", null))
                        .describedAs("Reporting Quarter")
                        .isEqualTo("2000-10-01T00:00:00");

                assertThat(generateInvalids("2000-Q4"))
                        .describedAs("Reporting Quarter format")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));

                assertThat(asList("2000-Q0", "2000-Q5"))
                        .describedAs("Reporting Quarter out-of-bounds")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));

                assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("2010-Q2", ref))
                        .describedAs("Reporting Quarter with start day")
                        .isEqualTo("2010-10-01T00:00:00");
            }

            @Test
            public void testReportingMonth() {
                assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("2000-M12", null))
                        .describedAs("Reporting Month")
                        .isEqualTo("2000-12-01T00:00:00");

                assertThat(generateInvalids("2000-M12"))
                        .describedAs("Reporting Month format")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));

                assertThat(asList("2000-M00", "2000-M13"))
                        .describedAs("Reporting Month out-of-bounds")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));

                assertThat(asList("2000-M1", "2000-M001"))
                        .describedAs("Reporting Month padding")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));
            }

            @Test
            public void testReportingWeek() {
                assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("2000-W53", null))
                        .describedAs("Reporting Week")
                        .isEqualTo("2001-01-01T00:00:00");

                assertThat(generateInvalids("2000-W53"))
                        .describedAs("Reporting Week format")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));

                assertThat(asList("2000-W00", "2000-W54"))
                        .describedAs("Reporting Week out-of-bounds")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));

                assertThat(asList("2000-W1", "2000-W001"))
                        .describedAs("Reporting Week padding")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));

                assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("2011-W36", ref))
                        .describedAs("Reporting Week with start day")
                        .isEqualTo("2012-03-05T00:00:00");
            }

            @Test
            public void testReportingDay() {
                assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("2000-D366", null))
                        .describedAs("Reporting Day")
                        .isEqualTo("2000-12-31T00:00:00");

                assertThat(generateInvalids("2000-D366"))
                        .describedAs("Reporting Day format")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));

                assertThat(asList("2000-D000", "2000-D367"))
                        .describedAs("Reporting Day out-of-bounds")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));

                assertThat(asList("2000-D1", "2000-D0001"))
                        .describedAs("Reporting Day padding")
                        .are(notValidWith(OBSERVATIONAL_TIME_PERIOD));
            }
        }

        @Test
        public void testTimeRange() {
            assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("2010-02-15/P2D", null))
                    .describedAs("Time Range")
                    .isEqualTo("2010-02-15T00:00:00");

            assertThat(OBSERVATIONAL_TIME_PERIOD.parseStartTime("2010-02-15T00:00/P2D", null))
                    .describedAs("Time Range")
                    .isEqualTo("2010-02-15T00:00:00");
        }
    }

    private static Condition<String> notValidWith(ObsTimeParser parser) {
        return new Condition<>(text -> parser.parseStartTime(text, null) == null, "not valid");
    }

    private static List<String> generateInvalids(String source) {
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
