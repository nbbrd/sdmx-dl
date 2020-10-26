/*
 * Copyright 2017 National Bank of Belgium
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

import org.assertj.core.api.Assertions;
import org.junit.Test;
import sdmxdl.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * @author Philippe Charles
 */
public class SdmxRepositoryTest {

    @Test
    public void testBuilder() {
        assertThat(SdmxRepository.builder().name("test").dataSet(dataSet).build().isSeriesKeysOnlySupported()).isTrue();
    }

    @Test
    public void testGetData() {
        Assertions.assertThat(repo.getDataSets()).hasSize(1).contains(dataSet);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetFlow() {
        assertThatNullPointerException().isThrownBy(() -> repo.getFlow(null));
        assertThat(repo.getFlow(goodFlowRef)).isNotEmpty();
        assertThat(repo.getFlow(badFlowRef)).isEmpty();
        assertThat(repo.getFlow(DataflowRef.of(null, "XYZ", null))).isNotEmpty();
    }

    @Test
    public void testGetStructures() {
        Assertions.assertThat(repo.getStructures()).containsExactly(struct);
    }

    @Test
    public void testGetFlows() {
        assertThat(repo.getFlows()).containsExactly(flow);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetStructure() {
        assertThatNullPointerException().isThrownBy(() -> repo.getStructure(null));
        Assertions.assertThat(repo.getStructure(goodStructRef)).isNotEmpty();
        Assertions.assertThat(repo.getStructure(badStructRef)).isEmpty();
    }

    private final DataStructureRef goodStructRef = DataStructureRef.of("NBB", "goodStruct", "v1.0");
    private final DataStructureRef badStructRef = DataStructureRef.parse("badStruct");
    private final DataflowRef goodFlowRef = DataflowRef.of("NBB", "XYZ", "v2.0");
    private final DataflowRef badFlowRef = DataflowRef.parse("other");
    private final Dataflow flow = Dataflow.of(goodFlowRef, goodStructRef, "flow1");
    private final DataStructure struct = DataStructure.builder().ref(goodStructRef).primaryMeasureId("").label("struct1").build();
    private final Series series = Series.builder().key(Key.of("BE")).freq(Frequency.MONTHLY).obs(Obs.of(LocalDateTime.now(), Math.PI)).meta("hello", "world").build();
    private final DataSet dataSet = DataSet.builder().ref(goodFlowRef).series(series).build();
    private final SdmxRepository repo = SdmxRepository
            .builder()
            .name("test")
            .structure(struct)
            .flow(flow)
            .dataSet(dataSet)
            .build();
}
