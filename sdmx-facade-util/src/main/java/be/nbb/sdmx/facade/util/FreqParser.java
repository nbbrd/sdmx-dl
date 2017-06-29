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
package be.nbb.sdmx.facade.util;

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dimension;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.Frequency;
import internal.util.FreqParsers;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public interface FreqParser {

    @Nonnull
    Frequency parse(@Nonnull Key.Builder key, @Nonnull Function<String, String> attributes);

    @Nonnull
    static FreqParser sdmx20() {
        return (k, a) -> FreqParsers.parseByTimeFormat(a);
    }

    @Nonnull
    static FreqParser sdmx21(@Nonnull DataStructure dfs) {
        return sdmx21(getFrequencyCodeIdIndex(dfs));
    }

    @Nonnull
    static FreqParser sdmx21(int frequencyCodeIdIndex) {
        return frequencyCodeIdIndex != NO_FREQUENCY_CODE_ID_INDEX
                ? (k, a) -> FreqParsers.parseByFreqCodeIdIndex(k, frequencyCodeIdIndex)
                : (k, a) -> Frequency.UNDEFINED;
    }

    static final int NO_FREQUENCY_CODE_ID_INDEX = -1;

    static int getFrequencyCodeIdIndex(@Nonnull DataStructure dfs) {
        for (Dimension o : dfs.getDimensions()) {
            switch (o.getId()) {
                case FreqUtil.FREQ_CONCEPT:
                case "FREQUENCY":
                    return (o.getPosition() - 1);
            }
        }
        return NO_FREQUENCY_CODE_ID_INDEX;
    }
}
