/*
 * Copyright 2020 National Bank of Belgium
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
package sdmxdl;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("ConstantConditions")
public class DataSetTest {

    @Test
    public void testToDataSet() {
        assertThatNullPointerException()
                .isThrownBy(() -> DataSet.toDataSet(null, Query.ALL));

        assertThatNullPointerException()
                .isThrownBy(() -> DataSet.toDataSet(FlowRef.parse(""), null));
    }

    @Test
    public void testGetData() {
        assertThatNullPointerException()
                .isThrownBy(() -> dataSet.getData(null));

        // Query.ALL is a no-op shortcut — returns same instance
        assertThat(dataSet.getData(Query.ALL)).isSameAs(dataSet);

        // Query with key filter returns only matching series
        Query keyQuery = Query.builder().key(Key.of("BE")).build();
        assertThat(dataSet.getData(keyQuery).getData()).containsExactly(series);

        // Query that matches nothing returns empty DataSet
        Query noMatchQuery = Query.builder().key(Key.of("XX")).build();
        assertThat(dataSet.getData(noMatchQuery).getData()).isEmpty();
    }

    @Test
    public void testGetDataStream() {
        assertThatNullPointerException()
                .isThrownBy(() -> dataSet.getDataStream(null));

        assertThat(dataSet.getDataStream(dataSet.getQuery()))
                .containsExactly(series);
    }

    private final StructureRef goodStructRef = StructureRef.of("NBB", "goodStruct", "v1.0");
    private final StructureRef badStructRef = StructureRef.parse("badStruct");
    private final FlowRef goodFlowRef = FlowRef.of("NBB", "XYZ", "v2.0");
    private final FlowRef badFlowRef = FlowRef.parse("other");
    private final Flow flow = Flow.builder().ref(goodFlowRef).structureRef(goodStructRef).name("flow1").build();
    private final Structure struct = Structure.builder().ref(goodStructRef).primaryMeasureId("").name("struct1").build();
    private final Obs obs1 = Obs.builder().period(TimeInterval.of(LocalDateTime.of(2010, 1, 2, 3, 4), Duration.parse("P1M"))).value(Math.PI).build();
    private final Series series = Series.builder().key(Key.of("BE")).obs(obs1).meta("hello", "world").build();
    private final DataSet dataSet = DataSet.builder().ref(goodFlowRef).series(series).build();
}
