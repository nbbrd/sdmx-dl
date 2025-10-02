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
package tests.sdmxdl.api;

import sdmxdl.*;
import sdmxdl.web.MonitorReport;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.MonitorStatus;
import sdmxdl.web.WebSource;

import java.time.LocalDate;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class RepoSamples {

    public static final StructureRef BAD_STRUCT_REF = StructureRef.parse("badStruct");
    public static final FlowRef BAD_FLOW_REF = FlowRef.parse("other");
    public static final CodelistRef BAD_CODELIST_REF = CodelistRef.parse("badCodelist");

    public static final StructureRef STRUCT_REF = StructureRef.of("NBB", "goodStruct", "v1.0");
    public static final FlowRef FLOW_REF = FlowRef.of("NBB", "XYZ", "v2.0");

    public static final Flow FLOW = Flow.builder().ref(FLOW_REF).structureRef(STRUCT_REF).name("flow1 name").description("flow1 description").build();

    public static final CodelistRef CL_REF1 = CodelistRef.parse("CL_FREQ");
    public static final CodelistRef CL_REF2 = CodelistRef.parse("CL_REGION");
    public static final CodelistRef CL_REF3 = CodelistRef.parse("CL_SECTOR");
    public static final CodelistRef CL_REF4 = CodelistRef.parse("CL_OBS_STATUS");

    public static final Codelist CL1 = Codelist.builder().ref(CL_REF1).code("M", "Monthly").build();
    public static final Codelist CL2 = Codelist.builder().ref(CL_REF2).code("BE", "Belgium").code("FR", "France").build();
    public static final Codelist CL3 = Codelist.builder().ref(CL_REF3).code("INDUSTRY", "Industry").code("XXX", "Other").build();
    public static final Codelist CL4 = Codelist.builder().ref(CL_REF4).code("A", "Normal value").build();

    public static final Dimension DIM1 = Dimension.builder().id("FREQ").codelist(CL1).name("Frequency").position(1).build();
    public static final Dimension DIM2 = Dimension.builder().id("REGION").codelist(CL2).name("Region").position(3).build();
    public static final Dimension DIM3 = Dimension.builder().id("SECTOR").codelist(CL3).name("Sector").position(4).build();

    public static final Attribute NOT_CODED_ATTRIBUTE = Attribute.builder().id("TITLE").name("Title").relationship(AttributeRelationship.SERIES).build();
    public static final Attribute CODED_ATTRIBUTE = Attribute.builder().id("OBS_STATUS").codelist(CL4).name("Observation status").relationship(AttributeRelationship.OBSERVATION).build();

    public static final Structure STRUCT = Structure
            .builder()
            .ref(STRUCT_REF)
            .dimension(DIM1)
            .dimension(DIM2)
            .dimension(DIM3)
            .attribute(NOT_CODED_ATTRIBUTE)
            .attribute(CODED_ATTRIBUTE)
            .timeDimensionId("TIME")
            .primaryMeasureId("OBS_VALUE")
            .name("structName")
            .build();

    public static final MetaSet META_SET = MetaSet
            .builder()
            .flow(FLOW)
            .structure(STRUCT)
            .build();

    public static final Obs OBS1 = Obs.builder().period(periodOf(2010, 1)).value(Math.PI).build();
    public static final Obs OBS2 = Obs.builder().period(periodOf(2010, 2)).value(Math.E).build();

    public static final Key K1 = Key.of("M", "BE", "INDUSTRY");
    public static final Key K2 = Key.of("M", "BE", "XXX");
    public static final Key K3 = Key.of("M", "FR", "INDUSTRY");
    public static final Key INVALID_KEY = Key.of("M", "BE");

    public static final Series S1 = Series
            .builder()
            .key(K1)
            .obs(OBS1)
            .obs(OBS2)
            .meta(NOT_CODED_ATTRIBUTE.getId(), "hello world")
            .build();

    public static final Series S2 = Series
            .builder()
            .key(K2)
            .build();

    public static final Series S3 = Series
            .builder()
            .key(K3)
            .obs(OBS1)
            .build();

    public static final DataSet DATA_SET = DataSet
            .builder()
            .ref(FLOW_REF)
            .series(S1)
            .series(S2)
            .series(S3)
            .build();

    public static final Database DATABASE = new Database(DatabaseRef.parse("IIF"), "Invest in Finland");

    public static final DataRepository EMPTY_REPO = DataRepository.builder().build();

    public static final DataRepository REPO = DataRepository
            .builder()
            .name("repoName")
            .database(DATABASE)
            .structure(STRUCT)
            .flow(FLOW)
            .dataSet(DATA_SET)
            .build();

    public static final MonitorReports EMPTY_REPORTS = MonitorReports.builder().uriScheme("abc").build();

    public static final MonitorReports REPORTS = MonitorReports
            .builder()
            .uriScheme("abc")
            .report(MonitorReport.builder().source("xyz").status(MonitorStatus.DOWN).uptimeRatio(0.5).averageResponseTime(1234L).build())
            .build();

    public static final WebSource BASIC_SOURCE = WebSource
            .builder()
            .id("ECB")
            .driver("ri:sdmx21")
            .endpointOf("https://data-api.ecb.europa.eu/service")
            .build();

    public static final WebSource FULL_SOURCE = WebSource
            .builder()
            .id("ECB")
            .name("en", "European Central Bank")
            .driver("ri:sdmx21")
            .confidentiality(Confidentiality.SECRET)
            .endpointOf("https://data-api.ecb.europa.eu/service")
            .property("detailSupported", "true")
            .alias("XYZ")
            .websiteOf("https://sdw.ecb.europa.eu")
            .monitorOf("ABC:xyz")
            .monitorWebsiteOf("https://someaddress")
            .build();

    private static TimeInterval periodOf(int year, int month) {
        return TimeInterval.of(LocalDate.of(year, month, 1).atStartOfDay(), Duration.parse("P1M"));
    }
}
