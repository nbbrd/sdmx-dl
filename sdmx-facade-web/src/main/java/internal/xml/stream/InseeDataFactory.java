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
package internal.xml.stream;

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Frequency;
import static be.nbb.sdmx.facade.Frequency.ANNUAL;
import static be.nbb.sdmx.facade.Frequency.HALF_YEARLY;
import static be.nbb.sdmx.facade.Frequency.MONTHLY;
import static be.nbb.sdmx.facade.Frequency.QUARTERLY;
import static be.nbb.sdmx.facade.Frequency.UNDEFINED;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.util.FreqParser;
import be.nbb.sdmx.facade.util.ObsParser;
import be.nbb.sdmx.facade.util.SafeParser;
import be.nbb.sdmx.facade.xml.stream.SdmxXmlStreams;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * https://www.insee.fr/fr/information/2862759
 *
 * @author Philippe Charles
 */
public final class InseeDataFactory implements Function<DataStructure, SdmxXmlStreams.DataParser> {

    private final Map<Frequency, SafeParser<LocalDateTime>> parsers = Stream.of(Frequency.values()).collect(Collectors.toMap(Function.identity(), o -> getPeriodParser(o)));

    @Override
    public SdmxXmlStreams.DataParser apply(DataStructure dsd) {
        FreqParser freq = getFreqParser(dsd);
        Function<Frequency, SafeParser<LocalDateTime>> period = parsers::get;
        return new SdmxXmlStreams.DataParser(Key.builder(dsd), freq, new ObsParser(period, SafeParser.onDouble()), dsd.getTimeDimensionId(), dsd.getPrimaryMeasureId());
    }

    private static FreqParser getFreqParser(DataStructure dsd) {
        int index = FreqParser.getFrequencyCodeIdIndex(dsd);
        return index != FreqParser.NO_FREQUENCY_CODE_ID_INDEX
                ? (k, a) -> parseByFreq(k, index)
                : (k, a) -> Frequency.UNDEFINED;
    }

    private static Frequency parseByFreq(Key.Builder key, int index) {
        String codeId = key.getItem(index);
        return codeId.length() == 1 ? parseByCode(codeId.charAt(0)) : Frequency.UNDEFINED;
    }

    private static Frequency parseByCode(char code) {
        switch (code) {
            case 'A':
                return ANNUAL;
            case 'S':
                return HALF_YEARLY;
            case 'T':
                return QUARTERLY;
            case 'M':
                return MONTHLY;
            case 'B':
                return MONTHLY;
            default:
                return UNDEFINED;
        }
    }

    private static SafeParser<LocalDateTime> getPeriodParser(Frequency freq) {
        switch (freq) {
            case ANNUAL:
                return SafeParser.onDatePattern("yyyy");
            case HALF_YEARLY:
                return SafeParser.onYearFreqPos("S", 2);
            case QUARTERLY:
                return SafeParser.onYearFreqPos("Q", 4);
            case MONTHLY:
                return SafeParser.onDatePattern("yyyy-MM").or(SafeParser.onYearFreqPos("B", 12));
            default:
                return SafeParser.onNull();
        }
    }
}
