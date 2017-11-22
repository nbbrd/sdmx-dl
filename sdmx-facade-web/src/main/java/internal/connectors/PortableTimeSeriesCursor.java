/*
 * Copyright 2015 National Bank of Belgium
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
package internal.connectors;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.parser.ObsParser;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import be.nbb.sdmx.facade.parser.Freqs;
import static be.nbb.sdmx.facade.parser.Freqs.TIME_FORMAT_CONCEPT;

/**
 *
 * @author Philippe Charles
 */
public final class PortableTimeSeriesCursor implements DataCursor {

    private final Iterator<PortableTimeSeries> data;
    private final ObsParser obs;
    private PortableTimeSeries current;
    private int index;
    private boolean closed;
    private boolean hasObs;

    public PortableTimeSeriesCursor(List<PortableTimeSeries> data, ObsParser obs) {
        this.data = data.iterator();
        this.obs = obs;
        this.closed = false;
        this.hasObs = false;
    }

    @Override
    public boolean nextSeries() throws IOException {
        checkState();
        boolean result = data.hasNext();
        if (result) {
            current = data.next();
            obs.frequency(getFrequency(current));
            index = -1;
        } else {
            current = null;
        }
        return result;
    }

    @Override
    public boolean nextObs() throws IOException {
        checkSeriesState();
        index++;
        return hasObs = (index < current.getObservations().size());
    }

    @Override
    public Key getSeriesKey() throws IOException {
        checkSeriesState();
        return parseKey(current);
    }

    @Override
    public Frequency getSeriesFrequency() throws IOException {
        checkSeriesState();
        return obs.getFrequency();
    }

    @Override
    public String getSeriesAttribute(String key) throws IOException {
        checkSeriesState();
        Objects.requireNonNull(key);
        return current.getAttribute(key);
    }

    @Override
    public Map<String, String> getSeriesAttributes() throws IOException {
        checkSeriesState();
        return current.getAttributesMap();
    }

    @Override
    public LocalDateTime getObsPeriod() throws IOException {
        checkObsState();
        return obs.period(current.getTimeSlots().get(index)).parsePeriod();
    }

    @Override
    public Double getObsValue() throws IOException {
        checkObsState();
        Object result = current.getObservations().get(index);
        return result instanceof Double ? (Double) result : null;
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
        if (current == null) {
            throw new IllegalStateException();
        }
    }

    private void checkObsState() throws IOException, IllegalStateException {
        checkSeriesState();
        if (!hasObs) {
            throw new IllegalStateException();
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static Frequency getFrequency(PortableTimeSeries input) {
        if (input.getFrequency() != null) {
            return Freqs.parseByFreq(input.getFrequency());
        }
        String value = input.getAttribute(TIME_FORMAT_CONCEPT);
        if (value != null) {
            return Freqs.parseByTimeFormat(value);
        }
        return Frequency.UNDEFINED;
    }

    private static Key parseKey(PortableTimeSeries ts) throws IOException {
        Key result = Key.of(ts.getDimensionsMap().values());
        if (!result.isSeries()) {
            throw new IOException("Invalid series key '" + result + "'");
        }
        return result;
    }
    //</editor-fold>
}
