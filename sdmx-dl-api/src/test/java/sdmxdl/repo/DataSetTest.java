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
package sdmxdl.repo;

import org.junit.Test;
import sdmxdl.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * @author Philippe Charles
 */
public class DataSetTest {

    @Test
    public void testBuilderCopyOf() {
        assertThatNullPointerException()
                .isThrownBy(() -> DataSet.builder().copyOf(null));
    }

    @Test
    public void testGetData() {
        assertThatNullPointerException()
                .isThrownBy(() -> dataSet.getData(null, DataFilter.FULL));

        assertThatNullPointerException()
                .isThrownBy(() -> dataSet.getData(Key.ALL, null));

        assertThat(dataSet.getData(Key.ALL, DataFilter.FULL))
                .containsExactly(series);
    }

    @Test
    public void testGetDataStream() {
        assertThatNullPointerException()
                .isThrownBy(() -> dataSet.getDataStream(null, DataFilter.FULL));

        assertThatNullPointerException()
                .isThrownBy(() -> dataSet.getDataStream(Key.ALL, null));

        assertThat(dataSet.getDataStream(Key.ALL, DataFilter.FULL))
                .containsExactly(series);
    }

    @Test
    public void testGetDataCursor() {
        assertThatNullPointerException()
                .isThrownBy(() -> dataSet.getDataCursor(null, DataFilter.FULL));

        assertThatNullPointerException()
                .isThrownBy(() -> dataSet.getDataCursor(Key.ALL, null));

        assertThat(dataSet.getDataCursor(Key.ALL, DataFilter.FULL))
                .isNotNull();
    }

    private final DataStructureRef goodStructRef = DataStructureRef.of("NBB", "goodStruct", "v1.0");
    private final DataStructureRef badStructRef = DataStructureRef.parse("badStruct");
    private final DataflowRef goodFlowRef = DataflowRef.of("NBB", "XYZ", "v2.0");
    private final DataflowRef badFlowRef = DataflowRef.parse("other");
    private final Dataflow flow = Dataflow.of(goodFlowRef, goodStructRef, "flow1");
    private final DataStructure struct = DataStructure.builder().ref(goodStructRef).primaryMeasureId("").label("struct1").build();
    private final Series series = Series.builder().key(Key.of("BE")).freq(Frequency.MONTHLY).obs(Obs.of(LocalDateTime.now(), Math.PI)).meta("hello", "world").build();
    private final DataSet dataSet = DataSet.builder().ref(goodFlowRef).series(series).build();
}
