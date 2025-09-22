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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import sdmxdl.Languages;
import sdmxdl.format.MemCachingSupport;
import sdmxdl.provider.ri.networking.RiNetworking;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.web.spi.DriverAssert;
import tests.sdmxdl.web.spi.EnableWebQueriesOnSystemProperty;

import java.io.IOException;

/**
 * @author Philippe Charles
 */
public class Sdmx21RiDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new Sdmx21RiDriver());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "Sdmx21RiDriverTest.csv", useHeadersInDisplayName = true)
    @EnableWebQueriesOnSystemProperty
    public void testBuiltinSources(String source, int minFlowCount, String flow, int dimCount, String key, int minSeriesCount, int minObsCount, String details) throws IOException {
        DriverAssert.assertBuiltinSource(new Sdmx21RiDriver(), DriverAssert.SourceQuery
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
