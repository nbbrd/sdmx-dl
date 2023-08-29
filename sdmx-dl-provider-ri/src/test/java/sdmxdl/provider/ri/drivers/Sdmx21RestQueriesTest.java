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

import nbbrd.io.text.Parser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sdmxdl.provider.ri.drivers.Sdmx21RestQueries;
import sdmxdl.*;
import sdmxdl.provider.DataRef;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.DataDetail.FULL;
import static sdmxdl.DataDetail.SERIES_KEYS_ONLY;

/**
 * @author Philippe Charles
 */
public class Sdmx21RestQueriesTest {

    @Test
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testNPE() {
        for (boolean trailingSlash : new boolean[]{false, true}) {
            Sdmx21RestQueries x = Sdmx21RestQueries.builder().trailingSlashRequired(trailingSlash).build();
            assertThatNullPointerException().isThrownBy(() -> x.getFlowsQuery(null));
            assertThatNullPointerException().isThrownBy(() -> x.getFlowQuery(null, specificFlow));
            assertThatNullPointerException().isThrownBy(() -> x.getFlowQuery(base, null));
            assertThatNullPointerException().isThrownBy(() -> x.getStructureQuery(null, specificStruct));
            assertThatNullPointerException().isThrownBy(() -> x.getStructureQuery(base, null));
            assertThatNullPointerException().isThrownBy(() -> x.getDataQuery(null, DataRef.of(specificFlow, newDataQuery(Key.ALL, SERIES_KEYS_ONLY)), ignoreDsdRef));
            assertThatNullPointerException().isThrownBy(() -> x.getDataQuery(base, null, ignoreDsdRef));
            assertThatNullPointerException().isThrownBy(() -> x.getDataQuery(base, DataRef.of(specificFlow, newDataQuery(Key.ALL, SERIES_KEYS_ONLY)), null));
        }
    }

    @Test
    public void testGetFlowsQuery() {
        Assertions.assertThat(Sdmx21RestQueries.builder().build())
                .satisfies(x -> assertThat(x.getFlowsQuery(base))
                        .hasToString("http://base/dataflow/all/all/latest"));

        Assertions.assertThat(Sdmx21RestQueries.builder().trailingSlashRequired(true).build().getFlowsQuery(base))
                .hasToString("http://base/dataflow/all/all/latest/");
    }

    @Test
    public void testGetFlowQuery() {
        Assertions.assertThat(Sdmx21RestQueries.builder().build())
                .satisfies(x -> {
                    assertThat(x.getFlowQuery(base, specificFlow))
                            .hasToString("http://base/dataflow/ECB/EXR/1.0");

                    assertThat(x.getFlowQuery(base, genericFlow))
                            .hasToString("http://base/dataflow/all/EXR/latest");
                });

        Assertions.assertThat(Sdmx21RestQueries.builder().trailingSlashRequired(true).build().getFlowQuery(base, specificFlow))
                .hasToString("http://base/dataflow/ECB/EXR/1.0/");
    }

    @Test
    public void testGetStructureQuery() {
        Assertions.assertThat(Sdmx21RestQueries.builder().build())
                .satisfies(x -> {
                    assertThat(x.getStructureQuery(base, specificStruct))
                            .hasToString("http://base/datastructure/ECB/EXR/1.0?references=children");

                    assertThat(x.getStructureQuery(base, genericStruct))
                            .hasToString("http://base/datastructure/all/EXR/latest?references=children");
                });

        Assertions.assertThat(Sdmx21RestQueries.builder().trailingSlashRequired(true).build().getStructureQuery(base, specificStruct))
                .hasToString("http://base/datastructure/ECB/EXR/1.0/?references=children");
    }

    @Test
    public void testGetDataQuery() {
        Assertions.assertThat(Sdmx21RestQueries.builder().build())
                .satisfies(x -> {
                    assertThat(x.getDataQuery(base, DataRef.of(specificFlow, newDataQuery(Key.ALL, FULL)), ignoreDsdRef))
                            .hasToString("http://base/data/ECB%2CEXR%2C1.0/all/all");

                    assertThat(x.getDataQuery(base, DataRef.of(specificFlow, newDataQuery(Key.ALL, SERIES_KEYS_ONLY)), ignoreDsdRef))
                            .hasToString("http://base/data/ECB%2CEXR%2C1.0/all/all?detail=serieskeysonly");

                    assertThat(x.getDataQuery(base, DataRef.of(genericFlow, newDataQuery(Key.ALL, SERIES_KEYS_ONLY)), ignoreDsdRef))
                            .hasToString("http://base/data/all%2CEXR%2Clatest/all/all?detail=serieskeysonly");

                    assertThat(x.getDataQuery(base, DataRef.of(specificFlow, newDataQuery(key, SERIES_KEYS_ONLY)), ignoreDsdRef))
                            .hasToString("http://base/data/ECB%2CEXR%2C1.0/D.NOK.EUR.SP00.A/all?detail=serieskeysonly");

                    assertThat(x.getDataQuery(base, DataRef.of(genericFlow, newDataQuery(key, SERIES_KEYS_ONLY)), ignoreDsdRef))
                            .hasToString("http://base/data/all%2CEXR%2Clatest/D.NOK.EUR.SP00.A/all?detail=serieskeysonly");
                });

        Assertions.assertThat(Sdmx21RestQueries.builder().trailingSlashRequired(true).build().getDataQuery(base, DataRef.of(specificFlow, newDataQuery(Key.ALL, FULL)), ignoreDsdRef))
                .hasToString("http://base/data/ECB%2CEXR%2C1.0/all/all/");
    }

    private final URL base = Parser.onURL().parse("http://base");
    private final DataflowRef specificFlow = DataflowRef.of("ECB", "EXR", "1.0");
    private final DataflowRef genericFlow = DataflowRef.of(null, "EXR", null);
    private final DataStructureRef specificStruct = DataStructureRef.of("ECB", "EXR", "1.0");
    private final DataStructureRef genericStruct = DataStructureRef.of(null, "EXR", null);
    private final Key key = Key.parse("D.NOK.EUR.SP00.A");
    private final DataStructureRef ignoreDsdRef = DataStructureRef.parse("abc");

    private static DataQuery newDataQuery(Key key, DataDetail detail) {
        return DataQuery.builder().key(key).detail(detail).build();
    }
}
