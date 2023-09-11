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

import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;
import org.junit.jupiter.api.Test;
import sdmxdl.Flow;
import sdmxdl.StructureRef;
import sdmxdl.FlowRef;
import sdmxdl.Languages;
import tests.sdmxdl.format.xml.SdmxXmlSources;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class XMLStreamFlow21Test {

    @Test
    public void test() throws IOException {
        Xml.Parser<List<Flow>> p = Stax.StreamParser.valueOf(new XMLStreamFlow21(Languages.ANY)::parse);

        assertThat(p.parseReader(SdmxXmlSources.ECB_DATAFLOWS::openReader))
                .containsExactly(
                        Flow
                                .builder()
                                .ref(FlowRef.of("ECB", "AME", "1.0"))
                                .structureRef(StructureRef.of("ECB", "ECB_AME1", "1.0"))
                                .name("AMECO")
                                .build(),
                        Flow
                                .builder()
                                .ref(FlowRef.of("ECB", "BKN", "1.0"))
                                .structureRef(StructureRef.of("ECB", "ECB_BKN1", "1.0"))
                                .name("Banknotes statistics")
                                .build(),
                        Flow
                                .builder()
                                .ref(FlowRef.of("ECB", "BLS", "1.0"))
                                .structureRef(StructureRef.of("ECB", "ECB_BLS1", "1.0"))
                                .name("Bank Lending Survey Statistics")
                                .build()
                );
    }

    @Test
    public void testDescription() throws IOException {
        Xml.Parser<List<Flow>> p = Stax.StreamParser.valueOf(new XMLStreamFlow21(Languages.ANY)::parse);

        assertThat(p.parseResource(XMLStreamMessageFooter21Test.class, "FlowWithDescription.xml"))
                .containsExactly(
                        Flow
                                .builder()
                                .ref(FlowRef.of("CD2030", "CD2030", "1.0"))
                                .structureRef(StructureRef.of("CD2030", "CD2030", "1.0"))
                                .name("Coundown 2030")
                                .description("This dataset is used to support the downolad of the CD2030 data")
                                .build(),
                        Flow
                                .builder()
                                .ref(FlowRef.of("EAPRO", "DF_EAPRO_CROSS_SECTOR", "1.0"))
                                .structureRef(StructureRef.of("EAPRO", "DSD_EAPRO", "1.0"))
                                .name("EAPRO Cross Sector Indicators")
                                .description("A dataflow based on a subset of the global cross-sector indicators.")
                                .build()
                );
    }
}
