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
package internal.sdmxdl.provider.ri.web.drivers;

import org.junit.jupiter.api.Test;
import sdmxdl.DataQuery;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.provider.DataRef;
import tests.sdmxdl.web.WebDriverAssert;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.DataDetail.FULL;
import static sdmxdl.DataDetail.SERIES_KEYS_ONLY;

/**
 * @author Philippe Charles
 */
public class BbkDriverTest {

    @Test
    public void testCompliance() {
        WebDriverAssert.assertCompliance(new BbkDriver());
    }

    @Test
    public void testQueries() throws MalformedURLException {
        URL endpoint = new URL(" https://api.statistiken.bundesbank.de/rest");

        BbkDriver.BbkQueries queries = new BbkDriver.BbkQueries();

        assertThat(queries.getFlowsQuery(endpoint).build())
                .describedAs("SdmxFix#1 + SdmxFix#2 + SdmxFix#3")
                .hasToString("https://api.statistiken.bundesbank.de/rest/metadata/dataflow/BBK");

        assertThat(queries.getFlowQuery(endpoint, DataflowRef.parse("BBEX3")).build())
                .describedAs("SdmxFix#1 + SdmxFix#2")
                .hasToString("https://api.statistiken.bundesbank.de/rest/metadata/dataflow/BBK/BBEX3");

        assertThat(queries.getStructureQuery(endpoint, DataStructureRef.parse("BBK_ERX")).build())
                .describedAs("SdmxFix#1 + SdmxFix#2")
                .hasToString("https://api.statistiken.bundesbank.de/rest/metadata/datastructure/BBK/BBK_ERX?references=children");

        assertThat(queries.getDataQuery(endpoint, DataRef.of(DataflowRef.parse("BBEX3"), DataQuery.builder().key(Key.parse("M.ISK.EUR+USD.CA.AC.A01")).detail(FULL).build()), DataStructureRef.parse("abc")).build())
                .describedAs("SdmxFix#4")
                .hasToString("https://api.statistiken.bundesbank.de/rest/data/BBEX3/M.ISK.EUR%2BUSD.CA.AC.A01");

        assertThat(queries.getDataQuery(endpoint, DataRef.of(DataflowRef.parse("BBEX3"), DataQuery.builder().key(Key.parse("M.ISK.EUR+USD.CA.AC.A01")).detail(SERIES_KEYS_ONLY).build()), DataStructureRef.parse("abc")).build())
                .describedAs("SdmxFix#5")
                .hasToString("https://api.statistiken.bundesbank.de/rest/data/BBEX3/M.ISK.EUR%2BUSD.CA.AC.A01?detail=serieskeyonly");
    }
}