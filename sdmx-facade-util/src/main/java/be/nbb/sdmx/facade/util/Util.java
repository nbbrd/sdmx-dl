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

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dimension;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.TimeFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class Util {

    final int NO_FREQUENCY_CODE_ID_INDEX = -1;

    int getFrequencyCodeIdIndex(@Nonnull DataStructure dfs) {
        Dimension dimension = tryFindFreq(dfs.getDimensions());
        return dimension != null ? (dimension.getPosition() - 1) : NO_FREQUENCY_CODE_ID_INDEX;
    }

    private Dimension tryFindFreq(Set<Dimension> list) {
        for (Dimension o : list) {
            switch (o.getId()) {
                case "FREQ":
                case "FREQUENCY":
                    return o;
            }
        }
        return null;
    }

    <K, V> Map<K, V> immutableCopyOf(Map<K, V> map) {
        switch (map.size()) {
            case 0:
                return Collections.emptyMap();
            case 1:
                Map.Entry<K, V> single = map.entrySet().iterator().next();
                return Collections.singletonMap(single.getKey(), single.getValue());
            default:
                return Collections.unmodifiableMap(new HashMap<>(map));
        }
    }

    enum Status {
        HALT, CONTINUE, SUSPEND;
    }

    interface Func {

        Status visitTag(boolean start, String localName) throws XMLStreamException;
    }

    boolean nextWhile(XMLStreamReader reader, Func func) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                switch (func.visitTag(true, reader.getLocalName())) {
                    case CONTINUE:
                        break;
                    case HALT:
                        return false;
                    case SUSPEND:
                        return true;
                }
            } else if (event == XMLStreamReader.END_ELEMENT) {
                switch (func.visitTag(false, reader.getLocalName())) {
                    case CONTINUE:
                        break;
                    case HALT:
                        return false;
                    case SUSPEND:
                        return true;
                }
            }
        }
        return false;
    }

    static final class AttributesBuilder {

        private final Map<String, String> data = new HashMap<>();

        @Nonnull
        AttributesBuilder clear() {
            data.clear();
            return this;
        }

        @Nonnull
        AttributesBuilder put(@Nullable String key, @Nullable String value) {
            if (key != null && value != null) {
                data.put(key, value);
            }
            return this;
        }

        @Nullable
        String getAttribute(@Nonnull String key) {
            return data.get(key);
        }

        @Nonnull
        Map<String, String> build() {
            return immutableCopyOf(data);
        }
    }

    TimeFormat parseTimeFormat20(AttributesBuilder attributesBuilder) {
        String value = attributesBuilder.getAttribute("TIME_FORMAT");
        return value != null ? TimeFormat.parseByTimeFormat(value) : TimeFormat.UNDEFINED;
    }

    TimeFormat parseTimeFormat21(Key.Builder keyBuilder, int frequencyCodeIdIndex) {
        if (frequencyCodeIdIndex != Util.NO_FREQUENCY_CODE_ID_INDEX) {
            String frequencyCodeId = keyBuilder.getItem(frequencyCodeIdIndex);
            if (!frequencyCodeId.isEmpty()) {
                return TimeFormat.parseByFrequencyCodeId(frequencyCodeId);
            }
        }
        return TimeFormat.UNDEFINED;
    }
}
