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
package be.nbb.sdmx.facade.repo;

import be.nbb.sdmx.facade.DataFilter;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.Obs;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.Frequency;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SdmxRepositoryTest {

    @Test
    public void testBuilder() {
        assertThat(SdmxRepository.builder().name("test").data(goodFlowRef, series).build().isSeriesKeysOnlySupported()).isTrue();
    }

    @Test
    @SuppressWarnings("null")
    public void testGetCursor() {
        assertThatNullPointerException().isThrownBy(() -> repo.getDataCursor(null, Key.ALL, DataFilter.ALL));
        assertThatNullPointerException().isThrownBy(() -> repo.getDataCursor(goodFlowRef, null, DataFilter.ALL));
        assertThatNullPointerException().isThrownBy(() -> repo.getDataCursor(goodFlowRef, Key.ALL, null));

        assertThat(repo.getDataCursor(goodFlowRef, Key.ALL, DataFilter.ALL)).isNotEmpty();
        assertThat(repo.getDataCursor(badFlowRef, Key.ALL, DataFilter.ALL)).isEmpty();
    }

    @Test
    public void testGetData() {
        assertThat(repo.getData()).hasSize(1).containsKey(goodFlowRef);
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
        assertThat(repo.getStructures()).containsExactly(struct);
    }

    @Test
    public void testGetFlows() {
        assertThat(repo.getFlows()).containsExactly(flow);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetStream() {
        assertThatNullPointerException().isThrownBy(() -> repo.getDataStream(null, Key.ALL, DataFilter.ALL));
        assertThatNullPointerException().isThrownBy(() -> repo.getDataStream(goodFlowRef, null, DataFilter.ALL));
        assertThatNullPointerException().isThrownBy(() -> repo.getDataStream(goodFlowRef, Key.ALL, null));
        assertThat(repo.getDataStream(goodFlowRef, Key.ALL, DataFilter.ALL)).isNotEmpty();
        assertThat(repo.getDataStream(badFlowRef, Key.ALL, DataFilter.ALL)).isEmpty();
    }

    @Test
    @SuppressWarnings("null")
    public void testGetStructure() {
        assertThatNullPointerException().isThrownBy(() -> repo.getStructure(null));
        assertThat(repo.getStructure(goodStructRef)).isNotEmpty();
        assertThat(repo.getStructure(badStructRef)).isEmpty();
    }

    private final DataStructureRef goodStructRef = DataStructureRef.of("NBB", "goodStruct", "v1.0");
    private final DataStructureRef badStructRef = DataStructureRef.parse("badStruct");
    private final DataflowRef goodFlowRef = DataflowRef.of("NBB", "XYZ", "v2.0");
    private final DataflowRef badFlowRef = DataflowRef.parse("other");
    private final Dataflow flow = Dataflow.of(goodFlowRef, goodStructRef, "flow1");
    private final DataStructure struct = DataStructure.builder().ref(goodStructRef).label("struct1").build();
    private final Series series = Series.builder().key(Key.of("BE")).freq(Frequency.MONTHLY).obs(Obs.of(LocalDateTime.now(), Math.PI)).meta("hello", "world").build();
    private final SdmxRepository repo = SdmxRepository
            .builder()
            .name("test")
            .structure(struct)
            .flow(flow)
            .data(goodFlowRef, series)
            .build();
}
