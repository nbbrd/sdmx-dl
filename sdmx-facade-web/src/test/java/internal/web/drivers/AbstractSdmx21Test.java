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
package internal.web.drivers;

import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import static internal.web.drivers.AbstractSdmx21.*;
import java.io.IOException;
import java.net.URL;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class AbstractSdmx21Test {

    @Test
    @SuppressWarnings("null")
    public void testGetFlowsQuery() throws IOException {
        URL endpoint = new URL("http://localhost");

        assertThatNullPointerException().isThrownBy(() -> getFlowsQuery(null));

        assertThat(getFlowsQuery(endpoint).build())
                .hasToString("http://localhost/dataflow/all/all/latest");
    }

    @Test
    @SuppressWarnings("null")
    public void testGetFlowQuery() throws IOException {
        URL endpoint = new URL("http://localhost");

        assertThatNullPointerException().isThrownBy(() -> getFlowQuery(null, specificFlow));
        assertThatNullPointerException().isThrownBy(() -> getFlowQuery(endpoint, null));

        assertThat(getFlowQuery(endpoint, specificFlow).build())
                .hasToString("http://localhost/dataflow/ECB/EXR/1.0");

        assertThat(getFlowQuery(endpoint, genericFlow).build())
                .hasToString("http://localhost/dataflow/all/EXR/latest");
    }

    @Test
    @SuppressWarnings("null")
    public void testGetStructureQuery() throws IOException {
        URL endpoint = new URL("http://localhost");

        assertThatNullPointerException().isThrownBy(() -> getStructureQuery(null, specificStruct));
        assertThatNullPointerException().isThrownBy(() -> getStructureQuery(endpoint, null));

        assertThat(getStructureQuery(endpoint, specificStruct).build())
                .hasToString("http://localhost/datastructure/ECB/EXR/1.0?references=children");

        assertThat(getStructureQuery(endpoint, genericStruct).build())
                .hasToString("http://localhost/datastructure/all/EXR/latest?references=children");
    }

    @Test
    @SuppressWarnings("null")
    public void testGetDataQuery() throws IOException {
        URL endpoint = new URL("http://localhost");

        assertThatNullPointerException().isThrownBy(() -> getDataQuery(null, specificFlow, DataQuery.of(Key.ALL, true)));
        assertThatNullPointerException().isThrownBy(() -> getDataQuery(endpoint, null, DataQuery.of(Key.ALL, true)));
        assertThatNullPointerException().isThrownBy(() -> getDataQuery(endpoint, specificFlow, null));

        assertThat(getDataQuery(endpoint, specificFlow, DataQuery.of(Key.ALL, true)).build())
                .hasToString("http://localhost/data/ECB%2CEXR%2C1.0/all/all?detail=serieskeysonly");

        assertThat(getDataQuery(endpoint, genericFlow, DataQuery.of(Key.ALL, true)).build())
                .hasToString("http://localhost/data/all%2CEXR%2Clatest/all/all?detail=serieskeysonly");
    }

    private final DataflowRef specificFlow = DataflowRef.of("ECB", "EXR", "1.0");
    private final DataflowRef genericFlow = DataflowRef.of(null, "EXR", null);
    private final DataStructureRef specificStruct = DataStructureRef.of("ECB", "EXR", "1.0");
    private final DataStructureRef genericStruct = DataStructureRef.of(null, "EXR", null);
}
