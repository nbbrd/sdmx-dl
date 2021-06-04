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

import nbbrd.io.text.Parser;
import org.junit.Test;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;
import sdmxdl.Key;

import java.net.URL;

import static internal.sdmxdl.ri.web.Sdmx21RestQueries.builder;
import static internal.sdmxdl.ri.web.SdmxResourceType.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.DataFilter.FULL;
import static sdmxdl.DataFilter.SERIES_KEYS_ONLY;

/**
 * @author Philippe Charles
 */
public class Sdmx21RestQueriesTest {

    @Test
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testNPE() {
        for (boolean trailingSlash : new boolean[]{false, true}) {
            Sdmx21RestQueries x = builder().trailingSlashRequired(trailingSlash).build();
            assertThatNullPointerException().isThrownBy(() -> x.getFlowsQuery(null));
            assertThatNullPointerException().isThrownBy(() -> x.getFlowQuery(null, specificFlow));
            assertThatNullPointerException().isThrownBy(() -> x.getFlowQuery(base, null));
            assertThatNullPointerException().isThrownBy(() -> x.getStructureQuery(null, specificStruct));
            assertThatNullPointerException().isThrownBy(() -> x.getStructureQuery(base, null));
            assertThatNullPointerException().isThrownBy(() -> x.getDataQuery(null, specificFlow, Key.ALL, SERIES_KEYS_ONLY));
            assertThatNullPointerException().isThrownBy(() -> x.getDataQuery(base, null, Key.ALL, SERIES_KEYS_ONLY));
            assertThatNullPointerException().isThrownBy(() -> x.getDataQuery(base, specificFlow, null, SERIES_KEYS_ONLY));
            assertThatNullPointerException().isThrownBy(() -> x.getDataQuery(base, specificFlow, Key.ALL, null));
        }
    }

    @Test
    public void testGetFlowsQuery() {
        assertThat(builder().build())
                .satisfies(x -> {
                    assertThat(x.getFlowsQuery(base))
                            .hasToString("http://base/dataflow/all/all/latest");
                });

        assertThat(builder().trailingSlashRequired(true).build().getFlowsQuery(base))
                .hasToString("http://base/dataflow/all/all/latest/");

        assertThat(builder().customResource(DATAFLOW, asList("x", "y")).build().getFlowsQuery(base))
                .hasToString("http://base/x/y/all/all/latest");

        assertThat(builder().customResource(DATAFLOW, emptyList()).build().getFlowsQuery(base))
                .hasToString("http://base/all/all/latest");

        assertThat(builder().customResource(DATAFLOW, null).build().getFlowsQuery(base))
                .hasToString("http://base/dataflow/all/all/latest");
    }

    @Test
    public void testGetFlowQuery() {
        assertThat(builder().build())
                .satisfies(x -> {
                    assertThat(x.getFlowQuery(base, specificFlow))
                            .hasToString("http://base/dataflow/ECB/EXR/1.0");

                    assertThat(x.getFlowQuery(base, genericFlow))
                            .hasToString("http://base/dataflow/all/EXR/latest");
                });

        assertThat(builder().trailingSlashRequired(true).build().getFlowQuery(base, specificFlow))
                .hasToString("http://base/dataflow/ECB/EXR/1.0/");

        assertThat(builder().customResource(DATAFLOW, asList("x", "y")).build().getFlowQuery(base, specificFlow))
                .hasToString("http://base/x/y/ECB/EXR/1.0");

        assertThat(builder().customResource(DATAFLOW, emptyList()).build().getFlowQuery(base, specificFlow))
                .hasToString("http://base/ECB/EXR/1.0");

        assertThat(builder().customResource(DATAFLOW, null).build().getFlowQuery(base, specificFlow))
                .hasToString("http://base/dataflow/ECB/EXR/1.0");
    }

    @Test
    public void testGetStructureQuery() {
        assertThat(builder().build())
                .satisfies(x -> {
                    assertThat(x.getStructureQuery(base, specificStruct))
                            .hasToString("http://base/datastructure/ECB/EXR/1.0?references=children");

                    assertThat(x.getStructureQuery(base, genericStruct))
                            .hasToString("http://base/datastructure/all/EXR/latest?references=children");
                });

        assertThat(builder().trailingSlashRequired(true).build().getStructureQuery(base, specificStruct))
                .hasToString("http://base/datastructure/ECB/EXR/1.0/?references=children");

        assertThat(builder().customResource(DATASTRUCTURE, asList("x", "y")).build().getStructureQuery(base, specificStruct))
                .hasToString("http://base/x/y/ECB/EXR/1.0?references=children");

        assertThat(builder().customResource(DATASTRUCTURE, emptyList()).build().getStructureQuery(base, specificStruct))
                .hasToString("http://base/ECB/EXR/1.0?references=children");

        assertThat(builder().customResource(DATASTRUCTURE, null).build().getStructureQuery(base, specificStruct))
                .hasToString("http://base/datastructure/ECB/EXR/1.0?references=children");
    }

    @Test
    public void testGetDataQuery() {
        assertThat(builder().build())
                .satisfies(x -> {
                    assertThat(x.getDataQuery(base, specificFlow, Key.ALL, FULL))
                            .hasToString("http://base/data/ECB%2CEXR%2C1.0/all/all");

                    assertThat(x.getDataQuery(base, specificFlow, Key.ALL, SERIES_KEYS_ONLY))
                            .hasToString("http://base/data/ECB%2CEXR%2C1.0/all/all?detail=serieskeysonly");

                    assertThat(x.getDataQuery(base, genericFlow, Key.ALL, SERIES_KEYS_ONLY))
                            .hasToString("http://base/data/all%2CEXR%2Clatest/all/all?detail=serieskeysonly");

                    assertThat(x.getDataQuery(base, specificFlow, key, SERIES_KEYS_ONLY))
                            .hasToString("http://base/data/ECB%2CEXR%2C1.0/D.NOK.EUR.SP00.A/all?detail=serieskeysonly");

                    assertThat(x.getDataQuery(base, genericFlow, key, SERIES_KEYS_ONLY))
                            .hasToString("http://base/data/all%2CEXR%2Clatest/D.NOK.EUR.SP00.A/all?detail=serieskeysonly");
                });

        assertThat(builder().trailingSlashRequired(true).build().getDataQuery(base, specificFlow, Key.ALL, FULL))
                .hasToString("http://base/data/ECB%2CEXR%2C1.0/all/all/");

        assertThat(builder().customResource(DATA, asList("x", "y")).build().getDataQuery(base, specificFlow, Key.ALL, FULL))
                .hasToString("http://base/x/y/ECB%2CEXR%2C1.0/all/all");

        assertThat(builder().customResource(DATA, emptyList()).build().getDataQuery(base, specificFlow, Key.ALL, FULL))
                .hasToString("http://base/ECB%2CEXR%2C1.0/all/all");

        assertThat(builder().customResource(DATA, null).build().getDataQuery(base, specificFlow, Key.ALL, FULL))
                .hasToString("http://base/data/ECB%2CEXR%2C1.0/all/all");
    }

    private final URL base = Parser.onURL().parse("http://base");
    private final DataflowRef specificFlow = DataflowRef.of("ECB", "EXR", "1.0");
    private final DataflowRef genericFlow = DataflowRef.of(null, "EXR", null);
    private final DataStructureRef specificStruct = DataStructureRef.of("ECB", "EXR", "1.0");
    private final DataStructureRef genericStruct = DataStructureRef.of(null, "EXR", null);
    private final Key key = Key.parse("D.NOK.EUR.SP00.A");
}
