package sdmxdl.util.parser;

import nbbrd.io.text.Parser;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.util.parser.TimeFormatParsers.FIRST_DAY_OF_YEAR;

public class TimeFormatParsersTest {

    final Parser<LocalDateTime> x = TimeFormatParsers.getObservationalTimePeriod(FIRST_DAY_OF_YEAR);
    final Parser<LocalDateTime> y = TimeFormatParsers.getObservationalTimePeriod(MonthDay.of(7, 1));

    @Test
    public void testGetObservationalTimePeriod() {
        assertThatNullPointerException()
                .isThrownBy(() -> TimeFormatParsers.getObservationalTimePeriod(null));
    }

    @Test
    public void testGregorianYear() {
        assertThat(x.parse("2001"))
                .describedAs("Gregorian Year")
                .isEqualTo("2001-01-01T00:00:00");

        assertThat(generateInvalids("2001"))
                .describedAs("Gregorian Year format")
                .are(notValidWith(x));
    }

    @Test
    public void testGregorianYearMonth() {
        assertThat(x.parse("2001-02"))
                .describedAs("Gregorian Year Month")
                .isEqualTo("2001-02-01T00:00:00");

        assertThat(generateInvalids("2001-02"))
                .describedAs("Gregorian Year Month format")
                .are(notValidWith(x));
    }

    @Test
    public void testGregorianDay() {
        assertThat(x.parse("2001-02-03"))
                .describedAs("Gregorian Day")
                .isEqualTo("2001-02-03T00:00:00");

        assertThat(generateInvalids("2001-02-03"))
                .describedAs("Gregorian Day format")
                .are(notValidWith(x));

        assertThat("2019-02-29")
                .describedAs("Gregorian Day strict")
                .is(notValidWith(x));
    }

    @Test
    public void testDateTime() {
        assertThat(x.parse("2001-02-03T04:05:06"))
                .describedAs("Date Time")
                .isEqualTo("2001-02-03T04:05:06");

        assertThat(generateInvalids("2001-02-03T04:05:06"))
                .describedAs("Date Time format")
                .are(notValidWith(x));

        assertThat("2019-02-29T11:23:56")
                .describedAs("Date Time strict")
                .is(notValidWith(x));
    }

    @Test
    public void testReportingYear() {
        assertThat(x.parse("2000-A1"))
                .describedAs("Reporting Year")
                .isEqualTo("2000-01-01T00:00:00");

        assertThat(generateInvalids("2000-A1"))
                .describedAs("Reporting Year format")
                .are(notValidWith(x));

        assertThat(asList("2000-A0", "2000-A2"))
                .describedAs("Reporting Year out-of-bounds")
                .are(notValidWith(x));
    }

    @Test
    public void testReportingSemester() {
        assertThat(x.parse("2000-S2"))
                .describedAs("Reporting Semester")
                .isEqualTo("2000-07-01T00:00:00");

        assertThat(generateInvalids("2000-S2"))
                .describedAs("Reporting Semester format")
                .are(notValidWith(x));

        assertThat(asList("2000-S0", "2000-S3"))
                .describedAs("Reporting Semester out-of-bounds")
                .are(notValidWith(x));
    }

    @Test
    public void testReportingTrimester() {
        assertThat(x.parse("2000-T3"))
                .describedAs("Reporting Trimester")
                .isEqualTo("2000-09-01T00:00:00");

        assertThat(generateInvalids("2000-T3"))
                .describedAs("Reporting Trimester format")
                .are(notValidWith(x));

        assertThat(asList("2000-T0", "2000-T4"))
                .describedAs("Reporting Trimester out-of-bounds")
                .are(notValidWith(x));
    }

    @Test
    public void testReportingQuarter() {
        assertThat(x.parse("2000-Q4"))
                .describedAs("Reporting Quarter")
                .isEqualTo("2000-10-01T00:00:00");

        assertThat(generateInvalids("2000-Q4"))
                .describedAs("Reporting Quarter format")
                .are(notValidWith(x));

        assertThat(asList("2000-Q0", "2000-Q5"))
                .describedAs("Reporting Quarter out-of-bounds")
                .are(notValidWith(x));

        assertThat(y.parse("2010-Q2"))
                .describedAs("Reporting Quarter with start day")
                .isEqualTo("2010-10-01T00:00:00");
    }

    @Test
    public void testReportingMonth() {
        assertThat(x.parse("2000-M12"))
                .describedAs("Reporting Month")
                .isEqualTo("2000-12-01T00:00:00");

        assertThat(generateInvalids("2000-M12"))
                .describedAs("Reporting Month format")
                .are(notValidWith(x));

        assertThat(asList("2000-M00", "2000-M13"))
                .describedAs("Reporting Month out-of-bounds")
                .are(notValidWith(x));

        assertThat(asList("2000-M1", "2000-M001"))
                .describedAs("Reporting Month padding")
                .are(notValidWith(x));
    }

    @Test
    public void testReportingWeek() {
        assertThat(x.parse("2000-W53"))
                .describedAs("Reporting Week")
                .isEqualTo("2001-01-01T00:00");

        assertThat(generateInvalids("2000-W53"))
                .describedAs("Reporting Week format")
                .are(notValidWith(x));

        assertThat(asList("2000-W00", "2000-W54"))
                .describedAs("Reporting Week out-of-bounds")
                .are(notValidWith(x));

        assertThat(asList("2000-W1", "2000-W001"))
                .describedAs("Reporting Week padding")
                .are(notValidWith(x));

        assertThat(y.parse("2011-W36"))
                .describedAs("Reporting Week with start day")
                .isEqualTo("2012-03-05T00:00:00");
    }

    @Test
    public void testReportingDay() {
        assertThat(x.parse("2000-D366"))
                .describedAs("Reporting Day")
                .isEqualTo("2000-12-31T00:00:00");

        assertThat(generateInvalids("2000-D366"))
                .describedAs("Reporting Day format")
                .are(notValidWith(x));

        assertThat(asList("2000-D000", "2000-D367"))
                .describedAs("Reporting Day out-of-bounds")
                .are(notValidWith(x));

        assertThat(asList("2000-D1", "2000-D0001"))
                .describedAs("Reporting Day padding")
                .are(notValidWith(x));
    }

    @Test
    public void testTimeRange() {
        // TODO
    }

    private static Condition<String> notValidWith(Parser<?> parser) {
        return new Condition<>(text -> parser.parse(text) == null, "not valid");
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
