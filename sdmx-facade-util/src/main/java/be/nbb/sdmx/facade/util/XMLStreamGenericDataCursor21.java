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
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.TimeFormat;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Philippe Charles
 */
public final class XMLStreamGenericDataCursor21 extends DataCursor {

    @Nullable
    public static DataCursor genericData21(@Nonnull XMLInputFactory factory, @Nonnull InputStreamReader stream, @Nonnull DataStructure dsd) throws IOException {
        try {
            return new XMLStreamGenericDataCursor21(factory.createXMLStreamReader(stream), Key.builder(dsd), Util.getFrequencyCodeIdIndex(dsd));
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    private static final String SERIES_ELEMENT = "Series";
    private static final String OBS_ELEMENT = "Obs";
    private static final String OBS_VALUE_ELEMENT = "ObsValue";
    private static final String SERIES_KEY_ELEMENT = "SeriesKey";
    private static final String VALUE_ELEMENT = "Value";
    private static final String OBS_DIMENSION_ELEMENT = "ObsDimension";
    private static final String ID_ATTRIBUTE = "id";
    private static final String VALUE_ATTRIBUTE = "value";

    private final XMLStreamReader reader;
    private final Key.Builder keyBuilder;
    private final int frequencyCodeIdIndex;
    private final ObsParser obs;

    XMLStreamGenericDataCursor21(XMLStreamReader reader, Key.Builder keyBuilder, int frequencyCodeIdIndex) {
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
                            case SERIES_KEY_ELEMENT:
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
                            case OBS_ELEMENT:
                                return parseObs(reader, obs.clear());
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        switch (reader.getLocalName()) {
                            case SERIES_ELEMENT:
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
                        case VALUE_ELEMENT:
                            keyBuilder.put(reader.getAttributeValue(null, ID_ATTRIBUTE), reader.getAttributeValue(null, VALUE_ATTRIBUTE));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case SERIES_KEY_ELEMENT:
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
                        case OBS_DIMENSION_ELEMENT:
                            obs.periodString(reader.getAttributeValue(null, VALUE_ATTRIBUTE));
                            break;
                        case OBS_VALUE_ELEMENT:
                            obs.valueString(reader.getAttributeValue(null, VALUE_ATTRIBUTE));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case OBS_ELEMENT:
                            return true;
                    }
                    break;
            }
        }
        return false;
    }

    private static TimeFormat parseTimeFormat(Key.Builder keyBuilder, int frequencyCodeIdIndex) {
        if (frequencyCodeIdIndex != Util.NO_FREQUENCY_CODE_ID_INDEX) {
            String frequencyCodeId = keyBuilder.getItem(frequencyCodeIdIndex);
            if (!frequencyCodeId.isEmpty()) {
                return TimeFormat.parseByFrequencyCodeId(frequencyCodeId);
            }
        }
        return TimeFormat.UNDEFINED;
    }
}
