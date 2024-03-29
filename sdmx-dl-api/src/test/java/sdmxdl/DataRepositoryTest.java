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
package sdmxdl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * @author Philippe Charles
 */
public class DataRepositoryTest {

    @Test
    public void testBuilder() {
        assertThat(DataRepository.builder().name("test").dataSet(dataSet).build()).isNotNull();
    }

    @Test
    public void testGetData() {
        Assertions.assertThat(repo.getDataSets()).singleElement().isEqualTo(dataSet);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetFlow() {
        assertThatNullPointerException().isThrownBy(() -> repo.getFlow(null));
        assertThat(repo.getFlow(goodFlowRef)).isNotEmpty();
        assertThat(repo.getFlow(badFlowRef)).isEmpty();
        assertThat(repo.getFlow(FlowRef.of(null, "XYZ", null))).isNotEmpty();
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

    private final StructureRef goodStructRef = StructureRef.of("NBB", "goodStruct", "v1.0");
    private final StructureRef badStructRef = StructureRef.parse("badStruct");
    private final FlowRef goodFlowRef = FlowRef.of("NBB", "XYZ", "v2.0");
    private final FlowRef badFlowRef = FlowRef.parse("other");
    private final Flow flow = Flow.builder().ref(goodFlowRef).structureRef(goodStructRef).name("flow1").build();
    private final Structure struct = Structure.builder().ref(goodStructRef).primaryMeasureId("").name("struct1").build();
    private final Obs obs1 = Obs.builder().period(TimeInterval.of(LocalDateTime.of(2010, 1, 2, 3, 4), Duration.parse("P1M"))).value(Math.PI).build();
    private final Series series = Series.builder().key(Key.of("BE")).obs(obs1).meta("hello", "world").build();
    private final DataSet dataSet = DataSet.builder().ref(goodFlowRef).series(series).build();
    private final DataRepository repo = DataRepository
            .builder()
            .name("test")
            .structure(struct)
            .flow(flow)
            .dataSet(dataSet)
            .build();
}
