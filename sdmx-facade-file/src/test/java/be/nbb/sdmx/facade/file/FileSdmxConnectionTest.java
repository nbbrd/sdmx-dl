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
package be.nbb.sdmx.facade.file;

import be.nbb.sdmx.facade.file.impl.XMLStreamSdmxDecoder;
import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.TimeFormat;
import java.io.File;
import java.io.IOException;
import javax.xml.stream.XMLInputFactory;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public class FileSdmxConnectionTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
    private final XMLInputFactory factory = XMLInputFactory.newInstance();
    private final SdmxDecoder decoder = new XMLStreamSdmxDecoder(factory);

    @Test
    public void testCompactData21() throws IOException {
        File compact21 = TestResources.COMPACT21_FILE.copyTo(temp);

        FileSdmxConnection conn = new FileSdmxConnection(compact21, factory, decoder);

        DataflowRef flowRef = DataflowRef.parse(compact21.getName());

        assertEquals(1, conn.getDataflows().size());
        assertEquals(7, conn.getDataStructure(flowRef).getDimensions().size());

        Key key = Key.of("A", "BEL", "1", "0", "0", "0", "OVGD");

        try (DataCursor cursor = conn.getData(flowRef, Key.ALL, false)) {
            assertTrue(cursor.nextSeries());
            assertEquals(key, cursor.getKey());
            assertEquals(TimeFormat.YEARLY, cursor.getTimeFormat());
            int indexObs = -1;
            while (cursor.nextObs()) {
                switch (++indexObs) {
                    case 0:
                        assertThat(cursor.getPeriod()).isEqualTo("1960-01-01");
                        assertEquals(92.0142, cursor.getValue(), 0d);
                        break;
                    case 56:
                        assertThat(cursor.getPeriod()).isEqualTo("2016-01-01");
                        assertEquals(386.5655, cursor.getValue(), 0d);
                        break;
                }
            }
            assertEquals(56, indexObs);
            assertFalse(cursor.nextSeries());
        }
    }
}
