package sdmxdl.format.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.format.ObsTimeParserTest.generateInvalids;
import static sdmxdl.format.ObsTimeParserTest.throwingDateTimeParseExceptionOn;
import static sdmxdl.format.time.StandardReportingFormat.REPORTING_MONTH;
import static sdmxdl.format.time.StandardReportingPeriodTest.M2;

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
                .are(throwingDateTimeParseExceptionOn(GregorianTimePeriod.Year::parse));
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
