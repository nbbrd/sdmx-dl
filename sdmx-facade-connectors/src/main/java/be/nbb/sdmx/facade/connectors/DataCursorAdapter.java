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
package be.nbb.sdmx.facade.connectors;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.TimeFormat;
import be.nbb.sdmx.facade.util.ObsParser;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Philippe Charles
 */
final class DataCursorAdapter implements DataCursor {

    private final Iterator<PortableTimeSeries> data;
    private final ObsParser obs;
    private PortableTimeSeries current;
    private int index;
    private boolean closed;
    private boolean hasObs;

    DataCursorAdapter(List<PortableTimeSeries> data) {
        this.data = data.iterator();
        this.obs = new ObsParser();
        this.closed = false;
        this.hasObs = false;
    }

    @Override
    public boolean nextSeries() throws IOException {
        checkState();
        boolean result = data.hasNext();
        if (result) {
            current = data.next();
            obs.setTimeFormat(getTimeFormat(current));
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
    public TimeFormat getSeriesTimeFormat() throws IOException {
        checkSeriesState();
        return obs.getTimeFormat();
    }

    @Override
    public String getSeriesAttribute(String key) throws IOException {
        checkSeriesState();
        Objects.requireNonNull(key);
        return current.getAttributeValue(key);
    }

    @Override
    public Map<String, String> getSeriesAttributes() throws IOException {
        checkSeriesState();
        return parseAttributes(current);
    }

    @Override
    public LocalDateTime getObsPeriod() throws IOException {
        checkObsState();
        return obs.periodString(current.getTimeSlots().get(index)).getPeriod();
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
    private static TimeFormat getTimeFormat(PortableTimeSeries input) {
        String value = input.getAttributeValue("TIME_FORMAT");
        if (value != null) {
            return TimeFormat.parseByTimeFormat(value);
        }
        if (input.getFrequency() != null) {
            return TimeFormat.parseByFrequencyCodeId(input.getFrequency());
        }
        return TimeFormat.UNDEFINED;
    }

    private static Key parseKey(PortableTimeSeries ts) throws IOException {
        List<String> dimensions = ts.getDimensions();
        if (dimensions.isEmpty()) {
            return Key.ALL;
        }
        String[] result = new String[dimensions.size()];
        for (int i = 0; i < result.length; i++) {
            String o = dimensions.get(i);
            int sepIndex = o.indexOf("=");
            check(sepIndex != -1, "Invalid dimension entry: '%s'", o);
            result[i] = o.substring(sepIndex + 1);
        }
        return Key.of(result);
    }

    private static Map<String, String> parseAttributes(PortableTimeSeries ts) throws IOException {
        List<String> attributes = ts.getAttributes();
        switch (attributes.size()) {
            case 0:
                return Collections.emptyMap();
            case 1: {
                String o = attributes.get(0);
                int sepIndex = o.indexOf("=");
                check(sepIndex != -1, "Invalid attribute entry: '%s'", o);
                return Collections.singletonMap(o.substring(0, sepIndex), o.substring(sepIndex + 1));
            }
            default: {
                Map<String, String> result = new HashMap<>();
                for (String o : attributes) {
                    int sepIndex = o.indexOf("=");
                    check(sepIndex != -1, "Invalid attribute entry: '%s'", o);
                    result.put(o.substring(0, sepIndex), o.substring(sepIndex + 1));
                }
                return result;
            }
        }
    }

    private static void check(boolean expression, String message, Object... args) throws IOException {
        if (!expression) {
            throw new IOException(String.format(message, args));
        }
    }
    //</editor-fold>
}
