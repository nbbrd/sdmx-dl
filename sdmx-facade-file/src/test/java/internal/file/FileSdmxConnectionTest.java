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
package internal.file;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.QueryParameters;
import be.nbb.sdmx.facade.file.SdmxFile;
import be.nbb.sdmx.facade.samples.SdmxSource;
import be.nbb.sdmx.facade.tck.ConnectionAssert;
import java.io.File;
import java.io.IOException;
import javax.xml.stream.XMLInputFactory;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
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
        File compact21 = temp.newFile();
        SdmxSource.OTHER_COMPACT21.copyTo(compact21.toPath());

        SdmxFile file = new SdmxFile(compact21, null);

        FileSdmxConnection conn = new FileSdmxConnection(file, LanguagePriorityList.ANY, factory, decoder);

        assertThat(conn.getDataflows()).hasSize(1);
        assertThat(conn.getDataStructure(file.getDataflowRef()).getDimensions()).hasSize(7);

        Key key = Key.of("A", "BEL", "1", "0", "0", "0", "OVGD");

        try (DataCursor o = conn.getData(file.getDataflowRef(), Key.ALL, QueryParameters.FULL)) {
            assertThat(o.nextSeries()).isTrue();
            assertThat(o.getSeriesKey()).isEqualTo(key);
            assertThat(o.getSeriesFrequency()).isEqualTo(Frequency.ANNUAL);
            int indexObs = -1;
            while (o.nextObs()) {
                switch (++indexObs) {
                    case 0:
                        assertThat(o.getObsPeriod()).isEqualTo("1960-01-01T00:00:00");
                        assertThat(o.getObsValue()).isEqualTo(92.0142);
                        break;
                    case 56:
                        assertThat(o.getObsPeriod()).isEqualTo("2016-01-01T00:00:00");
                        assertThat(o.getObsValue()).isEqualTo(386.5655);
                        break;
                }
            }
            assertThat(indexObs).isEqualTo(56);
            assertThat(o.nextSeries()).isFalse();
        }

        ConnectionAssert.assertCompliance(() -> new FileSdmxConnection(file, LanguagePriorityList.ANY, factory, decoder), file.getDataflowRef());
    }
}
