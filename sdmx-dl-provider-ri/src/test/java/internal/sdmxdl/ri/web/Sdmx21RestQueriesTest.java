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
package internal.sdmxdl.ri.web;

import org.junit.Test;
import sdmxdl.DataFilter;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.util.web.DataRequest;

import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * @author Philippe Charles
 */
public class Sdmx21RestQueriesTest {

    private final Sdmx21RestQueries x = new Sdmx21RestQueries(false);

    @Test
    @SuppressWarnings("null")
    public void testGetFlowsQuery() throws IOException {
        URL endpoint = new URL("http://localhost");

        assertThatNullPointerException().isThrownBy(() -> x.getFlowsQuery(null));

        assertThat(x.getFlowsQuery(endpoint).build())
                .hasToString("http://localhost/dataflow/all/all/latest");
    }

    @Test
    @SuppressWarnings("null")
    public void testGetFlowQuery() throws IOException {
        URL endpoint = new URL("http://localhost");

        assertThatNullPointerException().isThrownBy(() -> x.getFlowQuery(null, specificFlow));
        assertThatNullPointerException().isThrownBy(() -> x.getFlowQuery(endpoint, null));

        assertThat(x.getFlowQuery(endpoint, specificFlow).build())
                .hasToString("http://localhost/dataflow/ECB/EXR/1.0");

        assertThat(x.getFlowQuery(endpoint, genericFlow).build())
                .hasToString("http://localhost/dataflow/all/EXR/latest");
    }

    @Test
    @SuppressWarnings("null")
    public void testGetStructureQuery() throws IOException {
        URL endpoint = new URL("http://localhost");

        assertThatNullPointerException().isThrownBy(() -> x.getStructureQuery(null, specificStruct));
        assertThatNullPointerException().isThrownBy(() -> x.getStructureQuery(endpoint, null));

        assertThat(x.getStructureQuery(endpoint, specificStruct).build())
                .hasToString("http://localhost/datastructure/ECB/EXR/1.0?references=children");

        assertThat(x.getStructureQuery(endpoint, genericStruct).build())
                .hasToString("http://localhost/datastructure/all/EXR/latest?references=children");
    }

    @Test
    @SuppressWarnings("null")
    public void testGetDataQuery() throws IOException {
        URL endpoint = new URL("http://localhost");

        DataRequest specificRequest = new DataRequest(specificFlow, Key.ALL, DataFilter.SERIES_KEYS_ONLY);
        DataRequest genericRequest = new DataRequest(genericFlow, Key.ALL, DataFilter.SERIES_KEYS_ONLY);

        assertThatNullPointerException().isThrownBy(() -> x.getDataQuery(null, specificRequest));
        assertThatNullPointerException().isThrownBy(() -> x.getDataQuery(endpoint, null));

        assertThat(x.getDataQuery(endpoint, specificRequest).build())
                .hasToString("http://localhost/data/ECB%2CEXR%2C1.0/all/all?detail=serieskeysonly");

        assertThat(x.getDataQuery(endpoint, genericRequest).build())
                .hasToString("http://localhost/data/all%2CEXR%2Clatest/all/all?detail=serieskeysonly");
    }

    private final DataflowRef specificFlow = DataflowRef.of("ECB", "EXR", "1.0");
    private final DataflowRef genericFlow = DataflowRef.of(null, "EXR", null);
    private final DataStructureRef specificStruct = DataStructureRef.of("ECB", "EXR", "1.0");
    private final DataStructureRef genericStruct = DataStructureRef.of(null, "EXR", null);
}
