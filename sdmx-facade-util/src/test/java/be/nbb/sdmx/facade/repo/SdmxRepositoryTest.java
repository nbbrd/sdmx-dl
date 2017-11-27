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

import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.Obs;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.tck.ConnectionAssert;
import be.nbb.sdmx.facade.tck.DataCursorAssert;
import be.nbb.sdmx.facade.util.SeriesSupport;
import java.time.LocalDateTime;
import java.util.Collections;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SdmxRepositoryTest {

    @Test
    public void testCompliance() {
        ConnectionAssert.assertCompliance(repo::asConnection, goodFlowRef);
    }

    @Test
    public void testDataCursorCompliance() {
        DataCursorAssert.assertCompliance(() -> SeriesSupport.asCursor(Collections.singletonList(series), Key.ALL));
    }

    @Test
    public void testBuilder() {
        assertThat(SdmxRepository.builder().name("test").data(goodFlowRef, series).build().isSeriesKeysOnlySupported()).isTrue();
    }

    @Test
    @SuppressWarnings("null")
    public void testGetCursor() {
        assertThatNullPointerException().isThrownBy(() -> repo.getCursor(null, all));
        assertThatNullPointerException().isThrownBy(() -> repo.getCursor(goodFlowRef, null));

        DataCursorAssert.assertCompliance(() -> repo.getCursor(goodFlowRef, all).get());

        assertThat(repo.getCursor(goodFlowRef, all)).isNotEmpty();
        assertThat(repo.getCursor(badFlowRef, all)).isEmpty();
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
    public void testGetDataStructures() {
        assertThat(repo.getDataStructures()).containsExactly(struct);
    }

    @Test
    public void testGetDataflows() {
        assertThat(repo.getDataflows()).containsExactly(flow);
    }

    @Test
    @SuppressWarnings("null")
    public void testGetStream() {
        assertThatNullPointerException().isThrownBy(() -> repo.getStream(null, all));
        assertThatNullPointerException().isThrownBy(() -> repo.getStream(goodFlowRef, null));
        assertThat(repo.getStream(goodFlowRef, all)).isNotEmpty();
        assertThat(repo.getStream(badFlowRef, all)).isEmpty();
    }

    @Test
    @SuppressWarnings("null")
    public void testGetStructure() {
        assertThatNullPointerException().isThrownBy(() -> repo.getStructure(null));
        assertThat(repo.getStructure(goodStructRef)).isNotEmpty();
        assertThat(repo.getStructure(badStructRef)).isEmpty();
    }

    private final DataStructureRef goodStructRef = DataStructureRef.of("NBB","goodStruct", "v1.0");
    private final DataStructureRef badStructRef = DataStructureRef.parse("badStruct");
    private final DataQuery all = DataQuery.of(Key.ALL, false);
    private final DataflowRef goodFlowRef = DataflowRef.of("NBB", "XYZ", "v2.0");
    private final DataflowRef badFlowRef = DataflowRef.parse("other");
    private final Dataflow flow = Dataflow.of(goodFlowRef, goodStructRef, "flow1");
    private final DataStructure struct = DataStructure.builder().ref(goodStructRef).label("struct1").build();
    private final Series series = Series.builder().key(Key.of("BE")).freq(Frequency.MONTHLY).obs(Obs.of(LocalDateTime.now(), Math.PI)).meta("hello", "world").build();
    private final SdmxRepository repo = SdmxRepository
            .builder()
            .name("test")
            .dataStructure(struct)
            .dataflow(flow)
            .data(goodFlowRef, series)
            .build();
}
