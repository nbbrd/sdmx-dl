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
package sdmxdl.xml.stream;

import nbbrd.io.xml.Xml;
import org.junit.Test;
import sdmxdl.Attribute;
import sdmxdl.DataStructure;
import sdmxdl.DataStructureRef;
import sdmxdl.LanguagePriorityList;
import sdmxdl.samples.SdmxSource;

import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

/**
 *
 * @author Philippe Charles
 */
public class XMLStreamStructure20Test {

    @Test
    @SuppressWarnings("null")
    public void test() throws Exception {
        Xml.Parser<List<DataStructure>> p1 = SdmxXmlStreams.struct20(LanguagePriorityList.ANY);

        assertThat(p1.parseReader(SdmxSource.NBB_DATA_STRUCTURE::openReader)).hasSize(1).element(0).satisfies(o -> {
            assertThat(o.getLabel()).isEqualTo("My first dataset");
            assertThat(o.getPrimaryMeasureId()).isEqualTo("OBS_VALUE");
            assertThat(o.getTimeDimensionId()).isEqualTo("TIME");
            assertThat(o.getRef()).isEqualTo(DataStructureRef.of("NBB", "TEST_DATASET", null));
            assertThat(o.getDimensions()).hasSize(3).element(0).satisfies(x -> {
                assertThat(x.getId()).isEqualTo("SUBJECT");
                assertThat(x.getLabel()).isEqualTo("Subject");
                assertThat(x.getPosition()).isEqualTo(1);
            });

            Set<Attribute> attributes = o.getAttributes();
            assertThat(attributes).hasSize(2);
            assertThat(attributes).filteredOn(attr -> attr.getId().equals("TIME_FORMAT")).singleElement().satisfies(x -> {
                assertThat(x.getLabel()).isEqualTo("Time Format");
                assertThat(x.getCodes()).isNotEmpty();
            });
            assertThat(attributes).filteredOn(attr -> attr.getId().equals("OBS_STATUS")).singleElement().satisfies(x -> {
                assertThat(x.getLabel()).isEqualTo("Observation Status");
                assertThat(x.getCodes()).isNotEmpty();
            });
        });

        Xml.Parser<List<DataStructure>> p2 = SdmxXmlStreams.struct20(LanguagePriorityList.parse("fr"));

        assertThat(p2.parseReader(SdmxSource.NBB_DATA_STRUCTURE::openReader)).hasSize(1).element(0).satisfies(o -> {
            assertThat(o.getLabel()).isEqualTo("Mon premier dataset");
            assertThat(o.getPrimaryMeasureId()).isEqualTo("OBS_VALUE");
            assertThat(o.getTimeDimensionId()).isEqualTo("TIME");
            assertThat(o.getRef()).isEqualTo(DataStructureRef.of("NBB", "TEST_DATASET", null));
            assertThat(o.getDimensions()).hasSize(3).element(0).satisfies(x -> {
                assertThat(x.getId()).isEqualTo("SUBJECT");
                assertThat(x.getLabel()).isEqualTo("Sujet");
                assertThat(x.getPosition()).isEqualTo(1);
            });
        });

        assertThatIOException()
                .isThrownBy(() -> p1.parseReader(SdmxSource.ECB_DATA_STRUCTURE::openReader))
                .withCauseInstanceOf(XMLStreamException.class)
                .withMessageContaining("Invalid namespace");
    }
}
