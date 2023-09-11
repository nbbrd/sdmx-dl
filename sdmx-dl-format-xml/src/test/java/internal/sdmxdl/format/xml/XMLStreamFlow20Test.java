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
public class XMLStreamFlow20Test {

    @Test
    public void test() throws IOException {
        Xml.Parser<List<Flow>> p = Stax.StreamParser.valueOf(new XMLStreamFlow20(Languages.ANY)::parse);

        assertThat(p.parseReader(SdmxXmlSources.OTHER_FLOWS20::openReader))
                .containsExactly(
                        Flow
                                .builder()
                                .ref(FlowRef.of("IMF", "DS-BOP_2017M06", "1.0"))
                                .structureRef(StructureRef.of("IMF", "BOP_2017M06", null))
                                .name("Balance of Payments (BOP), 2017 M06")
                                .build(),
                        Flow
                                .builder()
                                .ref(FlowRef.of("IMF", "DS-BOP_2020M3", "1.0"))
                                .structureRef(StructureRef.of("IMF", "BOP_2020M3", null))
                                .name("Balance of Payments (BOP), 2020 M03")
                                .build()
                );
    }
}
