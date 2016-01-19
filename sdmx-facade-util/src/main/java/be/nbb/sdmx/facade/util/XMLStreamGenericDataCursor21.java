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
package be.nbb.sdmx.facade.util;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.TimeFormat;
import java.io.IOException;
import java.util.Date;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Philippe Charles
 */
final class XMLStreamGenericDataCursor21 extends DataCursor {

    public static final int NO_FREQUENCY_CODE_ID_INDEX = -1;

    private final XMLStreamReader reader;
    private final Key.Builder keyBuilder;
    private final int frequencyCodeIdIndex;
    private final ObsParser obs;

    public XMLStreamGenericDataCursor21(XMLStreamReader reader, Key.Builder keyBuilder, int frequencyCodeIdIndex) {
        this.reader = reader;
        this.keyBuilder = keyBuilder;
        this.frequencyCodeIdIndex = frequencyCodeIdIndex;
        this.obs = new ObsParser();
    }

    @Override
    public boolean nextSeries() throws IOException {
        try {
            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamReader.START_ELEMENT:
                        switch (reader.getLocalName()) {
                            case "SeriesKey":
                                if (readSeriesKey(reader, keyBuilder.clear())) {
                                    obs.timeFormat(parseTimeFormat(keyBuilder, frequencyCodeIdIndex));
                                    return true;
                                }
                                return false;
                        }
                        break;
                }
            }
            return false;
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public boolean nextObs() throws IOException {
        try {
            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamReader.START_ELEMENT:
                        switch (reader.getLocalName()) {
                            case "Obs":
                                return parseObs(reader, obs.clear());
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        switch (reader.getLocalName()) {
                            case "Series":
                                return false;
                        }
                        break;
                }
            }
            return false;
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Key getKey() throws IOException {
        return keyBuilder.build();
    }

    @Override
    public TimeFormat getTimeFormat() throws IOException {
        return obs.getTimeFormat();
    }

    @Override
    public Date getPeriod() throws IOException {
        return obs.getPeriod();
    }

    @Override
    public Double getValue() throws IOException {
        return obs.getValue();
    }

    @Override
    public void close() throws IOException {
        try {
            reader.close();
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    private static boolean readSeriesKey(XMLStreamReader reader, Key.Builder keyBuilder) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Value":
                            keyBuilder.put(reader.getAttributeValue(null, "id"), reader.getAttributeValue(null, "value"));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "SeriesKey":
                            return true;
                    }
                    break;
            }
        }
        return false;
    }

    private static boolean parseObs(XMLStreamReader reader, ObsParser obs) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "ObsDimension":
                            obs.withPeriod(reader.getAttributeValue(null, "value"));
                            break;
                        case "ObsValue":
                            obs.withValue(reader.getAttributeValue(null, "value"));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Obs":
                            return true;
                    }
                    break;
            }
        }
        return false;
    }

    private static TimeFormat parseTimeFormat(Key.Builder keyBuilder, int frequencyCodeIdIndex) {
        if (frequencyCodeIdIndex != NO_FREQUENCY_CODE_ID_INDEX) {
            String frequencyCodeId = keyBuilder.getItem(frequencyCodeIdIndex);
            if (frequencyCodeId != null) {
                return TimeFormat.parseByFrequencyCodeId(frequencyCodeId);
            }
        }
        return TimeFormat.UNDEFINED;
    }
}
