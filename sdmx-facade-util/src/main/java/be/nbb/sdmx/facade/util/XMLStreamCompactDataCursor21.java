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
public final class XMLStreamCompactDataCursor21 extends DataCursor {

    @Nonnull
    public static DataCursor compactData21(@Nonnull XMLInputFactory factory, @Nonnull Reader stream, @Nonnull DataStructure dsd) throws IOException {
        try {
            return new XMLStreamCompactDataCursor21(factory.createXMLStreamReader(stream), Key.builder(dsd), Util.getFrequencyCodeIdIndex(dsd), dsd.getTimeDimensionId(), dsd.getPrimaryMeasureId());
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    private static final String SERIES_ELEMENT = "Series";
    private static final String OBS_ELEMENT = "Obs";

    private final XMLStreamReader reader;
    private final Key.Builder keyBuilder;
    private final int frequencyCodeIdIndex;
    private final ObsParser obs;
    private final String timeDimensionId;
    private final String primaryMeasureId;

    XMLStreamCompactDataCursor21(XMLStreamReader reader, Key.Builder keyBuilder, int frequencyCodeIdIndex, String timeDimensionId, String primaryMeasureId) {
        this.reader = reader;
        this.keyBuilder = keyBuilder;
        this.frequencyCodeIdIndex = frequencyCodeIdIndex;
        this.obs = new ObsParser();
        this.timeDimensionId = timeDimensionId;
        this.primaryMeasureId = primaryMeasureId;
    }

    @Override
    public boolean nextSeries() throws IOException {
        try {
            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamReader.START_ELEMENT:
                        switch (reader.getLocalName()) {
                            case SERIES_ELEMENT:
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
                                return parseObs(reader, obs.clear(), timeDimensionId, primaryMeasureId);
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

    private static boolean readSeriesKey(XMLStreamReader reader, Key.Builder keyBuilder) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            keyBuilder.put(reader.getAttributeName(i).getLocalPart(), reader.getAttributeValue(i));
        }
        return true;
    }

    private static boolean parseObs(XMLStreamReader reader, ObsParser obs, String timeDimensionId, String primaryMeasureId) {
        obs.periodString(reader.getAttributeValue(null, timeDimensionId));
        obs.valueString(reader.getAttributeValue(null, primaryMeasureId));
        return true;
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
