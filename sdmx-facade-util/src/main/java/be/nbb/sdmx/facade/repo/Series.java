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
import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.Key;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class Series {

    @lombok.NonNull
    Key key;

    @lombok.NonNull
    Frequency frequency;

    @lombok.NonNull
    @lombok.Singular(value = "obs")
    List<Obs> obs;

    @lombok.NonNull
    @lombok.Singular(value = "meta")
    Map<String, String> meta;

    @Nonnull
    public static List<Series> copyOf(@Nonnull DataCursor cursor) throws IOException {
        if (!cursor.nextSeries()) {
            return Collections.emptyList();
        }
        Series.Builder series = builder();
        List<Series> result = new ArrayList<>();
        result.add(getSeries(series, cursor));
        while (cursor.nextSeries()) {
            result.add(getSeries(series, cursor));
        }
        return result;
    }

    @Nonnull
    public static DataCursor asCursor(@Nonnull List<Series> list, @Nonnull Key key) {
        return new SeriesCursor(list, key);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static Series getSeries(Series.Builder series, DataCursor cursor) throws IOException {
        series.key(cursor.getSeriesKey())
                .frequency(cursor.getSeriesFrequency())
                .clearMeta()
                .clearObs()
                .meta(cursor.getSeriesAttributes());
        while (cursor.nextObs()) {
            series.obs(Obs.of(cursor.getObsPeriod(), cursor.getObsValue()));
        }
        return series.build();
    }

    private static final class SeriesCursor implements DataCursor {

        private final List<Series> col;
        private final Key key;
        private int i;
        private int j;
        private boolean closed;
        private boolean hasSeries;
        private boolean hasObs;

        SeriesCursor(List<Series> col, Key key) {
            this.col = col;
            this.key = key;
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
            } while (i < col.size() && !key.contains(col.get(i).getKey()));
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
    //</editor-fold>
}
