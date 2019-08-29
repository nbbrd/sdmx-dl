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
import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.Obs;
import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.tck.DataCursorAssert;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import javax.xml.stream.XMLStreamException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SeriesSupportTest {

    @Test
    @SuppressWarnings("null")
    public void testAsCursor() {
        assertThatNullPointerException().isThrownBy(() -> SeriesSupport.asCursor(null, Key.ALL));
        assertThatNullPointerException().isThrownBy(() -> SeriesSupport.asCursor(Collections.emptyList(), null));

        DataCursorAssert.assertCompliance(() -> SeriesSupport.asCursor(Collections.emptyList(), Key.ALL));
    }

    @Test
    @SuppressWarnings("null")
    public void testAsStream() {
        assertThatNullPointerException().isThrownBy(() -> SeriesSupport.asStream(null));

        assertThatIllegalStateException().isThrownBy(() -> {
            DataCursor cursor = DataCursor.empty();
            SeriesSupport.asStream(() -> cursor).count();
            cursor.getSeriesKey();
        });
    }

    @Test
    @SuppressWarnings("null")
    public void testCopyOf() throws IOException, XMLStreamException {
        assertThatNullPointerException().isThrownBy(() -> SeriesSupport.copyOf(null));

        try (DataCursor c = DataCursor.empty()) {
            assertThat(SeriesSupport.copyOf(c)).isEmpty();
        }

        try (DataCursor c = SeriesSupport.asCursor(Collections.singletonList(s1), Key.ALL)) {
            assertThat(SeriesSupport.copyOf(c)).hasSize(1).element(0).isSameAs(s1);
        }

        try (DataCursor c = SeriesSupport.asCursor(Arrays.asList(s1, s2), Key.ALL)) {
            assertThat(SeriesSupport.copyOf(c)).hasSize(2);
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testCopyOfKeysAndMeta() throws IOException, XMLStreamException {
        assertThatNullPointerException().isThrownBy(() -> SeriesSupport.copyOfKeysAndMeta(null));

        try (DataCursor c = DataCursor.empty()) {
            assertThat(SeriesSupport.copyOfKeysAndMeta(c)).isEmpty();
        }

        try (DataCursor c = SeriesSupport.asCursor(Collections.singletonList(s1), Key.ALL)) {
            assertThat(SeriesSupport.copyOfKeysAndMeta(c))
                    .allMatch(o -> o.getObs().isEmpty())
                    .hasSize(1)
                    .element(0)
                    .isEqualToComparingOnlyGivenFields(s1, "key", "freq", "meta");
        }

        try (DataCursor c = SeriesSupport.asCursor(Arrays.asList(s1, s2), Key.ALL)) {
            assertThat(SeriesSupport.copyOfKeysAndMeta(c))
                    .allMatch(o -> o.getObs().isEmpty())
                    .hasSize(2);
        }
    }

    private final Series s1 = Series.builder().key(Key.of("BE")).freq(Frequency.MONTHLY).obs(Obs.of(LocalDateTime.now(), Math.PI)).meta("hello", "world").build();
    private final Series s2 = Series.builder().key(Key.of("FR")).freq(Frequency.MONTHLY).obs(Obs.of(LocalDateTime.now(), Math.PI)).meta("hello", "world").build();
}
