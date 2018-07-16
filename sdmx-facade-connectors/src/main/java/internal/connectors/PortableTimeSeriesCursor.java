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
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.parser.DataFactory;
import be.nbb.sdmx.facade.parser.ObsParser;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import be.nbb.sdmx.facade.parser.Freqs;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class PortableTimeSeriesCursor implements DataCursor {

    public static PortableTimeSeriesCursor of(List<PortableTimeSeries<Double>> data, DataFactory df, DataStructure dsd) {
        ObsParser obsParser = new ObsParser(df::getPeriodParser, df.getValueParser());
        Freqs.Parser freqParser = df.getFreqParser(dsd);
        return new PortableTimeSeriesCursor(data.iterator(), Key.builder(dsd), obsParser, freqParser);
    }

    private final Iterator<PortableTimeSeries<Double>> data;
    private final Key.Builder keyBuilder;
    private final ObsParser obsParser;
    private final Freqs.Parser freqParser;

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
            obsParser.frequency(freqParser.parse(keyBuilder, current::getAttribute));
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
    public Key getSeriesKey() throws IOException {
        checkSeriesState();
        return keyBuilder.build();
    }

    @Override
    public Frequency getSeriesFrequency() throws IOException {
        checkSeriesState();
        return obsParser.getFrequency();
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
        Map<String, String> result = current.getAttributesMap();
        result.remove(PortableTimeSeries.GENERATEDNAME_ATTR_NAME);
        return result;
    }

    @Override
    public LocalDateTime getObsPeriod() throws IOException {
        checkObsState();
        return obsParser.period(current.get(index).getTimeslot()).parsePeriod();
    }

    @Override
    public Double getObsValue() throws IOException {
        checkObsState();
        Object result = current.get(index).getValue();
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
}
