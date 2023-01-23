package sdmxdl.format.time;

import org.junit.jupiter.api.Test;

import java.time.MonthDay;
import java.time.format.DateTimeParseException;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static sdmxdl.format.time.TimeFormatsTest.*;
import static sdmxdl.format.time.StandardReportingFormat.*;
import static sdmxdl.format.time.StandardReportingPeriod.parse;

public class StandardReportingPeriodTest {

    static final StandardReportingPeriod S = StandardReportingPeriod
            .builder()
            .reportingYear(2010)
            .periodIndicator('S')
            .periodValue(2)
            .periodValueDigits(1)
            .build();

    static final StandardReportingPeriod M2 = StandardReportingPeriod
            .builder()
            .reportingYear(2010)
            .periodIndicator('M')
            .periodValue(3)
            .periodValueDigits(2)
            .build();

    static final StandardReportingPeriod M1 = StandardReportingPeriod
            .builder()
            .reportingYear(2010)
            .periodIndicator('M')
            .periodValue(3)
            .periodValueDigits(1)
            .build();

    @Test
    public void testToString() {
        assertThat(S.toString()).isEqualTo("2010-S2");
        assertThat(M2.toString()).isEqualTo("2010-M03");
    }

    @Test
    public void testParse() {
        assertThat(parse("2010-S2")).isEqualTo(S);
        assertThat(parse("2010-M03")).isEqualTo(M2);
        assertThat(parse("2010-M3")).isEqualTo(M1);

        assertThatExceptionOfType(DateTimeParseException.class).isThrownBy(() -> parse("2010-M"));
        assertThatExceptionOfType(DateTimeParseException.class).isThrownBy(() -> parse("2010-03"));
        assertThatExceptionOfType(DateTimeParseException.class).isThrownBy(() -> parse("2010M03"));
        assertThatExceptionOfType(DateTimeParseException.class).isThrownBy(() -> parse("010-M03"));

        assertThat(generateInvalids("2000-A1"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Year format")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(asList("2000-A0", "2000-A2"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Year out-of-bounds")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(generateInvalids("2000-S2"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Semester format")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(asList("2000-S0", "2000-S3"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Semester out-of-bounds")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(generateInvalids("2000-T3"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Trimester format")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(asList("2000-T0", "2000-T4"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Trimester out-of-bounds")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(generateInvalids("2000-Q4"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Quarter format")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(asList("2000-Q0", "2000-Q5"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Quarter out-of-bounds")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(generateInvalids("2000-M12"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Month format")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(asList("2000-M00", "2000-M13"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Month out-of-bounds")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(asList("2000-M1", "2000-M001"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Month padding")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(generateInvalids("2000-W53"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Week format")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(asList("2000-W00", "2000-W54"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Week out-of-bounds")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(asList("2000-W1", "2000-W001"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Week padding")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(generateInvalids("2000-D366"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Day format")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(asList("2000-D000", "2000-D367"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Day out-of-bounds")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));

        assertThat(asList("2000-D1", "2000-D0001"))
                .filteredOn(not(StandardReportingPeriod::isParsable))
                .describedAs("Reporting Day padding")
                .are(throwingDateTimeParseExceptionOn(StandardReportingPeriod::parse));
    }

    @Test
    public void testToStartDate() {
        assertThat(parse("2000-A1").toStartDate(REPORTING_YEAR, null))
                .describedAs("Reporting Year")
                .isEqualTo("2000-01-01");

        assertThat(parse("2000-S2").toStartDate(REPORTING_SEMESTER, null))
                .describedAs("Reporting Semester")
                .isEqualTo("2000-07-01");

        assertThat(parse("2000-T3").toStartDate(REPORTING_TRIMESTER, null))
                .describedAs("Reporting Trimester")
                .isEqualTo("2000-09-01");

        assertThat(parse("2000-Q4").toStartDate(REPORTING_QUARTER, null))
                .describedAs("Reporting Quarter")
                .isEqualTo("2000-10-01");

        assertThat(parse("2010-Q2").toStartDate(REPORTING_QUARTER, null))
                .describedAs("Reporting Quarter without start day")
                .isEqualTo("2010-04-01");

        assertThat(parse("2010-Q2").toStartDate(REPORTING_QUARTER, MonthDay.of(7, 1)))
                .describedAs("Reporting Quarter with start day")
                .isEqualTo("2010-10-01");

        assertThat(parse("2000-M12").toStartDate(REPORTING_MONTH, null))
                .describedAs("Reporting Month")
                .isEqualTo("2000-12-01");

        assertThat(parse("2000-W53").toStartDate(REPORTING_WEEK, null))
                .describedAs("Reporting Week")
                .isEqualTo("2001-01-01");

        assertThat(parse("2011-W36").toStartDate(REPORTING_WEEK, null))
                .describedAs("Reporting Week without start day")
                .isEqualTo("2011-09-05");

        assertThat(parse("2011-W36").toStartDate(REPORTING_WEEK, MonthDay.of(7, 1)))
                .describedAs("Reporting Week with start day")
                .isEqualTo("2012-03-05");

        assertThat(parse("2000-D366").toStartDate(REPORTING_DAY, null))
                .describedAs("Reporting Day")
                .isEqualTo("2000-12-31");
    }
}
