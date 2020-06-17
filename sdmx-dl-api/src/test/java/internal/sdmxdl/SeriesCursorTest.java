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
package internal.sdmxdl;

import internal.sdmxdl.SeriesCursor;
import sdmxdl.DataCursor;
import sdmxdl.DataFilter;
import sdmxdl.Frequency;
import sdmxdl.Key;
import sdmxdl.Obs;
import sdmxdl.Series;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SeriesCursorTest {

    @Test
    @SuppressWarnings("null")
    public void testSeriesToStream() throws IOException {
        try (DataCursor c = new SeriesCursor(iter(s1, s2), Key.ALL)) {
            assertThatNullPointerException().isThrownBy(() -> c.toStream(null));
        }

        try (DataCursor c = new SeriesCursor(iter(s1, s2), Key.ALL)) {
            c.nextSeries(); // skip first
            assertThat(c.toStream(DataFilter.Detail.FULL)).hasSize(1).element(0).isSameAs(s2);
        }

        try (DataCursor c = new SeriesCursor(iter(s1, s2), Key.ALL)) {
            assertThat(c.toStream(DataFilter.Detail.FULL)).hasSize(2);
        }

        try (DataCursor c = new SeriesCursor(iter(s1), Key.ALL)) {
            assertThat(c.toStream(DataFilter.Detail.NO_DATA))
                    .allMatch(o -> o.getObs().isEmpty())
                    .hasSize(1)
                    .element(0)
                    .isEqualToComparingOnlyGivenFields(s1, "key", "freq", "meta");
        }

        try (DataCursor c = new SeriesCursor(iter(s1, s2), Key.ALL)) {
            assertThat(c.toStream(DataFilter.Detail.NO_DATA))
                    .allMatch(o -> o.getObs().isEmpty())
                    .hasSize(2);
        }
    }

    private Iterator<Series> iter(Series... items) {
        return Arrays.asList(items).iterator();
    }

    private final Series s1 = Series.builder().key(Key.of("BE")).freq(Frequency.MONTHLY).obs(Obs.of(LocalDateTime.now(), Math.PI)).meta("hello", "world").build();
    private final Series s2 = Series.builder().key(Key.of("FR")).freq(Frequency.MONTHLY).obs(Obs.of(LocalDateTime.now(), Math.PI)).meta("hello", "world").build();
}
