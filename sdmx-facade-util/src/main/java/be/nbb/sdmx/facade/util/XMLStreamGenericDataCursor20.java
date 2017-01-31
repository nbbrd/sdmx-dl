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
import java.io.Reader;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Philippe Charles
 */
public final class XMLStreamGenericDataCursor20 implements DataCursor {

    @Nonnull
    public static DataCursor genericData20(@Nonnull XMLInputFactory factory, @Nonnull Reader stream, @Nonnull DataStructure dsd) throws IOException {
        try {
            return new XMLStreamGenericDataCursor20(factory.createXMLStreamReader(stream), Key.builder(dsd));
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    private static final String SERIES_ELEMENT = "Series";
    private static final String OBS_ELEMENT = "Obs";
    private static final String TIME_ELEMENT = "Time";
    private static final String OBS_VALUE_ELEMENT = "ObsValue";
    private static final String SERIES_KEY_ELEMENT = "SeriesKey";
    private static final String VALUE_ELEMENT = "Value";
    private static final String ATTRIBUTES_ELEMENT = "Attributes";
    private static final String CONCEPT_ATTRIBUTE = "concept";
    private static final String VALUE_ATTRIBUTE = "value";

    private final XMLStreamReader reader;
    private final Key.Builder keyBuilder;
    private final ObsParser obs;

    XMLStreamGenericDataCursor20(XMLStreamReader reader, Key.Builder keyBuilder) {
        this.reader = reader;
        this.keyBuilder = keyBuilder;
        this.obs = new ObsParser();
    }

    @Override
    public boolean nextSeries() throws IOException {
        try {
            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamReader.START_ELEMENT:
                        switch (reader.getLocalName()) {
                            case SERIES_ELEMENT:
                                parseSeriesHeaders();
                                return true;
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
            if (reader.getEventType() == XMLStreamReader.START_ELEMENT && OBS_ELEMENT.equals(reader.getLocalName())) {
                parseObs();
                return true;
            }
            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamReader.START_ELEMENT:
                        switch (reader.getLocalName()) {
                            case OBS_ELEMENT:
                                parseObs();
                                return true;
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

    private void parseSeriesHeaders() throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case SERIES_KEY_ELEMENT:
                            parseSeriesKey();
                            break;
                        case ATTRIBUTES_ELEMENT:
                            parseAttributes();
                            break;
                        case OBS_ELEMENT:
                            return;
                    }
                    break;
            }
        }
    }

    private void parseSeriesKey() throws XMLStreamException {
        keyBuilder.clear();
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case VALUE_ELEMENT:
                            keyBuilder.put(reader.getAttributeValue(null, CONCEPT_ATTRIBUTE), reader.getAttributeValue(null, VALUE_ATTRIBUTE));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case SERIES_KEY_ELEMENT:
                            return;
                    }
                    break;
            }
        }
    }

    private void parseAttributes() throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case VALUE_ELEMENT:
                            if ("TIME_FORMAT".equals(reader.getAttributeValue(null, CONCEPT_ATTRIBUTE))) {
                                String tmp = reader.getAttributeValue(null, VALUE_ATTRIBUTE);
                                obs.timeFormat(tmp != null ? TimeFormat.parseByTimeFormat(tmp) : TimeFormat.UNDEFINED);
                            }
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case ATTRIBUTES_ELEMENT:
                            return;
                    }
                    break;
            }
        }
    }

    private void parseObs() throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case TIME_ELEMENT:
                            obs.periodString(reader.getElementText());
                            break;
                        case OBS_VALUE_ELEMENT:
                            obs.valueString(reader.getAttributeValue(null, VALUE_ATTRIBUTE));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case OBS_ELEMENT:
                            return;
                    }
                    break;
            }
        }
    }
}
