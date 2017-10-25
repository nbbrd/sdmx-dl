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
package be.nbb.sdmx.facade.repo;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.samples.SdmxSource;
import be.nbb.sdmx.facade.util.NoOpCursor;
import be.nbb.sdmx.facade.xml.stream.SdmxXmlStreams;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SeriesTest {

    @Test
    public void testCopyOf() throws IOException, XMLStreamException {
        try (DataCursor c = NoOpCursor.noOp()) {
            assertThat(Series.copyOf(c)).isEmpty();
        }

        Series s1 = Series.builder().key(Key.of("hello")).frequency(Frequency.WEEKLY).obs(Obs.of(LocalDateTime.now(), 3.14)).build();
        try (DataCursor c = Series.asCursor(Collections.singletonList(s1), Key.ALL)) {
            assertThat(Series.copyOf(c)).hasSize(1).element(0).isSameAs(s1);
        }

        List<DataStructure> dsds = SdmxXmlStreams.struct21(LanguagePriorityList.ANY).get(SdmxSource.ECB_DATA_STRUCTURE.openXmlStream(SdmxSource.XIF));
        try (DataCursor o = SdmxXmlStreams.genericData21(dsds.get(0)).get(SdmxSource.ECB_DATA.openXmlStream(SdmxSource.XIF))) {
            assertThat(Series.copyOf(o)).hasSize(120);
        }
    }
}
