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
package internal.util;

import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.util.FreqUtil;
import java.util.function.Function;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class FreqParsers {

    public Frequency parseByTimeFormat(Function<String, String> attributes) {
        String value = attributes.apply(FreqUtil.TIME_FORMAT_CONCEPT);
        return value != null ? FreqUtil.parseByTimeFormat(value) : Frequency.UNDEFINED;
    }

    public Frequency parseByFreqCodeIdIndex(Key.Builder key, int frequencyCodeIdIndex) {
        String frequencyCodeId = key.getItem(frequencyCodeIdIndex);
        if (!frequencyCodeId.isEmpty()) {
            return FreqUtil.parseByFreq(frequencyCodeId);
        }
        return Frequency.UNDEFINED;
    }

}
