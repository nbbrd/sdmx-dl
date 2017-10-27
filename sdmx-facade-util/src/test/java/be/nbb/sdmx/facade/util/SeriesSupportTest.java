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
package be.nbb.sdmx.facade.util;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.Obs;
import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.samples.SdmxSource;
import be.nbb.sdmx.facade.tck.DataCursorAssert;
import be.nbb.sdmx.facade.xml.stream.SdmxXmlStreams;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SeriesSupportTest {

    @Test
    @SuppressWarnings("null")
    public void testAsCursor() {
        assertThatThrownBy(() -> SeriesSupport.asCursor(null, Key.ALL)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SeriesSupport.asCursor(Collections.emptyList(), null)).isInstanceOf(NullPointerException.class);

        DataCursorAssert.assertCompliance(() -> SeriesSupport.asCursor(Collections.emptyList(), Key.ALL));
    }

    @Test
    @SuppressWarnings("null")
    public void testAsStream() {
        assertThatThrownBy(() -> SeriesSupport.asStream(null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> {
            DataCursor cursor = NoOpCursor.noOp();
            SeriesSupport.asStream(() -> cursor).count();
            cursor.getSeriesKey();
        }).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testCopyOf() throws IOException, XMLStreamException {
        assertThatThrownBy(() -> SeriesSupport.copyOf(null)).isInstanceOf(NullPointerException.class);

        try (DataCursor c = NoOpCursor.noOp()) {
            assertThat(SeriesSupport.copyOf(c)).isEmpty();
        }

        try (DataCursor c = SeriesSupport.asCursor(Collections.singletonList(series), Key.ALL)) {
            assertThat(SeriesSupport.copyOf(c)).hasSize(1).element(0).isSameAs(series);
        }

        List<DataStructure> dsds = SdmxXmlStreams.struct21(LanguagePriorityList.ANY).get(SdmxSource.ECB_DATA_STRUCTURE.openXmlStream(SdmxSource.XIF));
        try (DataCursor o = SdmxXmlStreams.genericData21(dsds.get(0)).get(SdmxSource.ECB_DATA.openXmlStream(SdmxSource.XIF))) {
            assertThat(SeriesSupport.copyOf(o)).hasSize(120);
        }
    }

    private final Series series = Series.builder().key(Key.of("BE")).freq(Frequency.MONTHLY).obs(Obs.of(LocalDateTime.now(), Math.PI)).meta("hello", "world").build();
}
