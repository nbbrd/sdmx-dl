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
package _test.samples;

import be.nbb.sdmx.facade.*;
import be.nbb.sdmx.facade.repo.SdmxRepository;

import java.time.LocalDateTime;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class FacadeResource {

    public final DataStructureRef goodStructRef = DataStructureRef.of("NBB", "goodStruct", "v1.0");
    public final DataStructureRef badStructRef = DataStructureRef.parse("badStruct");
    public final DataflowRef goodFlowRef = DataflowRef.of("NBB", "XYZ", "v2.0");
    public final DataflowRef badFlowRef = DataflowRef.parse("other");
    public final Dataflow flow = Dataflow.of(goodFlowRef, goodStructRef, "flow1");
    public final DataStructure struct = DataStructure.builder().ref(goodStructRef).label("struct1").build();
    public final Series series = Series.builder().key(Key.of("BE")).freq(Frequency.MONTHLY).obs(Obs.of(LocalDateTime.now(), Math.PI)).meta("hello", "world").build();
    public final SdmxRepository repo = SdmxRepository
            .builder()
            .name("test")
            .structure(struct)
            .flow(flow)
            .data(goodFlowRef, series)
            .build();
}
