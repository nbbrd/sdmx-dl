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
import be.nbb.sdmx.facade.parser.Freqs;
import be.nbb.sdmx.facade.util.Chars;
import be.nbb.sdmx.facade.parser.spi.SdmxDialect;
import java.time.LocalDateTime;
import org.openide.util.lookup.ServiceProvider;

/**
 * https://www.insee.fr/fr/information/2862759
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxDialect.class)
public final class InseeDialect implements SdmxDialect {

    @Override
    public String getName() {
        return "INSEE2017";
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public Freqs.Parser getFreqParser(DataStructure dsd) {
        return Freqs.Parser.of(Freqs.extractorByIndex(dsd), InseeDialect::parseInseeFreq);
    }

    @Override
    public Chars.Parser<LocalDateTime> getPeriodParser(Frequency freq) {
        return onInseeTimePeriod(freq);
    }

    @Override
    public Chars.Parser<Double> getValueParser() {
        return Chars.Parser.onStandardDouble();
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
                    // FIXME: define new freq?
                    return MONTHLY;
            }
        }
        return Frequency.UNDEFINED;
    }

    private static Chars.Parser<LocalDateTime> onInseeTimePeriod(Frequency freq) {
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

    private static final Chars.Parser<LocalDateTime> ANNUAL_PARSER = Chars.Parser.onDatePattern("yyyy");
    private static final Chars.Parser<LocalDateTime> HALF_YEARLY_PARSER = Chars.Parser.onYearFreqPos("S", 2);
    private static final Chars.Parser<LocalDateTime> QUARTERLY_PARSER = Chars.Parser.onYearFreqPos("Q", 4);
    private static final Chars.Parser<LocalDateTime> MONTHLY_PARSER = Chars.Parser.onDatePattern("yyyy-MM").or(Chars.Parser.onYearFreqPos("B", 12));
    private static final Chars.Parser<LocalDateTime> DEFAULT_PARSER = Chars.Parser.onNull();
}
