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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
final class DataCursorAdapter implements DataCursor {

    private final Iterator<PortableTimeSeries> data;
    private final ObsParser obs;
    private PortableTimeSeries current;
    private int index;

    DataCursorAdapter(List<PortableTimeSeries> data) {
        this.data = data.iterator();
        this.obs = new ObsParser();
    }

    @Override
    public boolean nextSeries() throws IOException {
        boolean result = data.hasNext();
        if (result) {
            current = data.next();
            obs.setTimeFormat(getTimeFormat(current));
            index = -1;
        }
        return result;
    }

    @Override
    public boolean nextObs() throws IOException {
        index++;
        return index < current.getObservations().size();
    }

    @Override
    public Key getSeriesKey() throws IOException {
        return parseKey(current);
    }

    @Override
    public TimeFormat getSeriesTimeFormat() throws IOException {
        return obs.getTimeFormat();
    }

    @Override
    public String getSeriesAttribute(String key) throws IOException {
        return current.getAttributeValue(key);
    }

    @Override
    public Map<String, String> getSeriesAttributes() throws IOException {
        Map<String, String> result = new HashMap<>();
        current.getAttributes().forEach(o -> {
            int sepIndex = o.indexOf("=");
            if (sepIndex != -1) {
                result.put(o.substring(0, sepIndex), o.substring(sepIndex + 1, o.length()));
            }
        });
        return result;
    }

    @Override
    public Date getObsPeriod() throws IOException {
        return obs.periodString(current.getTimeSlots().get(index)).getPeriod();
    }

    @Override
    public Double getObsValue() throws IOException {
        return current.getObservations().get(index);
    }

    @Override
    public void close() throws IOException {
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

    @Nonnull
    private static Key parseKey(PortableTimeSeries ts) {
        List<String> dimensions = ts.getDimensions();
        if (dimensions.isEmpty()) {
            return Key.ALL;
        }
        String[] result = new String[dimensions.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = dimensions.get(i).split("=")[1];
        }
        return Key.of(result);
    }
    //</editor-fold>
}
