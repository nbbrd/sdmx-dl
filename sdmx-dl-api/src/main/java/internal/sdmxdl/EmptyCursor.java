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

import sdmxdl.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
public final class EmptyCursor implements DataCursor {

    private boolean closed = false;

    @Override
    public boolean nextSeries() throws IOException {
        checkState();
        return false;
    }

    @Override
    public boolean nextObs() throws IOException {
        checkState();
        throw new IllegalStateException();
    }

    @Override
    public Key getSeriesKey() throws IOException {
        checkState();
        throw new IllegalStateException();
    }

    @Override
    public Frequency getSeriesFrequency() throws IOException {
        checkState();
        throw new IllegalStateException();
    }

    @Override
    public String getSeriesAttribute(String key) throws IOException {
        checkState();
        throw new IllegalStateException();
    }

    @Override
    public Map<String, String> getSeriesAttributes() throws IOException {
        checkState();
        throw new IllegalStateException();
    }

    @Override
    public LocalDateTime getObsPeriod() throws IOException {
        checkState();
        throw new IllegalStateException();
    }

    @Override
    public Double getObsValue() throws IOException {
        checkState();
        throw new IllegalStateException();
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public Stream<Series> toStream(DataFilter.Detail detail) throws IOException {
        Objects.requireNonNull(detail);
        checkState();
        return Stream.empty();
    }

    private void checkState() throws IOException {
        if (closed) {
            throw new IOException("Cursor closed");
        }
    }
}
