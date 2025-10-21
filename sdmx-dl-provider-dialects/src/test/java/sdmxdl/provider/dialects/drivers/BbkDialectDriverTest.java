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
package sdmxdl.provider.dialects.drivers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import sdmxdl.*;
import sdmxdl.format.MemCachingSupport;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.ri.networking.RiNetworking;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.web.spi.DriverAssert;
import tests.sdmxdl.web.spi.EnableWebQueriesOnSystemProperty;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.Detail.FULL;
import static sdmxdl.Detail.SERIES_KEYS_ONLY;

/**
 * @author Philippe Charles
 */
public class BbkDialectDriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new BbkDialectDriver());
    }

    @Test
    public void testQueries() throws MalformedURLException {
        URL endpoint = new URL(" https://api.statistiken.bundesbank.de/rest");

        BbkDialectDriver.BbkQueries queries = BbkDialectDriver.BbkQueries.INSTANCE;

        assertThat(queries.getFlowsQuery(endpoint).build())
                .describedAs("SdmxFix#1 + SdmxFix#2 + SdmxFix#3")
                .hasToString("https://api.statistiken.bundesbank.de/rest/metadata/dataflow/BBK");

        assertThat(queries.getFlowQuery(endpoint, FlowRef.parse("BBEX3")).build())
                .describedAs("SdmxFix#1 + SdmxFix#2")
                .hasToString("https://api.statistiken.bundesbank.de/rest/metadata/dataflow/BBK/BBEX3");

        assertThat(queries.getStructureQuery(endpoint, StructureRef.parse("BBK_ERX")).build())
                .describedAs("SdmxFix#1 + SdmxFix#2")
                .hasToString("https://api.statistiken.bundesbank.de/rest/metadata/datastructure/BBK/BBK_ERX?references=children");

        assertThat(queries.getDataQuery(endpoint, DataRef.of(FlowRef.parse("BBEX3"), Query.builder().key(Key.parse("M.ISK.EUR+USD.CA.AC.A01")).detail(FULL).build()), StructureRef.parse("abc")).build())
                .describedAs("SdmxFix#4")
                .hasToString("https://api.statistiken.bundesbank.de/rest/data/BBEX3/M.ISK.EUR%2BUSD.CA.AC.A01");

        assertThat(queries.getDataQuery(endpoint, DataRef.of(FlowRef.parse("BBEX3"), Query.builder().key(Key.parse("M.ISK.EUR+USD.CA.AC.A01")).detail(SERIES_KEYS_ONLY).build()), StructureRef.parse("abc")).build())
                .describedAs("SdmxFix#5")
                .hasToString("https://api.statistiken.bundesbank.de/rest/data/BBEX3/M.ISK.EUR%2BUSD.CA.AC.A01?detail=serieskeyonly");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "BbkDialectDriverTest.csv", useHeadersInDisplayName = true)
    @EnableWebQueriesOnSystemProperty
    public void testBuiltinSources(String source, String flow, String key, int minFlowCount, int dimCount, int minSeriesCount, int minObsCount, String details) throws IOException {
        DriverAssert.assertBuiltinSource(new BbkDialectDriver(), DriverAssert.SourceQuery
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
