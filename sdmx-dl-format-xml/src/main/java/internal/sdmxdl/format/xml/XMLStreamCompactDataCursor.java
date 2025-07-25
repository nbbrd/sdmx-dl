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
package internal.sdmxdl.format.xml;

import lombok.NonNull;
import nbbrd.io.WrappedIOException;
import org.jspecify.annotations.Nullable;
import sdmxdl.Key;
import sdmxdl.format.DataCursor;
import sdmxdl.format.ObsParser;
import sdmxdl.format.time.ObservationalTimePeriod;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * @author Philippe Charles
 */
public final class XMLStreamCompactDataCursor implements DataCursor {

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

    public XMLStreamCompactDataCursor(XMLStreamReader reader, Closeable onClose, Key.Builder keyBuilder, ObsParser obsParser, String timeDimensionId, String primaryMeasureId) {
        if (!XMLStreamUtil.isNotNamespaceAware(reader)) {
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
    public @NonNull Key getSeriesKey() throws IOException {
        checkSeriesState();
        if (!keyBuilder.isSeries()) {
            throw new IOException("Invalid series key '" + keyBuilder + "'");
        }
        return keyBuilder.build();
    }

    @Override
    public String getSeriesAttribute(@NonNull String key) throws IOException {
        checkSeriesState();
        return seriesAttributes.getAttribute(key);
    }

    @Override
    public @NonNull Map<String, String> getSeriesAttributes() throws IOException {
        checkSeriesState();
        return seriesAttributes.build();
    }

    @Override
    public ObservationalTimePeriod getObsPeriod() throws IOException {
        checkObsState();
        return obsParser.parsePeriod();
    }

    @Override
    public Double getObsValue() throws IOException {
        checkObsState();
        return obsParser.parseValue();
    }

    @Override
    public @NonNull Map<String, String> getObsAttributes() throws IOException {
        checkObsState();
        return obsAttributes.build();
    }

    @Override
    public @Nullable String getObsAttribute(@NonNull String key) throws IOException, IllegalStateException {
        checkObsState();
        return obsAttributes.getAttribute(key);
    }

    @Override
    public void close() throws IOException {
        closed = true;
        XMLStreamUtil.closeBoth(reader, onClose);
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

    private XMLStreamUtil.Status onDataSet(boolean start, String localName) {
        if (start) {
            return XMLStreamUtil.isTagMatch(localName, SERIES_TAG) ? parseSeries() : XMLStreamUtil.Status.CONTINUE;
        } else {
            return XMLStreamUtil.isTagMatch(localName, DATASET_TAG) ? XMLStreamUtil.Status.HALT : XMLStreamUtil.Status.CONTINUE;
        }
    }

    private XMLStreamUtil.Status parseSeries() {
        if (isValidSeriesHead()) {
            parserSeriesHead();
            return XMLStreamUtil.Status.SUSPEND;
        }
        return XMLStreamUtil.Status.CONTINUE;
    }

    private boolean isValidSeriesHead() {
        return reader.getAttributeCount() > 0;
    }

    private void parserSeriesHead() {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String id = reader.getAttributeName(i).getLocalPart();
            if (keyBuilder.isDimension(id)) {
                keyBuilder.put(id, reader.getAttributeValue(i));
            } else {
                seriesAttributes.put(id, reader.getAttributeValue(i));
            }
        }
    }

    private XMLStreamUtil.Status onSeriesBody(boolean start, String localName) {
        if (start) {
            return XMLStreamUtil.isTagMatch(localName, OBS_TAG) ? parseObs() : XMLStreamUtil.Status.CONTINUE;
        } else {
            return XMLStreamUtil.isTagMatch(localName, SERIES_TAG) ? XMLStreamUtil.Status.HALT : XMLStreamUtil.Status.CONTINUE;
        }
    }

    private XMLStreamUtil.Status parseObs() {
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
        return XMLStreamUtil.Status.SUSPEND;
    }

    private boolean nextWhile(XMLStreamUtil.TagVisitor func) throws XMLStreamException {
        return XMLStreamUtil.nextWhile(reader, func);
    }
}
