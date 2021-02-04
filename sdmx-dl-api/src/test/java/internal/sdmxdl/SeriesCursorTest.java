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

import org.junit.Test;
import sdmxdl.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class SeriesCursorTest {

    @Test
    public void testSeriesToStream() throws IOException {
        try (DataCursor c = SeriesCursor.of(iter())) {
            assertThat(c.toStream()).isEmpty();
        }

        try (DataCursor c = SeriesCursor.of(iter(s1))) {
            assertThat(c.toStream()).containsExactly(s1);
        }

        try (DataCursor c = SeriesCursor.of(iter(s1, s2))) {
            assertThat(c.toStream()).containsExactly(s1, s2);
        }

        try (DataCursor c = SeriesCursor.of(iter(s1, s2))) {
            c.nextSeries(); // skip first
            assertThat(c.toStream()).containsExactly(s2);
        }

        try (DataCursor c = SeriesCursor.of(iter(s1, s2))) {
            c.nextSeries(); // skip first
            c.nextSeries(); // skip second
            assertThat(c.toStream()).isEmpty();
        }
    }

    @Test
    public void testNextSeries() throws IOException {
        try (DataCursor c = SeriesCursor.of(iter())) {
            assertThat(c.nextSeries()).isFalse();
        }

        try (DataCursor c = SeriesCursor.of(iter(s1))) {
            assertThat(c.nextSeries()).isTrue();
            assertThat(c.nextSeries()).isFalse();
        }

        try (DataCursor c = SeriesCursor.of(iter(s1, s2))) {
            assertThat(c.nextSeries()).isTrue();
            assertThat(c.nextSeries()).isTrue();
            assertThat(c.nextSeries()).isFalse();
        }
    }

    private Iterator<Series> iter(Series... items) {
        return Arrays.asList(items).iterator();
    }

    private final Series s1 = Series.builder().key(Key.of("BE")).freq(Frequency.MONTHLY).obs(Obs.of(LocalDateTime.now(), Math.PI)).meta("hello", "world").build();
    private final Series s2 = Series.builder().key(Key.of("FR")).freq(Frequency.MONTHLY).obs(Obs.of(LocalDateTime.now(), Math.PI)).meta("hello", "world").build();
}
