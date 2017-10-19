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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
final class XMLStreamGenericDataCursor implements DataCursor {

    static XMLStreamGenericDataCursor sdmx20(XMLStreamReader reader, Key.Builder keyBuilder, ObsParser obsParser, FreqParser freqParser) {
        return new XMLStreamGenericDataCursor(reader, keyBuilder, obsParser, freqParser, SeriesHeadParser.SDMX20);
    }

    static XMLStreamGenericDataCursor sdmx21(XMLStreamReader reader, Key.Builder keyBuilder, ObsParser obsParser, FreqParser freqParser) {
        return new XMLStreamGenericDataCursor(reader, keyBuilder, obsParser, freqParser, SeriesHeadParser.SDMX21);
    }

    private static final String DATASET_ELEMENT = "DataSet";
    private static final String SERIES_ELEMENT = "Series";
    private static final String OBS_ELEMENT = "Obs";
    private static final String OBS_VALUE_ELEMENT = "ObsValue";
    private static final String SERIES_KEY_ELEMENT = "SeriesKey";
    private static final String ATTRIBUTES_ELEMENT = "Attributes";
    private static final String VALUE_ELEMENT = "Value";
    private static final String VALUE_ATTRIBUTE = "value";

    private final XMLStreamReader reader;
    private final Key.Builder keyBuilder;
    private final AttributesBuilder attributesBuilder;
    private final ObsParser obsParser;
    private final FreqParser freqParser;
    private final SeriesHeadParser headParser;
    private boolean closed;
    private boolean hasSeries;
    private boolean hasObs;

    private XMLStreamGenericDataCursor(XMLStreamReader reader, Key.Builder keyBuilder, ObsParser obsParser, FreqParser freqParser, SeriesHeadParser headParser) {
        this.reader = reader;
        this.keyBuilder = keyBuilder;
        this.attributesBuilder = new AttributesBuilder();
        this.obsParser = obsParser;
        this.freqParser = freqParser;
        this.headParser = headParser;
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
            if (isCurrentElementStartOfObs()) {
                parseObs();
                return hasObs = true;
            }
            if (isCurrentElementEnfOfSeries()) {
                return hasObs = false;
            }
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

    private Status parseSeries() throws XMLStreamException {
        nextWhile(this::onSeriesHead);
        obsParser.frequency(freqParser.parse(keyBuilder, attributesBuilder::getAttribute));
        return SUSPEND;
    }

    private Status onSeriesHead(boolean start, String localName) throws XMLStreamException {
        if (start) {
            switch (localName) {
                case SERIES_KEY_ELEMENT:
                    return parseSeriesKey();
                case ATTRIBUTES_ELEMENT:
                    return parseAttributes();
                case OBS_ELEMENT:
                    return HALT;
                default:
                    return CONTINUE;
            }
        } else {
            return localName.equals(SERIES_ELEMENT) ? HALT : CONTINUE;
        }
    }

    private Status parseSeriesKey() throws XMLStreamException {
        nextWhile(this::onSeriesKey);
        return CONTINUE;
    }

    private Status parseAttributes() throws XMLStreamException {
        nextWhile(this::onAttributes);
        return CONTINUE;
    }

    private Status onSeriesKey(boolean start, String localName) throws XMLStreamException {
        if (start) {
            return localName.equals(VALUE_ELEMENT) ? parseSeriesKeyValue() : CONTINUE;
        } else {
            return localName.equals(SERIES_KEY_ELEMENT) ? HALT : CONTINUE;
        }
    }

    private Status onAttributes(boolean start, String localName) throws XMLStreamException {
        if (start) {
            return localName.equals(VALUE_ELEMENT) ? parseAttributesValue() : CONTINUE;
        } else {
            return localName.equals(ATTRIBUTES_ELEMENT) ? HALT : CONTINUE;
        }
    }

    private Status parseSeriesKeyValue() throws XMLStreamException {
        headParser.parseValueElement(reader, keyBuilder::put);
        return CONTINUE;
    }

    private Status parseAttributesValue() throws XMLStreamException {
        headParser.parseValueElement(reader, attributesBuilder::put);
        return CONTINUE;
    }

    private boolean isCurrentElementStartOfObs() {
        return reader.getEventType() == XMLStreamReader.START_ELEMENT && OBS_ELEMENT.equals(reader.getLocalName());
    }

    private boolean isCurrentElementEnfOfSeries() {
        return reader.getEventType() == XMLStreamReader.END_ELEMENT && SERIES_ELEMENT.equals(reader.getLocalName());
    }

    private Status onSeriesBody(boolean start, String localName) throws XMLStreamException {
        if (start) {
            return localName.equals(OBS_ELEMENT) ? parseObs() : CONTINUE;
        } else {
            return localName.equals(SERIES_ELEMENT) ? HALT : CONTINUE;
        }
    }

    private Status onObs(boolean start, String localName) throws XMLStreamException {
        if (start) {
            if (localName.equals(headParser.getTimeELement())) {
                return parseObsTime();
            }
            return localName.equals(OBS_VALUE_ELEMENT) ? parseObsValue() : CONTINUE;
        } else {
            return localName.equals(OBS_ELEMENT) ? HALT : CONTINUE;
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
        obsParser.value(reader.getAttributeValue(null, VALUE_ATTRIBUTE));
        return CONTINUE;
    }

    private boolean nextWhile(XMLStreamUtil.Func func) throws XMLStreamException {
        return XMLStreamUtil.nextWhile(reader, func);
    }

    private enum SeriesHeadParser {

        SDMX20 {
            @Override
            public void parseValueElement(XMLStreamReader r, BiConsumer<String, String> c) throws XMLStreamException {
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
            public void parseValueElement(XMLStreamReader r, BiConsumer<String, String> c) throws XMLStreamException {
                c.accept(r.getAttributeValue(null, "id"), r.getAttributeValue(null, "value"));
            }

            @Override
            public void parseTimeElement(XMLStreamReader r, Consumer<String> c) throws XMLStreamException {
                c.accept(r.getAttributeValue(null, "value"));
            }

            @Override
            public String getTimeELement() {
                return "ObsDimension";
            }
        };

        abstract void parseValueElement(@Nonnull XMLStreamReader r, @Nonnull BiConsumer<String, String> c) throws XMLStreamException;

        abstract void parseTimeElement(@Nonnull XMLStreamReader r, @Nonnull Consumer<String> c) throws XMLStreamException;

        @Nonnull
        abstract String getTimeELement();
    }
}
