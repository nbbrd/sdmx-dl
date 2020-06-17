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

import sdmxdl.DataStructureRef;
import sdmxdl.Dataflow;
import sdmxdl.DataflowRef;
import sdmxdl.LanguagePriorityList;
import sdmxdl.samples.SdmxSource;
import java.io.IOException;
import java.util.List;
import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class XMLStreamFlow21Test {

    @Test
    public void test() throws IOException {
        Xml.Parser<List<Dataflow>> p = Stax.StreamParser.valueOf(new XMLStreamFlow21(LanguagePriorityList.ANY)::parse);

        assertThat(p.parseReader(SdmxSource.ECB_DATAFLOWS::openReader))
                .containsExactly(
                        Dataflow.of(DataflowRef.of("ECB", "AME", "1.0"), DataStructureRef.of("ECB", "ECB_AME1", "1.0"), "AMECO"),
                        Dataflow.of(DataflowRef.of("ECB", "BKN", "1.0"), DataStructureRef.of("ECB", "ECB_BKN1", "1.0"), "Banknotes statistics"),
                        Dataflow.of(DataflowRef.of("ECB", "BLS", "1.0"), DataStructureRef.of("ECB", "ECB_BLS1", "1.0"), "Bank Lending Survey Statistics")
                );
    }
}
