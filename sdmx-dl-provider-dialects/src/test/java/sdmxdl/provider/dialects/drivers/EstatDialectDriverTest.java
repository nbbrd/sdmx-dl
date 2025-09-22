package sdmxdl.provider.dialects.drivers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import sdmxdl.Languages;
import sdmxdl.format.MemCachingSupport;
import sdmxdl.provider.ri.networking.RiNetworking;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.web.spi.DriverAssert;
import tests.sdmxdl.web.spi.EnableWebQueriesOnSystemProperty;

import java.io.IOException;

public class EstatDialectDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new EstatDialectDriver());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "EstatDialectDriverTest.csv", useHeadersInDisplayName = true)
    @EnableWebQueriesOnSystemProperty
    public void testBuiltinSources(String source, int minFlowCount, String flow, int dimCount, String key, int minSeriesCount, int minObsCount, String details) throws IOException {
        DriverAssert.assertBuiltinSource(new EstatDialectDriver(), DriverAssert.SourceQuery
                        .builder()
                        .source(source)
                        .minFlowCount(minFlowCount)
                        .flow(flow)
                        .key(key)
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
