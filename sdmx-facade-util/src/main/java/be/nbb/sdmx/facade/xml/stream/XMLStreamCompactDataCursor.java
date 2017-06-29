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
package be.nbb.sdmx.facade.xml.stream;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.util.ObsParser;
import be.nbb.sdmx.facade.xml.stream.XMLStreamUtil.Status;
import static be.nbb.sdmx.facade.xml.stream.XMLStreamUtil.Status.CONTINUE;
import static be.nbb.sdmx.facade.xml.stream.XMLStreamUtil.Status.HALT;
import static be.nbb.sdmx.facade.xml.stream.XMLStreamUtil.Status.SUSPEND;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import be.nbb.sdmx.facade.util.FreqParser;

/**
 *
 * @author Philippe Charles
 */
final class XMLStreamCompactDataCursor implements DataCursor {

    private static final String DATASET_ELEMENT = "DataSet";
    private static final String SERIES_ELEMENT = "Series";
    private static final String OBS_ELEMENT = "Obs";

    private final XMLStreamReader reader;
    private final Key.Builder keyBuilder;
    private final AttributesBuilder attributesBuilder;
    private final ObsParser obsParser;
    private final FreqParser freqParser;
    private final String timeDimensionId;
    private final String primaryMeasureId;
    private boolean closed;
    private boolean hasSeries;
    private boolean hasObs;

    XMLStreamCompactDataCursor(XMLStreamReader reader, Key.Builder keyBuilder, ObsParser obsParser, FreqParser freqParser, String timeDimensionId, String primaryMeasureId) {
        this.reader = reader;
        this.keyBuilder = keyBuilder;
        this.attributesBuilder = new AttributesBuilder();
        this.obsParser = obsParser;
        this.freqParser = freqParser;
        this.timeDimensionId = timeDimensionId;
        this.primaryMeasureId = primaryMeasureId;
        this.closed = false;
        this.hasSeries = false;
        this.hasObs = false;
    }

    @Override
    public boolean nextSeries() throws IOException {
        checkState();
        keyBuilder.clear();
        attributesBuilder.clear();
        try {
            return hasSeries = nextWhile(this::onDataSet);
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public boolean nextObs() throws IOException {
        checkSeriesState();
        obsParser.clear();
        try {
            return hasObs = nextWhile(this::onSeriesBody);
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Key getSeriesKey() throws IOException {
        checkSeriesState();
        if (!keyBuilder.isSeries()) {
            throw new IOException("Invalid series key '" + keyBuilder + "'");
        }
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
        return attributesBuilder.getAttribute(key);
    }

    @Override
    public Map<String, String> getSeriesAttributes() throws IOException {
        checkSeriesState();
        return attributesBuilder.build();
    }

    @Override
    public LocalDateTime getObsPeriod() throws IOException {
        checkObsState();
        return obsParser.parsePeriod();
    }

    @Override
    public Double getObsValue() throws IOException {
        checkObsState();
        return obsParser.parseValue();
    }

    @Override
    public void close() throws IOException {
        closed = true;
        try {
            reader.close();
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    private void checkState() throws IOException {
        if (closed) {
            throw new IOException("Cursor closed");
        }
    }

    private void checkSeriesState() throws IOException, IllegalStateException {
        checkState();
        if (!hasSeries) {
            throw new IllegalStateException();
        }
    }

    private void checkObsState() throws IOException, IllegalStateException {
        checkSeriesState();
        if (!hasObs) {
            throw new IllegalStateException();
        }
    }

    private Status onDataSet(boolean start, String localName) throws XMLStreamException {
        if (start) {
            return localName.equals(SERIES_ELEMENT) ? parseSeries() : CONTINUE;
        } else {
            return localName.equals(DATASET_ELEMENT) ? HALT : CONTINUE;
        }
    }

    private Status parseSeries() {
        parserSerieHead();
        obsParser.frequency(freqParser.parse(keyBuilder, attributesBuilder::getAttribute));
        return SUSPEND;
    }

    private void parserSerieHead() {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String id = reader.getAttributeName(i).getLocalPart();
            if (keyBuilder.isDimension(id)) {
                keyBuilder.put(id, reader.getAttributeValue(i));
            } else {
                attributesBuilder.put(id, reader.getAttributeValue(i));
            }
        }
    }

    private Status onSeriesBody(boolean start, String localName) throws XMLStreamException {
        if (start) {
            return localName.equals(OBS_ELEMENT) ? parseObs() : CONTINUE;
        } else {
            return localName.equals(SERIES_ELEMENT) ? HALT : CONTINUE;
        }
    }

    private Status parseObs() {
        obsParser.period(reader.getAttributeValue(null, timeDimensionId));
        obsParser.value(reader.getAttributeValue(null, primaryMeasureId));
        return SUSPEND;
    }

    private boolean nextWhile(XMLStreamUtil.Func func) throws XMLStreamException {
        return XMLStreamUtil.nextWhile(reader, func);
    }
}
