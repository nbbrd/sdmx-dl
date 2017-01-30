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
package be.nbb.sdmx.facade.file.impl;

import be.nbb.sdmx.facade.DataStructure;
import java.io.IOException;
import javax.xml.stream.XMLInputFactory;
import org.junit.Test;
import be.nbb.sdmx.facade.file.SdmxDecoder;
import be.nbb.sdmx.facade.file.TestResources;
import static be.nbb.sdmx.facade.file.impl.CustomDataStructureBuilder.ref;
import static org.assertj.core.api.Assertions.assertThat;
import static be.nbb.sdmx.facade.file.impl.CustomDataStructureBuilder.dimension;
import java.io.File;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public class XMLStreamSdmxDecoderTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
    private final XMLStreamSdmxDecoder decoder = new XMLStreamSdmxDecoder(XMLInputFactory.newInstance());

    @Test
    public void testDecodeGeneric20() throws IOException {
        File generic20 = TestResources.GENERIC20_FILE.copyTo(temp);

        DataStructure ds = DataStructure.builder()
                .ref(ref("BIS_JOINT_DEBT"))
                .dimension(dimension("FREQ", 1, "A", "M"))
                .dimension(dimension("JD_TYPE", 2, "P"))
                .dimension(dimension("JD_CATEGORY", 3, "A"))
                .dimension(dimension("VIS_CTY", 4, "MX"))
                .name("BIS_JOINT_DEBT")
                .timeDimensionId("TIME_FORMAT")
                .primaryMeasureId("OBS_VALUE")
                .build();

        assertThat(decoder.decode(generic20)).isEqualTo(SdmxDecoder.Info.of(SdmxDecoder.FileType.GENERIC20, ds));
    }

    @Test
    public void testDecodeCompact20() throws IOException {
        File compact20 = TestResources.COMPACT20_FILE.copyTo(temp);

        DataStructure ds = DataStructure.builder()
                .ref(ref("TODO"))
                .dimension(dimension("FREQ", 1, "A", "M"))
                .dimension(dimension("COLLECTION", 2, "B"))
                .dimension(dimension("VIS_CTY", 3, "MX"))
                .dimension(dimension("JD_TYPE", 4, "P"))
                .dimension(dimension("JD_CATEGORY", 5, "A", "B"))
                .name("TODO")
                .timeDimensionId("TIME_FORMAT")
                .primaryMeasureId("OBS_VALUE")
                .build();

        assertThat(decoder.decode(compact20)).isEqualTo(SdmxDecoder.Info.of(SdmxDecoder.FileType.COMPACT20, ds));
    }

    @Test
    public void testDecodeGeneric21() throws IOException {
        File generic21 = TestResources.GENERIC21_FILE.copyTo(temp);

        DataStructure ds = DataStructure.builder()
                .ref(ref("ECB_AME1"))
                .dimension(dimension("FREQ", 1, "A"))
                .dimension(dimension("AME_REF_AREA", 2, "BEL"))
                .dimension(dimension("AME_TRANSFORMATION", 3, "1"))
                .dimension(dimension("AME_AGG_METHOD", 4, "0"))
                .dimension(dimension("AME_UNIT", 5, "0"))
                .dimension(dimension("AME_REFERENCE", 6, "0"))
                .dimension(dimension("AME_ITEM", 7, "OVGD"))
                .name("ECB_AME1")
                .timeDimensionId("TIME_PERIOD")
                .primaryMeasureId("OBS_VALUE")
                .build();

        assertThat(decoder.decode(generic21)).isEqualTo(SdmxDecoder.Info.of(SdmxDecoder.FileType.GENERIC21, ds));
    }

    @Test
    public void testDecodeCompact21() throws IOException {
        File compact21 = TestResources.COMPACT21_FILE.copyTo(temp);

        DataStructure ds = DataStructure.builder()
                .ref(ref("ECB_AME1"))
                .dimension(dimension("FREQ", 1, "A"))
                .dimension(dimension("AME_REF_AREA", 2, "BEL"))
                .dimension(dimension("AME_TRANSFORMATION", 3, "1"))
                .dimension(dimension("AME_AGG_METHOD", 4, "0"))
                .dimension(dimension("AME_UNIT", 5, "0"))
                .dimension(dimension("AME_REFERENCE", 6, "0"))
                .dimension(dimension("AME_ITEM", 7, "OVGD"))
                .name("ECB_AME1")
                .timeDimensionId("TIME_PERIOD")
                .primaryMeasureId("OBS_VALUE")
                .build();

        assertThat(decoder.decode(compact21)).isEqualTo(SdmxDecoder.Info.of(SdmxDecoder.FileType.COMPACT21, ds));
    }
}
