/*
 * Copyright 2017 National Bank of Belgium
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

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dimension;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.TimeFormat;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
interface TimeFormatParser {

    @Nonnull
    TimeFormat parse(@Nonnull Key.Builder key, @Nonnull AttributesBuilder attributes);

    @Nonnull
    static TimeFormatParser sdmx20() {
        return (k, a) -> {
            String value = a.getAttribute("TIME_FORMAT");
            return value != null ? TimeFormat.parseByTimeFormat(value) : TimeFormat.UNDEFINED;
        };
    }

    @Nonnull
    static TimeFormatParser sdmx21(int frequencyCodeIdIndex) {
        if (frequencyCodeIdIndex != NO_FREQUENCY_CODE_ID_INDEX) {
            return (k, a) -> {
                String frequencyCodeId = k.getItem(frequencyCodeIdIndex);
                if (!frequencyCodeId.isEmpty()) {
                    return TimeFormat.parseByFrequencyCodeId(frequencyCodeId);
                }
                return TimeFormat.UNDEFINED;
            };
        }
        return (k, a) -> TimeFormat.UNDEFINED;
    }

    static final int NO_FREQUENCY_CODE_ID_INDEX = -1;

    static int getFrequencyCodeIdIndex(@Nonnull DataStructure dfs) {
        for (Dimension o : dfs.getDimensions()) {
            switch (o.getId()) {
                case "FREQ":
                case "FREQUENCY":
                    return (o.getPosition() - 1);
            }
        }
        return NO_FREQUENCY_CODE_ID_INDEX;
    }
}
