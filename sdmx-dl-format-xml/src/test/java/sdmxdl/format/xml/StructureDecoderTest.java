/*
 * Copyright 2015 National Bank of Belgium
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
package sdmxdl.format.xml;

import org.junit.jupiter.api.Test;
import sdmxdl.Structure;
import sdmxdl.StructureRef;
import tests.sdmxdl.format.xml.SdmxXmlSources;

import java.io.IOException;

import static internal.sdmxdl.format.xml.CustomDataStructureBuilder.dimension;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class StructureDecoderTest {

    @Test
    public void testDecodeGeneric20() throws IOException {
        Structure ds = Structure.builder()
                .ref(StructureRef.of(null, "BIS_JOINT_DEBT", null))
                .dimension(dimension("FREQ", "A", "M"))
                .dimension(dimension("JD_TYPE", "P"))
                .dimension(dimension("JD_CATEGORY", "A"))
                .dimension(dimension("VIS_CTY", "MX"))
                .name("BIS_JOINT_DEBT")
                .timeDimensionId("TIME_PERIOD")
                .primaryMeasureId("OBS_VALUE")
                .build();

        assertThat(DataStructureDecoder.generic20().parseReader(SdmxXmlSources.OTHER_GENERIC20::openReader)).isEqualTo(ds);
    }

    @Test
    public void testDecodeCompact20() throws IOException {
        Structure ds = Structure.builder()
                .ref(StructureRef.of(null, "UNKNOWN", null))
                .dimension(dimension("FREQ", "A", "M"))
                .dimension(dimension("COLLECTION", "B"))
                .dimension(dimension("VIS_CTY", "MX"))
                .dimension(dimension("JD_TYPE", "P"))
                .dimension(dimension("JD_CATEGORY", "A", "B"))
                .name("UNKNOWN")
                .timeDimensionId("TIME_PERIOD")
                .primaryMeasureId("OBS_VALUE")
                .build();

        assertThat(DataStructureDecoder.compact20().parseReader(SdmxXmlSources.OTHER_COMPACT20::openReader)).isEqualTo(ds);
    }

    @Test
    public void testDecodeGeneric21() throws IOException {
        Structure ds = Structure.builder()
                .ref(StructureRef.of(null, "ECB_AME1", null))
                .dimension(dimension("FREQ", "A"))
                .dimension(dimension("AME_REF_AREA", "BEL"))
                .dimension(dimension("AME_TRANSFORMATION", "1"))
                .dimension(dimension("AME_AGG_METHOD", "0"))
                .dimension(dimension("AME_UNIT", "0"))
                .dimension(dimension("AME_REFERENCE", "0"))
                .dimension(dimension("AME_ITEM", "OVGD"))
                .name("ECB_AME1")
                .timeDimensionId("TIME_PERIOD")
                .primaryMeasureId("OBS_VALUE")
                .build();

        assertThat(DataStructureDecoder.generic21().parseReader(SdmxXmlSources.OTHER_GENERIC21::openReader)).isEqualTo(ds);
    }

    @Test
    public void testDecodeCompact21() throws IOException {
        Structure ds = Structure.builder()
                .ref(StructureRef.of(null, "ECB_AME1", null))
                .dimension(dimension("FREQ", "A"))
                .dimension(dimension("AME_REF_AREA", "BEL"))
                .dimension(dimension("AME_TRANSFORMATION", "1"))
                .dimension(dimension("AME_AGG_METHOD", "0"))
                .dimension(dimension("AME_UNIT", "0"))
                .dimension(dimension("AME_REFERENCE", "0"))
                .dimension(dimension("AME_ITEM", "OVGD"))
                .name("ECB_AME1")
                .timeDimensionId("TIME_PERIOD")
                .primaryMeasureId("OBS_VALUE")
                .build();

        assertThat(DataStructureDecoder.compact21().parseReader(SdmxXmlSources.OTHER_COMPACT21::openReader)).isEqualTo(ds);
    }
}
