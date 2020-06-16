/*
 * Copyright 2019 National Bank of Belgium
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
package internal.facade;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataFilter;
import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.Series;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class SeriesCursor implements DataCursor {

    @lombok.NonNull
    private final Iterator<Series> col;

    @lombok.NonNull
    private final Key ref;

    private Series current = null;
    private int obsIdx = -1;
    private boolean closed = false;
    private boolean hasSeries = false;
    private boolean hasObs = false;

    @Override
    public boolean nextSeries() throws IOException {
        checkState();
        do {
            current = col.hasNext() ? col.next() : null;
            obsIdx = -1;
        } while (current != null && !isValidSeries(current));
        return hasSeries = current != null;
    }

    @Override
    public boolean nextObs() throws IOException {
        checkSeriesState();
        obsIdx++;
        return hasObs = (obsIdx < current.getObs().size());
    }

    @Override
    public Key getSeriesKey() throws IOException {
        checkSeriesState();
        return current.getKey();
    }

    @Override
    public Frequency getSeriesFrequency() throws IOException {
        checkSeriesState();
        return current.getFreq();
    }

    @Override
    public String getSeriesAttribute(String key) throws IOException {
        checkSeriesState();
        Objects.requireNonNull(key);
        return current.getMeta().get(key);
    }

    @Override
    public Map<String, String> getSeriesAttributes() throws IOException {
        checkSeriesState();
        return current.getMeta();
    }

    @Override
    public LocalDateTime getObsPeriod() throws IOException {
        checkObsState();
        return current.getObs().get(obsIdx).getPeriod();
    }

    @Override
    public Double getObsValue() throws IOException {
        checkObsState();
        return current.getObs().get(obsIdx).getValue();
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

    @Override
    public Stream<Series> toStream(DataFilter.Detail detail) throws IOException {
        Objects.requireNonNull(detail);
        checkState();
        switch (detail) {
            case FULL:
                return getRemainingItems();
            default:
                return DataCursor.super.toStream(detail);
        }
    }

    private boolean isValidSeries(Series series) {
        return ref.contains(series.getKey());
    }

    private Stream<Series> getRemainingItems() {
        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(col, 0), false)
                .filter(this::isValidSeries);
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
