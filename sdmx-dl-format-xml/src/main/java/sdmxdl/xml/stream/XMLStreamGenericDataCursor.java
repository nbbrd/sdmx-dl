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
import org.checkerframework.checker.nullness.qual.NonNull;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static sdmxdl.xml.stream.XMLStreamUtil.Status.*;
import static sdmxdl.xml.stream.XMLStreamUtil.isTagMatch;

/**
 * @author Philippe Charles
 */
final class XMLStreamGenericDataCursor implements DataCursor {

    static XMLStreamGenericDataCursor sdmx20(XMLStreamReader reader, Closeable onClose, Key.Builder keyBuilder, ObsParser obsParser) {
        return new XMLStreamGenericDataCursor(reader, onClose, keyBuilder, obsParser, SeriesHeadParser.SDMX20);
    }

    static XMLStreamGenericDataCursor sdmx21(XMLStreamReader reader, Closeable onClose, Key.Builder keyBuilder, ObsParser obsParser) {
        return new XMLStreamGenericDataCursor(reader, onClose, keyBuilder, obsParser, SeriesHeadParser.SDMX21);
    }

    private static final String DATASET_TAG = "DataSet";
    private static final String SERIES_TAG = "Series";
    private static final String OBS_TAG = "Obs";
    private static final String OBS_VALUE_TAG = "ObsValue";
    private static final String SERIES_KEY_TAG = "SeriesKey";
    private static final String ATTRIBUTES_TAG = "Attributes";
    private static final String VALUE_TAG = "Value";
    private static final String VALUE_ATTR = "value";

    private final XMLStreamReader reader;
    private final Closeable onClose;
    private final Key.Builder keyBuilder;
    private final AttributesBuilder seriesAttributes;
    private final ObsParser obsParser;
    private final AttributesBuilder obsAttributes;
    private final SeriesHeadParser headParser;
    private boolean closed;
    private boolean hasSeries;
    private boolean hasObs;

    private XMLStreamGenericDataCursor(XMLStreamReader reader, Closeable onClose, Key.Builder keyBuilder, ObsParser obsParser, SeriesHeadParser headParser) {
        if (!StaxUtil.isNotNamespaceAware(reader)) {
            throw new IllegalArgumentException("Using XMLStreamReader with namespace awareness");
        }
        this.reader = reader;
        this.onClose = onClose;
        this.keyBuilder = keyBuilder;
        this.seriesAttributes = new AttributesBuilder();
        this.obsParser = obsParser;
        this.obsAttributes = new AttributesBuilder();
        this.headParser = headParser;
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
            if (isCurrentElementStartOfObs()) {
                parseObs();
                return hasObs = true;
            }
            if (isCurrentElementEnfOfSeries()) {
                return hasObs = false;
            }
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
        return obsParser.parsePeriod();
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

    private Status onDataSet(boolean start, String localName) throws XMLStreamException {
        if (start) {
            return isTagMatch(localName, SERIES_TAG) ? parseSeries() : CONTINUE;
        } else {
            return isTagMatch(localName, DATASET_TAG) ? HALT : CONTINUE;
        }
    }

    private Status parseSeries() throws XMLStreamException {
        nextWhile(this::onSeriesHead);
        obsParser.frequency(keyBuilder, seriesAttributes::getAttribute);
        return SUSPEND;
    }

    private Status onSeriesHead(boolean start, String localName) throws XMLStreamException {
        if (start) {
            if (isTagMatch(localName, SERIES_KEY_TAG)) {
                return parseSeriesKey();
            } else if (isTagMatch(localName, ATTRIBUTES_TAG)) {
                return parseAttributes(seriesAttributes);
            } else if (isTagMatch(localName, OBS_TAG)) {
                return HALT;
            } else {
                return CONTINUE;
            }
        } else {
            return isTagMatch(localName, SERIES_TAG) ? HALT : CONTINUE;
        }
    }

    private Status parseSeriesKey() throws XMLStreamException {
        nextWhile(this::onSeriesKey);
        return CONTINUE;
    }

    private Status parseAttributes(AttributesBuilder builder) throws XMLStreamException {
        nextWhile((start, localName) -> onAttributes(start, localName, builder));
        return CONTINUE;
    }

    private Status onSeriesKey(boolean start, String localName) throws XMLStreamException {
        if (start) {
            return isTagMatch(localName, VALUE_TAG) ? parseSeriesKeyValue() : CONTINUE;
        } else {
            return isTagMatch(localName, SERIES_KEY_TAG) ? HALT : CONTINUE;
        }
    }

    private Status onAttributes(boolean start, String localName, AttributesBuilder builder) throws XMLStreamException {
        if (start) {
            return isTagMatch(localName, VALUE_TAG) ? parseAttributesValue(builder) : CONTINUE;
        } else {
            return isTagMatch(localName, ATTRIBUTES_TAG) ? HALT : CONTINUE;
        }
    }

    private Status parseSeriesKeyValue() throws XMLStreamException {
        headParser.parseValueElement(reader, keyBuilder::put);
        return CONTINUE;
    }

    private Status parseAttributesValue(AttributesBuilder builder) throws XMLStreamException {
        headParser.parseValueElement(reader, builder::put);
        return CONTINUE;
    }

    private boolean isCurrentElementStartOfObs() {
        return reader.isStartElement() && isTagMatch(reader.getLocalName(), OBS_TAG);
    }

    private boolean isCurrentElementEnfOfSeries() {
        return reader.isEndElement() && isTagMatch(reader.getLocalName(), SERIES_TAG);
    }

    private Status onSeriesBody(boolean start, String localName) throws XMLStreamException {
        if (start) {
            return isTagMatch(localName, OBS_TAG) ? parseObs() : CONTINUE;
        } else {
            return isTagMatch(localName, SERIES_TAG) ? HALT : CONTINUE;
        }
    }

    private Status onObs(boolean start, String localName) throws XMLStreamException {
        if (start) {
            if (isTagMatch(localName, headParser.getTimeELement())) {
                return parseObsTime();
            } else if (isTagMatch(localName, ATTRIBUTES_TAG)) {
                return parseAttributes(obsAttributes);
            } else if (isTagMatch(localName, OBS_VALUE_TAG)) {
                return parseObsValue();
            } else {
                return CONTINUE;
            }
        } else {
            return isTagMatch(localName, OBS_TAG) ? HALT : CONTINUE;
        }
    }

    private Status parseObs() throws XMLStreamException {
        nextWhile(this::onObs);
        return SUSPEND;
    }

    private Status parseObsTime() throws XMLStreamException {
        headParser.parseTimeElement(reader, obsParser::period);
        return CONTINUE;
    }

    private Status parseObsValue() {
        obsParser.value(reader.getAttributeValue(null, VALUE_ATTR));
        return CONTINUE;
    }

    private boolean nextWhile(XMLStreamUtil.TagVisitor func) throws XMLStreamException {
        return XMLStreamUtil.nextWhile(reader, func);
    }

    private enum SeriesHeadParser {

        SDMX20 {
            @Override
            public void parseValueElement(XMLStreamReader r, BiConsumer<String, String> c) {
                c.accept(r.getAttributeValue(null, "concept"), r.getAttributeValue(null, "value"));
            }

            @Override
            public void parseTimeElement(XMLStreamReader r, Consumer<String> c) throws XMLStreamException {
                c.accept(r.getElementText());
            }

            @Override
            public String getTimeELement() {
                return "Time";
            }
        },
        SDMX21 {
            @Override
            public void parseValueElement(XMLStreamReader r, BiConsumer<String, String> c) {
                c.accept(r.getAttributeValue(null, "id"), r.getAttributeValue(null, "value"));
            }

            @Override
            public void parseTimeElement(XMLStreamReader r, Consumer<String> c) {
                c.accept(r.getAttributeValue(null, "value"));
            }

            @Override
            public String getTimeELement() {
                return "ObsDimension";
            }
        };

        abstract void parseValueElement(@NonNull XMLStreamReader r, @NonNull BiConsumer<String, String> c) throws XMLStreamException;

        abstract void parseTimeElement(@NonNull XMLStreamReader r, @NonNull Consumer<String> c) throws XMLStreamException;

        @NonNull
        abstract String getTimeELement();
    }
}
