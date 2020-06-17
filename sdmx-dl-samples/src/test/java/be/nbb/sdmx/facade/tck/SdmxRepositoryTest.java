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
package be.nbb.sdmx.facade.tck;

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

import be.nbb.sdmx.facade.repo.SdmxRepository;
import be.nbb.sdmx.facade.tck.DataCursorAssert;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SdmxRepositoryTest {

    @Test
    @SuppressWarnings("null")
    public void testGetCursor() {
        DataCursorAssert.assertCompliance(() -> repo.getDataCursor(goodFlowRef, Key.ALL, DataFilter.ALL).get());
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
