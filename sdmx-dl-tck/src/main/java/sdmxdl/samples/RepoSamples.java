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

    public final Obs OBS1 = Obs.builder().period(LocalDate.of(2010, 1, 1).atStartOfDay()).value(Math.PI).build();

    public final Obs OBS2 = Obs.builder().period(LocalDate.of(2010, 2, 1).atStartOfDay()).value(Math.E).build();

    public final Key K1 = Key.of("M", "BE", "INDUSTRY");
    public final Key INVALID_KEY = Key.of("M", "BE");

    public final Series S1 = Series
            .builder()
            .key(K1)
            .freq(Frequency.MONTHLY)
            .obs(OBS1)
            .obs(OBS2)
            .meta("TITLE", "hello world")
            .build();

    public final DataSet DATA_SET = DataSet.builder().ref(GOOD_FLOW_REF).series(S1).build();

    public final SdmxRepository REPO = SdmxRepository
            .builder()
            .name("test")
            .structure(STRUCT)
            .flow(FLOW)
            .dataSet(DATA_SET)
            .build();
}
