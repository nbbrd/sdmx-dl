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
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.samples.SdmxSource;
import be.nbb.util.Stax;
import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class XMLStreamFlow21Test {

    @Test
    public void test() throws IOException {
        Stax.Parser<List<Dataflow>> p = Stax.Parser.of(new XMLStreamFlow21(LanguagePriorityList.ANY)::parse);

        assertThat(p.onReader(xif).parseWithIO(SdmxSource.ECB_DATAFLOWS::openReader))
                .containsExactly(
                        Dataflow.of(DataflowRef.of("ECB", "AME", "1.0"), DataStructureRef.of("ECB", "ECB_AME1", "1.0"), "AMECO"),
                        Dataflow.of(DataflowRef.of("ECB", "BKN", "1.0"), DataStructureRef.of("ECB", "ECB_BKN1", "1.0"), "Banknotes statistics"),
                        Dataflow.of(DataflowRef.of("ECB", "BLS", "1.0"), DataStructureRef.of("ECB", "ECB_BLS1", "1.0"), "Bank Lending Survey Statistics")
                );
    }

    private final XMLInputFactory xif = Stax.getInputFactory();
}
