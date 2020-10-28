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
package sdmxdl.samples;

import sdmxdl.*;
import sdmxdl.repo.DataSet;
import sdmxdl.repo.SdmxRepository;

import java.time.LocalDate;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class RepoSamples {

    public final DataStructureRef GOOD_STRUCT_REF = DataStructureRef.of("NBB", "goodStruct", "v1.0");
    public final DataStructureRef BAD_STRUCT_REF = DataStructureRef.parse("badStruct");
    public final DataflowRef GOOD_FLOW_REF = DataflowRef.of("NBB", "XYZ", "v2.0");
    public final DataflowRef BAD_FLOW_REF = DataflowRef.parse("other");

    public final Dataflow FLOW = Dataflow.of(GOOD_FLOW_REF, GOOD_STRUCT_REF, "flow1");

    public final DataStructure STRUCT = DataStructure
            .builder()
            .ref(GOOD_STRUCT_REF)
            .dimension(Dimension.builder().id("FREQ").label("Frequency").position(1).build())
            .dimension(Dimension.builder().id("REGION").label("Region").position(2).build())
            .dimension(Dimension.builder().id("SECTOR").label("Sector").position(3).build())
            .attribute(Attribute.builder().id("TITLE").label("Title").build())
            .timeDimensionId("TIME")
            .primaryMeasureId("")
            .label("struct1")
            .build();

    public final Series SERIES = Series
            .builder()
            .key(Key.of("M", "BE", "INDUSTRY"))
            .freq(Frequency.MONTHLY)
            .obs(Obs.of(LocalDate.of(2010, 1, 1).atStartOfDay(), Math.PI))
            .obs(Obs.of(LocalDate.of(2010, 2, 1).atStartOfDay(), Math.E))
            .meta("TITLE", "hello world")
            .build();

    public final DataSet DATA_SET = DataSet.builder().ref(GOOD_FLOW_REF).series(SERIES).build();

    public final SdmxRepository REPO = SdmxRepository
            .builder()
            .name("test")
            .structure(STRUCT)
            .flow(FLOW)
            .dataSet(DATA_SET)
            .build();
}
