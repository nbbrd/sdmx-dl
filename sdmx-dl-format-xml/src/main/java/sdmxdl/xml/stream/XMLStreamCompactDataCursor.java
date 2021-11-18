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
package sdmxdl.xml.stream;

import nbbrd.io.WrappedIOException;
import sdmxdl.DataCursor;
import sdmxdl.Frequency;
import sdmxdl.Key;
import sdmxdl.ext.ObsParser;
import sdmxdl.xml.stream.XMLStreamUtil.Status;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import static sdmxdl.xml.stream.XMLStreamUtil.Status.*;
import static sdmxdl.xml.stream.XMLStreamUtil.isTagMatch;

/**
 * @author Philippe Charles
 */
final class XMLStreamCompactDataCursor implements DataCursor {

    private static final String DATASET_TAG = "DataSet";
    private static final String SERIES_TAG = "Series";
    private static final String OBS_TAG = "Obs";

    private final XMLStreamReader reader;
    private final Closeable onClose;
    private final Key.Builder keyBuilder;
    private final AttributesBuilder seriesAttributes;
    private final ObsParser obsParser;
    private final AttributesBuilder obsAttributes;
    private final String timeDimensionId;
    private final String primaryMeasureId;
    private boolean closed;
    private boolean hasSeries;
    private boolean hasObs;

    XMLStreamCompactDataCursor(XMLStreamReader reader, Closeable onClose, Key.Builder keyBuilder, ObsParser obsParser, String timeDimensionId, String primaryMeasureId) {
        if (!StaxUtil.isNotNamespaceAware(reader)) {
            throw new IllegalArgumentException("Using XMLStreamReader with namespace awareness");
        }
        this.reader = reader;
        this.onClose = onClose;
        this.keyBuilder = keyBuilder;
        this.seriesAttributes = new AttributesBuilder();
        this.obsParser = obsParser;
        this.obsAttributes = new AttributesBuilder();
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
        seriesAttributes.clear();
        try {
            return hasSeries = nextWhile(this::onDataSet);
        } catch (XMLStreamException ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public boolean nextObs() throws IOException {
        checkSeriesState();
        obsParser.clear();
        obsAttributes.clear();
        try {
            return hasObs = nextWhile(this::onSeriesBody);
        } catch (XMLStreamException ex) {
            throw WrappedIOException.wrap(ex);
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
        return seriesAttributes.getAttribute(key);
    }

    @Override
    public Map<String, String> getSeriesAttributes() throws IOException {
        checkSeriesState();
        return seriesAttributes.build();
    }

    @Override
    public LocalDateTime getObsPeriod() throws IOException {
        checkObsState();
        return obsParser.parsePeriod(obsAttributes::getAttribute);
    }

    @Override
    public Double getObsValue() throws IOException {
        checkObsState();
        return obsParser.parseValue();
    }

    @Override
    public Map<String, String> getObsAttributes() throws IOException {
        checkObsState();
        return obsAttributes.build();
    }

    @Override
    public void close() throws IOException {
        closed = true;
        StaxUtil.closeBoth(reader, onClose);
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

    private Status onDataSet(boolean start, String localName) {
        if (start) {
            return isTagMatch(localName, SERIES_TAG) ? parseSeries() : CONTINUE;
        } else {
            return isTagMatch(localName, DATASET_TAG) ? HALT : CONTINUE;
        }
    }

    private Status parseSeries() {
        parserSerieHead();
        obsParser.head(keyBuilder, seriesAttributes::getAttribute);
        return SUSPEND;
    }

    private void parserSerieHead() {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String id = reader.getAttributeName(i).getLocalPart();
            if (keyBuilder.isDimension(id)) {
                keyBuilder.put(id, reader.getAttributeValue(i));
            } else {
                seriesAttributes.put(id, reader.getAttributeValue(i));
            }
        }
    }

    private Status onSeriesBody(boolean start, String localName) {
        if (start) {
            return isTagMatch(localName, OBS_TAG) ? parseObs() : CONTINUE;
        } else {
            return isTagMatch(localName, SERIES_TAG) ? HALT : CONTINUE;
        }
    }

    private Status parseObs() {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);
            if (timeDimensionId.equals(name)) {
                obsParser.period(value);
            } else if (primaryMeasureId.equals(name)) {
                obsParser.value(value);
            } else {
                obsAttributes.put(name, value);
            }
        }
        return SUSPEND;
    }

    private boolean nextWhile(XMLStreamUtil.TagVisitor func) throws XMLStreamException {
        return XMLStreamUtil.nextWhile(reader, func);
    }
}
