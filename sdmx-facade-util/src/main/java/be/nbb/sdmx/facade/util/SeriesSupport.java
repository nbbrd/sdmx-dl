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
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SeriesSupport {

    @Nonnull
    public List<Series> copyOf(@Nonnull DataCursor cursor) throws IOException {
        if (!cursor.nextSeries()) {
            return Collections.emptyList();
        }

        if (cursor instanceof SeriesCursor) {
            return ((SeriesCursor) cursor).getRemainingItems();
        }

        Series.Builder b = Series.builder();
        List<Series> result = new ArrayList<>();
        do {
            result.add(getSeries(b, cursor));
        } while (cursor.nextSeries());
        return result;
    }

    @Nonnull
    public DataCursor asCursor(@Nonnull List<Series> list, @Nonnull Key ref) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(ref);
        return new SeriesCursor(list, ref);
    }

    @Nonnull
    public Stream<Series> asStream(@Nonnull IO.Supplier<DataCursor> supplier) throws IOException {
        return IO.stream(supplier, SeriesSupport::getDataStream);
    }

    @SuppressWarnings("null")
    private Stream<Series> getDataStream(DataCursor cursor) {
        Series.Builder builder = Series.builder();
        return IO.streamNonnull(() -> cursor.nextSeries() ? getSeries(builder, cursor) : null);
    }

    private Series getSeries(Series.Builder builder, DataCursor cursor) throws IOException {
        builder.key(cursor.getSeriesKey())
                .frequency(cursor.getSeriesFrequency())
                .clearMeta()
                .clearObs()
                .meta(cursor.getSeriesAttributes());
        while (cursor.nextObs()) {
            builder.obs(Obs.of(cursor.getObsPeriod(), cursor.getObsValue()));
        }
        return builder.build();
    }

    private static final class SeriesCursor implements DataCursor {

        private final List<Series> col;
        private final Key ref;
        private int i;
        private int j;
        private boolean closed;
        private boolean hasSeries;
        private boolean hasObs;

        SeriesCursor(List<Series> col, Key ref) {
            this.col = col;
            this.ref = ref;
            this.i = -1;
            this.j = -1;
            this.closed = false;
            this.hasSeries = false;
            this.hasObs = false;
        }

        @Override
        public boolean nextSeries() throws IOException {
            checkState();
            do {
                i++;
                j = -1;
            } while (i < col.size() && !ref.contains(col.get(i).getKey()));
            return hasSeries = (i < col.size());
        }

        @Override
        public boolean nextObs() throws IOException {
            checkSeriesState();
            j++;
            return hasObs = (j < col.get(i).getObs().size());
        }

        @Override
        public Key getSeriesKey() throws IOException {
            checkSeriesState();
            return col.get(i).getKey();
        }

        @Override
        public Frequency getSeriesFrequency() throws IOException {
            checkSeriesState();
            return col.get(i).getFrequency();
        }

        @Override
        public String getSeriesAttribute(String key) throws IOException {
            checkSeriesState();
            Objects.requireNonNull(key);
            return col.get(i).getMeta().get(key);
        }

        @Override
        public Map<String, String> getSeriesAttributes() throws IOException {
            checkSeriesState();
            return col.get(i).getMeta();
        }

        @Override
        public LocalDateTime getObsPeriod() throws IOException {
            checkObsState();
            return col.get(i).getObs().get(j).getPeriod();
        }

        @Override
        public Double getObsValue() throws IOException {
            checkObsState();
            return col.get(i).getObs().get(j).getValue();
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }

        List<Series> getRemainingItems() {
            return col.subList(i, col.size());
        }

        private void checkState() throws IOException {
            if (closed) {
                throw new IOException("Cursor closed");
            }
        }

        private void checkSeriesState() throws IOException, IllegalStateException {
            checkState();
            if (!hasSeries) {
                throw new IllegalStateException();
            }
        }

        private void checkObsState() throws IOException, IllegalStateException {
            checkSeriesState();
            if (!hasObs) {
                throw new IllegalStateException();
            }
        }
    }
}
