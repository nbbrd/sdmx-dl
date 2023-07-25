package sdmxdl.provider.ri.web.drivers;

import nbbrd.io.text.Parser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sdmxdl.format.time.*;
import tests.sdmxdl.web.spi.DriverAssert;

import java.time.Year;
import java.time.YearMonth;

import static sdmxdl.provider.ri.web.drivers.InseeDriver2.REPORTING_TWO_MONTH;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.format.time.StandardReportingFormat.REPORTING_QUARTER;
import static sdmxdl.format.time.StandardReportingFormat.REPORTING_SEMESTER;
import static sdmxdl.format.time.TimeFormats.IGNORE_ERROR;

public class InseeDriver2Test {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new InseeDriver2());
    }

    @Test
    public void testPeriodParser() {
        Parser<ObservationalTimePeriod> x = InseeDriver2.EXTENDED_TIME_PARSER;
        assertThat(x.parse("2013")).isEqualTo(GregorianTimePeriod.Year.of(Year.of(2013)));
        assertThat(x.parse("1990-09")).isEqualTo(GregorianTimePeriod.YearMonth.of(YearMonth.of(1990, 9)));
        assertThat(x.parse("2014-Q3")).isEqualTo(ReportingTimePeriod.of(REPORTING_QUARTER, StandardReportingPeriod.parse("2014-Q3")));
        assertThat(x.parse("2012-S2")).isEqualTo(ReportingTimePeriod.of(REPORTING_SEMESTER, StandardReportingPeriod.parse("2012-S2")));
        assertThat(x.parse("2012-B2")).isEqualTo(ReportingTimePeriod.of(REPORTING_TWO_MONTH, StandardReportingPeriod.parse("2012-B2")));
    }

    @Test
    public void testReportingTwoMonth() {
        Parser<ObservationalTimePeriod> x = TimeFormats.onReportingFormat(REPORTING_TWO_MONTH, IGNORE_ERROR);
        assertThat(x.parse("2012-B2"))
                .isEqualTo(ReportingTimePeriod.of(REPORTING_TWO_MONTH, StandardReportingPeriod.parse("2012-B2")))
                .extracting(o -> o.toStartTime(null), Assertions.LOCAL_DATE_TIME)
                .isEqualTo("2012-03-01T00:00:00");
    }
}
