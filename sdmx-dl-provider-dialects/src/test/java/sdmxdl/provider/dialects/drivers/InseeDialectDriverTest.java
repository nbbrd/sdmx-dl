package sdmxdl.provider.dialects.drivers;

import nbbrd.io.text.Parser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import sdmxdl.KeyRequest;
import sdmxdl.format.MemCachingSupport;
import sdmxdl.format.time.*;
import sdmxdl.provider.ri.networking.RiNetworking;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.web.spi.DriverAssert;
import tests.sdmxdl.web.spi.EnableWebQueriesOnSystemProperty;

import java.io.IOException;
import java.time.Year;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.format.time.StandardReportingFormat.REPORTING_QUARTER;
import static sdmxdl.format.time.StandardReportingFormat.REPORTING_SEMESTER;
import static sdmxdl.format.time.TimeFormats.IGNORE_ERROR;
import static sdmxdl.provider.dialects.drivers.InseeDialectDriver.REPORTING_TWO_MONTH;

public class InseeDialectDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new InseeDialectDriver());
    }

    @Test
    public void testPeriodParser() {
        Parser<ObservationalTimePeriod> x = InseeDialectDriver.EXTENDED_TIME_PARSER;
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

    @ParameterizedTest
    @CsvFileSource(resources = "InseeDialectDriverTest.csv", useHeadersInDisplayName = true)
    @EnableWebQueriesOnSystemProperty
    public void testBuiltinSources(String source, String flow, String key, int minFlowCount, int dimCount, int minSeriesCount, int minObsCount, String details) throws IOException {
        DriverAssert.assertBuiltinSource(new InseeDialectDriver(), DriverAssert.SourceQuery
                        .builder()
                        .source(source)
                        .keyRequest(KeyRequest.builder().flowOf(flow).keyOf(key).build())
                        .minFlowCount(minFlowCount)
                        .dimCount(dimCount)
                        .minSeriesCount(minSeriesCount)
                        .minObsCount(minObsCount)
                        .build(),
                context
        );
    }

    private final WebContext context = WebContext
            .builder()
            .caching(MemCachingSupport.builder().id("local").build())
            .networking(new RiNetworking())
            .onEvent(DriverAssert.eventOf(System.out::println))
            .build();
}
