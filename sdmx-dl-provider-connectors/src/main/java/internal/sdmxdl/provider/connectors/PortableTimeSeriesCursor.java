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
package internal.sdmxdl.provider.connectors;

import it.bancaditalia.oss.sdmx.api.BaseObservation;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import lombok.NonNull;
import sdmxdl.DataStructure;
import sdmxdl.Key;
import sdmxdl.format.ObsParser;
import sdmxdl.format.DataCursor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class PortableTimeSeriesCursor implements DataCursor {

    public static PortableTimeSeriesCursor of(List<PortableTimeSeries<Double>> data, Supplier<ObsParser> df, DataStructure dsd) {
        return new PortableTimeSeriesCursor(data.iterator(), Key.builder(dsd), df.get());
    }

    private final Iterator<PortableTimeSeries<Double>> data;
    private final Key.Builder keyBuilder;
    private final ObsParser obsParser;

    private PortableTimeSeries<Double> current = null;
    private int index = -1;
    private boolean closed = false;
    private boolean hasObs = false;

    @Override
    public boolean nextSeries() throws IOException {
        checkState();
        boolean result = data.hasNext();
        if (result) {
            current = data.next();
            current.getDimensionsMap().forEach(keyBuilder::put);
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
        return hasObs = (index < current.size());
    }

    @Override
    public @NonNull Key getSeriesKey() throws IOException {
        checkSeriesState();
        return keyBuilder.build();
    }

    @Override
    public String getSeriesAttribute(@NonNull String key) throws IOException {
        checkSeriesState();
        return current.getAttribute(key);
    }

    @Override
    public @NonNull Map<String, String> getSeriesAttributes() throws IOException {
        checkSeriesState();
        Map<String, String> result = current.getAttributesMap();
        result.remove(PortableTimeSeries.GENERATEDNAME_ATTR_NAME);
        result.remove(ACTION_ATTR_NAME);
        result.remove(VALID_FROM_ATTR_NAME);
        result.remove(VALID_TO_ATTR_NAME);
        return result;
    }

    @Override
    public LocalDateTime getObsPeriod() throws IOException {
        checkObsState();
        BaseObservation<? extends Double> obs = current.get(index);
        return obsParser
                .period(obs.getTimeslot())
                .parsePeriod(obs::getAttributeValue);
    }

    @Override
    public Double getObsValue() throws IOException {
        checkObsState();
        return current.get(index).getValue();
    }

    @Override
    public @NonNull Map<String, String> getObsAttributes() throws IOException, IllegalStateException {
        checkObsState();
        return current.get(index).getAttributes();
    }

    @Override
    public void close() {
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

    private static final String ACTION_ATTR_NAME = "action";
    private static final String VALID_FROM_ATTR_NAME = "validFromDate";
    private static final String VALID_TO_ATTR_NAME = "validToDate";
}
