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
package internal.util.drivers;

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Frequency;
import static be.nbb.sdmx.facade.Frequency.ANNUAL;
import static be.nbb.sdmx.facade.Frequency.HALF_YEARLY;
import static be.nbb.sdmx.facade.Frequency.MONTHLY;
import static be.nbb.sdmx.facade.Frequency.QUARTERLY;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.util.FreqParser;
import be.nbb.sdmx.facade.util.ObsParser;
import be.nbb.sdmx.facade.util.SafeParser;
import be.nbb.sdmx.facade.util.DataFactory;
import be.nbb.sdmx.facade.util.FreqUtil;
import java.time.LocalDateTime;

/**
 * https://www.insee.fr/fr/information/2862759
 *
 * @author Philippe Charles
 */
public final class InseeDataFactory implements DataFactory {

    @Override
    public Key.Builder getKeyBuilder(DataStructure dsd) {
        return Key.builder(dsd);
    }

    @Override
    public FreqParser getFreqParser(DataStructure dsd) {
        return FreqParser.of(FreqUtil.extractorByIndex(dsd), InseeDataFactory::parseInseeFreq);
    }

    @Override
    public ObsParser getObsParser(DataStructure dsd) {
        return new ObsParser(InseeDataFactory::onInseeTimePeriod, SafeParser.onStandardDouble());
    }

    private static Frequency parseInseeFreq(String code) {
        if (code.length() == 1) {
            switch (code.charAt(0)) {
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
            }
        }
        return Frequency.UNDEFINED;
    }

    private static SafeParser<LocalDateTime> onInseeTimePeriod(Frequency freq) {
        switch (freq) {
            case ANNUAL:
                return ANNUAL_PARSER;
            case HALF_YEARLY:
                return HALF_YEARLY_PARSER;
            case QUARTERLY:
                return QUARTERLY_PARSER;
            case MONTHLY:
                return MONTHLY_PARSER;
            default:
                return DEFAULT_PARSER;
        }
    }

    private static final SafeParser<LocalDateTime> ANNUAL_PARSER = SafeParser.onDatePattern("yyyy");
    private static final SafeParser<LocalDateTime> HALF_YEARLY_PARSER = SafeParser.onYearFreqPos("S", 2);
    private static final SafeParser<LocalDateTime> QUARTERLY_PARSER = SafeParser.onYearFreqPos("Q", 4);
    private static final SafeParser<LocalDateTime> MONTHLY_PARSER = SafeParser.onDatePattern("yyyy-MM").or(SafeParser.onYearFreqPos("B", 12));
    private static final SafeParser<LocalDateTime> DEFAULT_PARSER = SafeParser.onNull();
}
