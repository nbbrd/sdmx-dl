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
package be.nbb.sdmx.facade.parser;

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.Frequency;
import internal.parser.FreqParsers;
import java.util.function.BiFunction;
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
        return FreqParsers::parseSdmx20;
    }

    @Nonnull
    static FreqParser sdmx21(@Nonnull DataStructure dsd) {
        return of(FreqUtil.extractorByIndex(dsd), FreqUtil::parseByFreq);
    }

    @Nonnull
    static FreqParser sdmx21(int frequencyCodeIdIndex) {
        return of(FreqUtil.extractorByIndex(frequencyCodeIdIndex), FreqUtil::parseByFreq);
    }

    @Nonnull
    static FreqParser of(
            @Nonnull BiFunction<Key.Builder, Function<String, String>, String> extractor,
            @Nonnull Function<String, Frequency> mapper) {
        return (k, a) -> {
            String code = extractor.apply(k, a);
            if (code == null) {
                return Frequency.UNDEFINED;
            }
            Frequency freq = mapper.apply(code);
            if (freq == null) {
                return Frequency.UNDEFINED;
            }
            return freq;
        };
    }
}
