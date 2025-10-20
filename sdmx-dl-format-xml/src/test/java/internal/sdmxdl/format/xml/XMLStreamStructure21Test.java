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
package internal.sdmxdl.format.xml;

import nbbrd.io.xml.Xml;
import org.junit.jupiter.api.Test;
import sdmxdl.*;
import sdmxdl.format.xml.SdmxXmlStreams;
import tests.sdmxdl.format.xml.SdmxXmlSources;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nbbrd.io.text.TextResource.newBufferedReader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

/**
 * @author Philippe Charles
 */
public class XMLStreamStructure21Test {

    @Test
    @SuppressWarnings("null")
    public void test() throws Exception {
        Xml.Parser<List<Structure>> parser = SdmxXmlStreams.struct21(Languages.ANY);

        assertThat(parser.parseReader(SdmxXmlSources.ECB_DATA_STRUCTURE::openReader)).singleElement().satisfies(o -> {
            assertThat(o.getName()).isEqualTo("AMECO");
            assertThat(o.getPrimaryMeasureId()).isEqualTo("OBS_VALUE");
            assertThat(o.getTimeDimensionId()).isEqualTo("TIME_PERIOD");
            assertThat(o.getRef()).isEqualTo(StructureRef.of("ECB", "ECB_AME1", "1.0"));
            assertThat(o.getDimensions()).hasSize(7).element(0).satisfies(x -> {
                assertThat(x.getId()).isEqualTo("FREQ");
                assertThat(x.getName()).isEqualTo("Frequency");
                assertThat(x.getPosition()).isEqualTo(1);
                assertThat(x.getCodelist().getRef()).isEqualTo(CodelistRef.of("ECB", "CL_FREQ", "1.0"));
                assertThat(x.getCodelist().getCodes()).hasSize(10).containsEntry("M", "Monthly");
            });

            Set<Attribute> attributes = o.getAttributes();
            assertThat(attributes).hasSize(11);
            assertThat(attributes).filteredOn(Attribute::getId, "TIME_FORMAT").singleElement().satisfies(x -> {
                assertThat(x.getName()).isEqualTo("Time format code");
                assertThat(x.getRelationship()).isEqualTo(AttributeRelationship.SERIES);
                assertThat(x.getCodes()).isEmpty();
            });
            assertThat(attributes).filteredOn(Attribute::getId, "OBS_STATUS").singleElement().satisfies(x -> {
                assertThat(x.getName()).isEqualTo("Observation status");
                assertThat(x.getRelationship()).isEqualTo(AttributeRelationship.OBSERVATION);
                assertThat(x.getCodes()).isNotEmpty();
            });
        });

        assertThatIOException()
                .isThrownBy(() -> parser.parseReader(SdmxXmlSources.NBB_DATA_STRUCTURE::openReader))
                .withCauseInstanceOf(XMLStreamException.class)
                .withMessageContaining("Invalid namespace");
    }

    @Test
    public void testCoreRepresentation() throws IOException {
        Xml.Parser<List<Structure>> parser = SdmxXmlStreams.struct21(Languages.ANY);

        assertThat(parser.parseReader(() -> newBufferedReader(SdmxXmlSources.class, "other/CoreRepresentation21.xml", UTF_8))).singleElement().satisfies(o -> {
            assertThat(o.getName()).isEqualTo("Asia and Pacific Regional Economic Outlook (APDREO)");
            assertThat(o.getPrimaryMeasureId()).isEqualTo("OBS_VALUE");
            assertThat(o.getTimeDimensionId()).isEqualTo("TIME_PERIOD");
            assertThat(o.getRef()).isEqualTo(StructureRef.of("IMF.APD", "DSD_APDREO", "6.0.0"));
            assertThat(o.getDimensions()).hasSize(3).element(0).satisfies(x -> {
                assertThat(x.getId()).isEqualTo("COUNTRY");
                assertThat(x.getName()).isEqualTo("Country");
                assertThat(x.getPosition()).isEqualTo(0);
                assertThat(x.getCodelist().getRef()).isEqualTo(CodelistRef.of("IMF.APD", "CL_APDREO_COUNTRY", "3.0.0"));
                assertThat(x.getCodelist().getCodes()).hasSize(340).containsEntry("BEL", "Belgium");
            });
        });
    }
}
