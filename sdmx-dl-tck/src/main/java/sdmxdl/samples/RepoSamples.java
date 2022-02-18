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
import sdmxdl.DataSet;
import sdmxdl.repo.SdmxRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class RepoSamples {

    public static final DataStructureRef BAD_STRUCT_REF = DataStructureRef.parse("badStruct");
    public static final DataflowRef BAD_FLOW_REF = DataflowRef.parse("other");
    public static final CodelistRef BAD_CODELIST_REF = CodelistRef.parse("badCodelist");

    public static final DataStructureRef STRUCT_REF = DataStructureRef.of("NBB", "goodStruct", "v1.0");
    public static final DataflowRef FLOW_REF = DataflowRef.of("NBB", "XYZ", "v2.0");

    public static final Dataflow FLOW = Dataflow.of(FLOW_REF, STRUCT_REF, "flow1");

    public static final CodelistRef CL_REF1 = CodelistRef.parse("CL_FREQ");
    public static final CodelistRef CL_REF2 = CodelistRef.parse("CL_REGION");
    public static final CodelistRef CL_REF3 = CodelistRef.parse("CL_SECTOR");
    public static final CodelistRef CL_REF4 = CodelistRef.parse("CL_OBS_STATUS");

    public static final Codelist CL1 = Codelist.builder().ref(CL_REF1).code("M", "Monthly").build();
    public static final Codelist CL2 = Codelist.builder().ref(CL_REF2).code("BE", "Belgium").code("FR", "France").build();
    public static final Codelist CL3 = Codelist.builder().ref(CL_REF3).code("INDUSTRY", "Industry").code("XXX", "Other").build();
    public static final Codelist CL4 = Codelist.builder().ref(CL_REF4).code("A", "Normal value").build();

    public static final Dimension DIM1 = Dimension.builder().id("FREQ").codelist(CL1).label("Frequency").position(1).build();
    public static final Dimension DIM2 = Dimension.builder().id("REGION").codelist(CL2).label("Region").position(3).build();
    public static final Dimension DIM3 = Dimension.builder().id("SECTOR").codelist(CL3).label("Sector").position(4).build();

    public static final Attribute NOT_CODED_ATTRIBUTE = Attribute.builder().id("TITLE").label("Title").build();
    public static final Attribute CODED_ATTRIBUTE = Attribute.builder().id("OBS_STATUS").codelist(CL4).label("Observation status").build();

    public static final DataStructure STRUCT = DataStructure
            .builder()
            .ref(STRUCT_REF)
            .dimension(DIM1)
            .dimension(DIM2)
            .dimension(DIM3)
            .attribute(NOT_CODED_ATTRIBUTE)
            .attribute(CODED_ATTRIBUTE)
            .timeDimensionId("TIME")
            .primaryMeasureId("OBS_VALUE")
            .label("structName")
            .build();

    public static final Obs OBS1 = Obs.builder().period(dateTimeOf(2010, 1)).value(Math.PI).build();
    public static final Obs OBS2 = Obs.builder().period(dateTimeOf(2010, 2)).value(Math.E).build();

    public static final Key K1 = Key.of("M", "BE", "INDUSTRY");
    public static final Key K2 = Key.of("M", "BE", "XXX");
    public static final Key K3 = Key.of("M", "FR", "INDUSTRY");
    public static final Key INVALID_KEY = Key.of("M", "BE");

    public static final Series S1 = Series
            .builder()
            .key(K1)
            .freq(Frequency.MONTHLY)
            .obs(OBS1)
            .obs(OBS2)
            .meta(NOT_CODED_ATTRIBUTE.getId(), "hello world")
            .build();

    public static final Series S2 = Series
            .builder()
            .key(K2)
            .freq(Frequency.MONTHLY)
            .build();

    public static final Series S3 = Series
            .builder()
            .key(K3)
            .freq(Frequency.MONTHLY)
            .obs(OBS1)
            .build();

    public static final DataSet DATA_SET = DataSet
            .builder()
            .ref(DataRef.of(FLOW_REF))
            .series(S1)
            .series(S2)
            .series(S3)
            .build();

    public static final SdmxRepository REPO = SdmxRepository
            .builder()
            .name("repoName")
            .structure(STRUCT)
            .flow(FLOW)
            .dataSet(DATA_SET)
            .build();

    private static LocalDateTime dateTimeOf(int year, int month) {
        return LocalDate.of(year, month, 1).atStartOfDay();
    }
}
