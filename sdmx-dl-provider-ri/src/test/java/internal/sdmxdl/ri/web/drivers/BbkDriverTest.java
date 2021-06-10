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
package internal.sdmxdl.ri.web.drivers;

import org.junit.Test;
import sdmxdl.DataFilter;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.tck.web.SdmxWebDriverAssert;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class BbkDriverTest {

    @Test
    public void testCompliance() {
        SdmxWebDriverAssert.assertCompliance(new BbkDriver());
    }

    @Test
    public void testQueries() throws MalformedURLException {
        URL endpoint = new URL(" https://api.statistiken.bundesbank.de/rest");

        BbkDriver.BbkQueries queries = BbkDriver.BbkQueries.INSTANCE;

        assertThat(queries.getFlowsQuery(endpoint).build())
                .hasToString("https://api.statistiken.bundesbank.de/rest/metadata/dataflow/BBK");

        assertThat(queries.getFlowQuery(endpoint, DataflowRef.parse("BBEX3")).build())
                .hasToString("https://api.statistiken.bundesbank.de/rest/metadata/dataflow/BBK/BBEX3");

        assertThat(queries.getStructureQuery(endpoint, DataStructureRef.parse("BBK_ERX")).build())
                .hasToString("https://api.statistiken.bundesbank.de/rest/metadata/datastructure/BBK/BBK_ERX?references=children");

        assertThat(queries.getDataQuery(endpoint, DataflowRef.parse("BBEX3"), Key.parse("M.ISK.EUR+USD.CA.AC.A01"), DataFilter.FULL).build())
                .hasToString("https://api.statistiken.bundesbank.de/rest/data/BBEX3/M.ISK.EUR%2BUSD.CA.AC.A01");
    }
}
