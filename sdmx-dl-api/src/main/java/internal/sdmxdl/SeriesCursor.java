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
package internal.sdmxdl;

import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SeriesCursor implements DataCursor {

    public static @NonNull DataCursor of(@NonNull Iterator<Series> iterator) {
        return iterator instanceof SeriesIterator
                ? ((SeriesIterator) iterator).delegate
                : new SeriesCursor(iterator);
    }

    @lombok.NonNull
    final Iterator<Series> delegate;

    private Series series = null;
    private Iterator<Obs> obsIterator = null;
    private Obs obs = null;
    private boolean closed = false;

    @Override
    public boolean nextSeries() throws IOException {
        checkState();
        series = delegate.hasNext() ? delegate.next() : null;
        obsIterator = series != null ? series.getObs().iterator() : null;
        return series != null;
    }

    @Override
    public boolean nextObs() throws IOException {
        checkSeriesState();
        obs = obsIterator.hasNext() ? obsIterator.next() : null;
        return obs != null;
    }

    @Override
    public Key getSeriesKey() throws IOException {
        checkSeriesState();
        return series.getKey();
    }

    @Override
    public Frequency getSeriesFrequency() throws IOException {
        checkSeriesState();
        return series.getFreq();
    }

    @Override
    public String getSeriesAttribute(String key) throws IOException {
        checkSeriesState();
        Objects.requireNonNull(key);
        return series.getMeta().get(key);
    }

    @Override
    public Map<String, String> getSeriesAttributes() throws IOException {
        checkSeriesState();
        return series.getMeta();
    }

    @Override
    public LocalDateTime getObsPeriod() throws IOException {
        checkObsState();
        return obs.getPeriod();
    }

    @Override
    public Double getObsValue() throws IOException {
        checkObsState();
        return obs.getValue();
    }

    @Override
    public Map<String, String> getObsAttributes() throws IOException {
        checkObsState();
        return obs.getMeta();
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public Stream<Series> toStream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(delegate, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

    private void checkState() throws IOException {
        if (closed) {
            throw new IOException("Cursor closed");
        }
    }

    private void checkSeriesState() throws IOException, IllegalStateException {
        checkState();
        if (series == null) {
            throw new IllegalStateException();
        }
    }

    private void checkObsState() throws IOException, IllegalStateException {
        checkSeriesState();
        if (obs == null) {
            throw new IllegalStateException();
        }
    }
}
