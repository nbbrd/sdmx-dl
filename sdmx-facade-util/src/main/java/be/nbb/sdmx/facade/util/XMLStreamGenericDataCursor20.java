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
final class XMLStreamGenericDataCursor20 extends DataCursor {

    private final XMLStreamReader reader;
    private final Key.Builder keyBuilder;
    private final ObsParser obs;

    public XMLStreamGenericDataCursor20(XMLStreamReader reader, Key.Builder keyBuilder) {
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
                            case "Series":
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
            if (reader.getEventType() == XMLStreamReader.START_ELEMENT && "Obs".equals(reader.getLocalName())) {
                parseObs();
                return true;
            }
            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamReader.START_ELEMENT:
                        switch (reader.getLocalName()) {
                            case "Obs":
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
                        case "SeriesKey":
                            parseSeriesKey();
                            break;
                        case "Attributes":
                            parseAttributes();
                            break;
                        case "Obs":
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
                        case "Value":
                            keyBuilder.put(reader.getAttributeValue(null, "concept"), reader.getAttributeValue(null, "value"));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "SeriesKey":
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
                        case "Value":
                            if ("TIME_FORMAT".equals(reader.getAttributeValue(null, "concept"))) {
                                String tmp = reader.getAttributeValue(null, "value");
                                obs.timeFormat(tmp != null ? TimeFormat.parseByTimeFormat(tmp) : TimeFormat.UNDEFINED);
                            }
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Attributes":
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
                        case "Time":
                            obs.withPeriod(reader.getElementText());
                            break;
                        case "ObsValue":
                            obs.withValue(reader.getAttributeValue(null, "value"));
                            break;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "Obs":
                            return;
                    }
                    break;
            }
        }
    }
}
