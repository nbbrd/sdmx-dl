package sdmxdl.format.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.format.time.StandardReportingFormat.REPORTING_MONTH;
import static sdmxdl.format.time.StandardReportingFormat.REPORTING_YEAR;
import static sdmxdl.format.time.StandardReportingPeriodTest.M2;
import static sdmxdl.format.time.TimeFormatsTest.*;

@SuppressWarnings("DataFlowIssue")
public class ReportingTimePeriodTest {

    @Test
    public void testOf() {
        assertThatNullPointerException()
                .isThrownBy(() -> ReportingTimePeriod.of(null, M2));

        assertThatNullPointerException()
                .isThrownBy(() -> ReportingTimePeriod.of(REPORTING_MONTH, null));

        assertThat(ReportingTimePeriod.of(REPORTING_MONTH, M2))
                .isEqualTo(ReportingTimePeriod.of(REPORTING_MONTH, M2))
                .extracting(ReportingTimePeriod::getFormat, ReportingTimePeriod::getPeriod)
                .contains(REPORTING_MONTH, M2);
    }

    @Test
    public void testParse() {
        assertThatNullPointerException()
                .isThrownBy(() -> ReportingTimePeriod.parse(null));

        assertThat(ReportingTimePeriod.parse("2010-M03"))
                .isEqualTo(ReportingTimePeriod.of(REPORTING_MONTH, M2));

        assertThat(generateInvalids("2010-M03"))
                .describedAs("Invalid format")
                .are(throwingDateTimeParseExceptionOn(ReportingTimePeriod::parse));

        assertThat(asList("2000-A0", "2000-A2"))
                .describedAs("Out-of-bounds")
                .are(throwingDateTimeParseExceptionOn(ReportingTimePeriod::parse));
    }

    @Test
    public void testIsParsable() {
        assertThat(ReportingTimePeriod.isParsable(null)).isFalse();

        assertThat(ReportingTimePeriod.isParsable("2010-M03")).isTrue();

        assertThat(generateInvalids("2010-M03"))
                .filteredOn(not(ReportingTimePeriod::isParsable))
                .are(throwingDateTimeParseExceptionOn(ReportingTimePeriod::parse));
    }

    @Test
    public void testParseWith() {
        assertThatNullPointerException()
                .isThrownBy(() -> ReportingTimePeriod.parseWith(null, REPORTING_MONTH));

        assertThatNullPointerException()
                .isThrownBy(() -> ReportingTimePeriod.parseWith("2010-M03", null));

        assertThat(ReportingTimePeriod.parseWith("2010-M03", REPORTING_MONTH))
                .isEqualTo(ReportingTimePeriod.of(REPORTING_MONTH, M2));

        assertThat("2010-M03")
                .is(throwingDateTimeParseExceptionOn(o -> ReportingTimePeriod.parseWith(o, REPORTING_YEAR)));

        assertThat(generateInvalids("2010-M03"))
                .describedAs("Invalid format")
                .are(throwingDateTimeParseExceptionOn(o -> ReportingTimePeriod.parseWith(o, REPORTING_MONTH)));

        assertThat(asList("2000-A0", "2000-A2"))
                .describedAs("Out-of-bounds")
                .are(throwingDateTimeParseExceptionOn(o -> ReportingTimePeriod.parseWith(o, REPORTING_YEAR)));
    }

    @Test
    public void testIsParsableWith() {
        assertThatNullPointerException()
                .isThrownBy(() -> ReportingTimePeriod.isParsableWith("2010-M03", null));

        assertThat(ReportingTimePeriod.isParsableWith(null, REPORTING_MONTH)).isFalse();

        assertThat(ReportingTimePeriod.isParsableWith("2010-M03", REPORTING_MONTH)).isTrue();

        assertThat(ReportingTimePeriod.isParsableWith("2010-M03", REPORTING_YEAR)).isFalse();

        assertThat(generateInvalids("2010-M03"))
                .filteredOn(not(o -> ReportingTimePeriod.isParsableWith(o, REPORTING_MONTH)))
                .are(throwingDateTimeParseExceptionOn(o -> ReportingTimePeriod.parseWith(o, REPORTING_MONTH)));
    }

    @Test
    public void testToString() {
        assertThat(ReportingTimePeriod.of(REPORTING_MONTH, M2))
                .hasToString("2010-M03");
    }

    @Test
    public void testToStartTime() {
        assertThat(ReportingTimePeriod.of(REPORTING_MONTH, M2).toStartTime(null))
                .isEqualTo(LocalDateTime.of(2010, 3, 1, 0, 0));
    }
}
