package sdmxdl.provider.dialects.drivers;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import sdmxdl.KeyRequest;
import sdmxdl.provider.caching.MemCachingSupport;
import sdmxdl.provider.ri.networking.RiNetworking;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.web.spi.DriverAssert;

import java.io.IOException;

import static nbbrd.io.text.BaseProperty.keysOf;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.provider.ri.http.DumpingDecoration.DUMP_FOLDER_PROPERTY;
import static sdmxdl.provider.ri.http.RetryDecoration.MAX_RETRIES_PROPERTY;
import static sdmxdl.provider.web.DriverProperties.*;

public class UisDialectDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new UisDialectDriver());
    }

    @Test
    public void testProperties() {
        assertThat(new UisDialectDriver().getDriverPropertyNames())
                .containsExactlyInAnyOrderElementsOf(
                        keysOf(
                                CONNECT_TIMEOUT_PROPERTY,
                                READ_TIMEOUT_PROPERTY,
                                USER_AGENT_PROPERTY,
                                AUTH_SCHEME_PROPERTY,
                                MAX_REDIRECTS_PROPERTY,
                                MAX_RETRIES_PROPERTY,
                                DUMP_FOLDER_PROPERTY,
                                CACHE_TTL_PROPERTY)
                );
    }

    @ParameterizedTest
    @CsvFileSource(resources = "UisDialectDriverTest.csv", useHeadersInDisplayName = true)
    @Tag("webQueries")
    public void testBuiltinSources(String source, String flow, String key, int minFlowCount, int dimCount, int minSeriesCount, int minObsCount, String details) throws IOException {
        DriverAssert.assertBuiltinSource(new UisDialectDriver(), DriverAssert.SourceQuery
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
            .onEvent(source -> DriverAssert.eventOf(source, System.out::println))
            .build();
}
