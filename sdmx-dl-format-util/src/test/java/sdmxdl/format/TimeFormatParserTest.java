package sdmxdl.format;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import sdmxdl.format.TimeFormatParser;

import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.format.TimeFormatParser.onObservationalTimePeriod;

public class TimeFormatParserTest {

    final MonthDay ref = MonthDay.of(7, 1);

    @Test
    public void testOnObservationalTimePeriod() {
        assertThat(onObservationalTimePeriod().parse(null, ref)).isNull();
        assertThat(onObservationalTimePeriod().parse("null", null)).isNull();
    }

    @Test
    public void testGregorianYear() {
        assertThat(onObservationalTimePeriod().parse("2001", null))
                .describedAs("Gregorian Year")
                .isEqualTo("2001-01-01T00:00:00");

        assertThat(generateInvalids("2001"))
                .describedAs("Gregorian Year format")
                .are(notValidWith(onObservationalTimePeriod()));
    }

    @Test
    public void testGregorianYearMonth() {
        assertThat(onObservationalTimePeriod().parse("2001-02", null))
                .describedAs("Gregorian Year Month")
                .isEqualTo("2001-02-01T00:00:00");

        assertThat(generateInvalids("2001-02"))
                .describedAs("Gregorian Year Month format")
                .are(notValidWith(onObservationalTimePeriod()));
    }

    @Test
    public void testGregorianDay() {
        assertThat(onObservationalTimePeriod().parse("2001-02-03", null))
                .describedAs("Gregorian Day")
                .isEqualTo("2001-02-03T00:00:00");

        assertThat(generateInvalids("2001-02-03"))
                .describedAs("Gregorian Day format")
                .are(notValidWith(onObservationalTimePeriod()));

        assertThat("2019-02-29")
                .describedAs("Gregorian Day strict")
                .is(notValidWith(onObservationalTimePeriod()));
    }

    @Test
    public void testDateTime() {
        assertThat(onObservationalTimePeriod().parse("2001-02-03T04:05:06", null))
                .describedAs("Date Time")
                .isEqualTo("2001-02-03T04:05:06");

        assertThat(generateInvalids("2001-02-03T04:05:06"))
                .describedAs("Date Time format")
                .are(notValidWith(onObservationalTimePeriod()));

        assertThat("2019-02-29T11:23:56")
                .describedAs("Date Time strict")
                .is(notValidWith(onObservationalTimePeriod()));
    }

    @Test
    public void testReportingYear() {
        assertThat(onObservationalTimePeriod().parse("2000-A1", null))
                .describedAs("Reporting Year")
                .isEqualTo("2000-01-01T00:00:00");

        assertThat(generateInvalids("2000-A1"))
                .describedAs("Reporting Year format")
                .are(notValidWith(onObservationalTimePeriod()));

        assertThat(asList("2000-A0", "2000-A2"))
                .describedAs("Reporting Year out-of-bounds")
                .are(notValidWith(onObservationalTimePeriod()));
    }

    @Test
    public void testReportingSemester() {
        assertThat(onObservationalTimePeriod().parse("2000-S2", null))
                .describedAs("Reporting Semester")
                .isEqualTo("2000-07-01T00:00:00");

        assertThat(generateInvalids("2000-S2"))
                .describedAs("Reporting Semester format")
                .are(notValidWith(onObservationalTimePeriod()));

        assertThat(asList("2000-S0", "2000-S3"))
                .describedAs("Reporting Semester out-of-bounds")
                .are(notValidWith(onObservationalTimePeriod()));
    }

    @Test
    public void testReportingTrimester() {
        assertThat(onObservationalTimePeriod().parse("2000-T3", null))
                .describedAs("Reporting Trimester")
                .isEqualTo("2000-09-01T00:00:00");

        assertThat(generateInvalids("2000-T3"))
                .describedAs("Reporting Trimester format")
                .are(notValidWith(onObservationalTimePeriod()));

        assertThat(asList("2000-T0", "2000-T4"))
                .describedAs("Reporting Trimester out-of-bounds")
                .are(notValidWith(onObservationalTimePeriod()));
    }

    @Test
    public void testReportingQuarter() {
        assertThat(onObservationalTimePeriod().parse("2000-Q4", null))
                .describedAs("Reporting Quarter")
                .isEqualTo("2000-10-01T00:00:00");

        assertThat(generateInvalids("2000-Q4"))
                .describedAs("Reporting Quarter format")
                .are(notValidWith(onObservationalTimePeriod()));

        assertThat(asList("2000-Q0", "2000-Q5"))
                .describedAs("Reporting Quarter out-of-bounds")
                .are(notValidWith(onObservationalTimePeriod()));

        assertThat(onObservationalTimePeriod().parse("2010-Q2", ref))
                .describedAs("Reporting Quarter with start day")
                .isEqualTo("2010-10-01T00:00:00");
    }

    @Test
    public void testReportingMonth() {
        assertThat(onObservationalTimePeriod().parse("2000-M12", null))
                .describedAs("Reporting Month")
                .isEqualTo("2000-12-01T00:00:00");

        assertThat(generateInvalids("2000-M12"))
                .describedAs("Reporting Month format")
                .are(notValidWith(onObservationalTimePeriod()));

        assertThat(asList("2000-M00", "2000-M13"))
                .describedAs("Reporting Month out-of-bounds")
                .are(notValidWith(onObservationalTimePeriod()));

        assertThat(asList("2000-M1", "2000-M001"))
                .describedAs("Reporting Month padding")
                .are(notValidWith(onObservationalTimePeriod()));
    }

    @Test
    public void testReportingWeek() {
        assertThat(onObservationalTimePeriod().parse("2000-W53", null))
                .describedAs("Reporting Week")
                .isEqualTo("2001-01-01T00:00:00");

        assertThat(generateInvalids("2000-W53"))
                .describedAs("Reporting Week format")
                .are(notValidWith(onObservationalTimePeriod()));

        assertThat(asList("2000-W00", "2000-W54"))
                .describedAs("Reporting Week out-of-bounds")
                .are(notValidWith(onObservationalTimePeriod()));

        assertThat(asList("2000-W1", "2000-W001"))
                .describedAs("Reporting Week padding")
                .are(notValidWith(onObservationalTimePeriod()));

        assertThat(onObservationalTimePeriod().parse("2011-W36", ref))
                .describedAs("Reporting Week with start day")
                .isEqualTo("2012-03-05T00:00:00");
    }

    @Test
    public void testReportingDay() {
        assertThat(onObservationalTimePeriod().parse("2000-D366", null))
                .describedAs("Reporting Day")
                .isEqualTo("2000-12-31T00:00:00");

        assertThat(generateInvalids("2000-D366"))
                .describedAs("Reporting Day format")
                .are(notValidWith(onObservationalTimePeriod()));

        assertThat(asList("2000-D000", "2000-D367"))
                .describedAs("Reporting Day out-of-bounds")
                .are(notValidWith(onObservationalTimePeriod()));

        assertThat(asList("2000-D1", "2000-D0001"))
                .describedAs("Reporting Day padding")
                .are(notValidWith(onObservationalTimePeriod()));
    }

    @Test
    public void testTimeRange() {
        // TODO
    }

    private static Condition<String> notValidWith(TimeFormatParser parser) {
        return new Condition<>(text -> parser.parse(text, null) == null, "not valid");
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
