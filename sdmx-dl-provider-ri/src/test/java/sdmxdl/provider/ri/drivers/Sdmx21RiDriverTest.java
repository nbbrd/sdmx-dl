/*
 * Copyright 2018 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package sdmxdl.provider.ri.drivers;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import sdmxdl.KeyRequest;
import sdmxdl.provider.caching.MemCachingSupport;
import sdmxdl.provider.ri.http.CachingDecoration;
import sdmxdl.provider.ri.networking.RiNetworking;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.web.spi.DriverAssert;

import java.io.IOException;

import static nbbrd.io.text.BaseProperty.keysOf;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.provider.ri.http.CachingDecoration.HTTP_CACHING_PROPERTY;
import static sdmxdl.provider.ri.http.CookieDecoration.COOKIE_PROPERTY;
import static sdmxdl.provider.ri.http.DumpingDecoration.DUMP_FOLDER_PROPERTY;
import static sdmxdl.provider.ri.http.RateLimitingDecoration.RATE_LIMITING_PROPERTY;
import static sdmxdl.provider.ri.http.RetryDecoration.MAX_RETRIES_PROPERTY;
import static sdmxdl.provider.web.DriverProperties.*;

/**
 * @author Philippe Charles
 */
public class Sdmx21RiDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new Sdmx21RiDriver());
    }

    @Test
    public void testProperties() {
        assertThat(new Sdmx21RiDriver().getDriverPropertyNames())
                .containsExactlyInAnyOrderElementsOf(
                        keysOf(
                                CONNECT_TIMEOUT_PROPERTY,
                                READ_TIMEOUT_PROPERTY,
                                USER_AGENT_PROPERTY,
                                AUTH_SCHEME_PROPERTY,
                                MAX_REDIRECTS_PROPERTY,
                                MAX_RETRIES_PROPERTY,
                                DUMP_FOLDER_PROPERTY,
                                DETAIL_SUPPORTED_PROPERTY,
                                TRAILING_SLASH_PROPERTY,
                                CACHE_TTL_PROPERTY,
                                RATE_LIMITING_PROPERTY,
                                HTTP_CACHING_PROPERTY,
                                COOKIE_PROPERTY)
                );
    }

    @ParameterizedTest
    @CsvFileSource(resources = "Sdmx21RiDriverTest.csv", useHeadersInDisplayName = true)
    @Tag("webQueries")
    public void testBuiltinSources(String source, String flow, String key, int minFlowCount, int dimCount, int minSeriesCount, int minObsCount, String details) throws IOException {
        DriverAssert.assertBuiltinSource(new Sdmx21RiDriver(), DriverAssert.SourceQuery
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
