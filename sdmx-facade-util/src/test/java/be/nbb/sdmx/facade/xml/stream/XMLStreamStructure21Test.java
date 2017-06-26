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
package be.nbb.sdmx.facade.xml.stream;

import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.samples.SdmxSource;
import java.io.InputStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class XMLStreamStructure21Test {

    @Test
    @SuppressWarnings("null")
    public void test() throws Exception {
        XMLInputFactory factory = XMLInputFactory.newFactory();

        XMLStreamStructure21 parser = new XMLStreamStructure21(LanguagePriorityList.ANY);

        try (InputStreamReader stream = SdmxSource.ECB_DATA_STRUCTURE.openReader()) {
            assertThat(parser.parse(factory.createXMLStreamReader(stream))).hasSize(1).element(0).satisfies(o -> {
                assertThat(o.getLabel()).isEqualTo("AMECO");
                assertThat(o.getPrimaryMeasureId()).isEqualTo("OBS_VALUE");
                assertThat(o.getTimeDimensionId()).isEqualTo("TIME_PERIOD");
                assertThat(o.getRef()).isEqualTo(DataStructureRef.of("ECB", "ECB_AME1", "1.0"));
                assertThat(o.getDimensions()).hasSize(7).element(0).satisfies(x -> {
                    assertThat(x.getId()).isEqualTo("FREQ");
                    assertThat(x.getLabel()).isEqualTo("Frequency");
                    assertThat(x.getPosition()).isEqualTo(1);
                });
            });
        }

        try (InputStreamReader stream = SdmxSource.NBB_DATA_STRUCTURE.openReader()) {
            assertThatThrownBy(() -> parser.parse(factory.createXMLStreamReader(stream)))
                    .isInstanceOf(XMLStreamException.class)
                    .hasMessageContaining("Invalid namespace")
                    .hasNoCause();
        }

        assertThatThrownBy(() -> parser.parse(null)).isInstanceOf(NullPointerException.class);
    }
}
