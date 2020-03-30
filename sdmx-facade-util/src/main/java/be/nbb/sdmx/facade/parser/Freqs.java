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

import be.nbb.sdmx.facade.util.Chars;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dimension;
import be.nbb.sdmx.facade.Frequency;
import static be.nbb.sdmx.facade.Frequency.*;
import be.nbb.sdmx.facade.Key;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Freqs {

    public interface Parser {

        @NonNull
        Frequency parse(Key.@NonNull Builder key, @NonNull Function<String, String> attributes);

        @NonNull
        static Parser sdmx20() {
            return Freqs::parseSdmx20;
        }

        @NonNull
        static Parser sdmx21(@NonNull DataStructure dsd) {
            return of(extractorByIndex(dsd), Freqs::parseByFreq);
        }

        @NonNull
        static Parser sdmx21(int frequencyCodeIdIndex) {
            return of(extractorByIndex(frequencyCodeIdIndex), Freqs::parseByFreq);
        }

        @NonNull
        static Parser of(
                @NonNull BiFunction<Key.Builder, Function<String, String>, String> extractor,
                @NonNull Function<String, Frequency> mapper) {
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

    public final String FREQ_CONCEPT = "FREQ";
    public final String TIME_FORMAT_CONCEPT = "TIME_FORMAT";

    public static final int NO_FREQUENCY_CODE_ID_INDEX = -1;

    public int getFrequencyCodeIdIndex(@NonNull DataStructure dsd) {
        for (Dimension o : dsd.getDimensions()) {
            switch (o.getId()) {
                case FREQ_CONCEPT:
                case "FREQUENCY":
                    return (o.getPosition() - 1);
            }
        }
        return NO_FREQUENCY_CODE_ID_INDEX;
    }

    @NonNull
    public BiFunction<Key.Builder, Function<String, String>, String> extractorByIndex(@NonNull DataStructure dsd) {
        return extractorByIndex(getFrequencyCodeIdIndex(dsd));
    }

    @NonNull
    public BiFunction<Key.Builder, Function<String, String>, String> extractorByIndex(int frequencyCodeIdIndex) {
        return frequencyCodeIdIndex != NO_FREQUENCY_CODE_ID_INDEX
                ? (k, a) -> k.getItem(frequencyCodeIdIndex)
                : (k, a) -> null;
    }

    /**
     *
     * @param code
     * @return
     * @see
     * http://sdmx.org/wp-content/uploads/CL_FREQ_v2.0_update_April_2015.doc
     */
    @NonNull
    public Frequency parseByFreq(@NonNull String code) {
        switch (code.length()) {
            case 0:
                return UNDEFINED;
            case 1:
                return parseByStandardFreq(code.charAt(0));
            default:
                Frequency base = parseByStandardFreq(code.charAt(0));
                return isMultiplier(code.substring(1)) ? base : UNDEFINED;
        }
    }

    private Frequency parseByStandardFreq(char code) {
        switch (code) {
            case 'A':
                return ANNUAL;
            case 'S':
                return HALF_YEARLY;
            case 'Q':
                return QUARTERLY;
            case 'M':
                return MONTHLY;
            case 'W':
                return WEEKLY;
            case 'D':
                return DAILY;
            case 'H':
                return HOURLY;
            case 'B':
                return DAILY_BUSINESS;
            case 'N':
                return MINUTELY;
            default:
                return UNDEFINED;
        }
    }

    private boolean isMultiplier(String input) {
        try {
            return Integer.parseInt(input) > 1;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     *
     * @param code
     * @return
     * @see http://sdmx.org/wp-content/uploads/CL_TIME_FORMAT_1.0_2009.doc
     */
    @NonNull
    public Frequency parseByTimeFormat(@NonNull String code) {
        switch (code) {
            case "P1Y":
                return ANNUAL;
            case "P6M":
                return HALF_YEARLY;
            case "P3M":
                return QUARTERLY;
            case "P1M":
                return MONTHLY;
            case "P7D":
                return WEEKLY;
            case "P1D":
                return DAILY;
            case "PT1M":
                return MINUTELY;
            default:
                return UNDEFINED;
        }
    }

    public static nbbrd.io.text.@NonNull Parser<LocalDateTime> onStandardFreq(@NonNull Frequency freq) {
        return STANDARD_PARSERS.get(freq);
    }

    private final Map<Frequency, nbbrd.io.text.Parser<LocalDateTime>> STANDARD_PARSERS = initStandardParsers();

    private Map<Frequency, nbbrd.io.text.Parser<LocalDateTime>> initStandardParsers() {
        nbbrd.io.text.Parser<LocalDateTime> yearMonth = Chars.onDatePattern("yyyy-MM");
        nbbrd.io.text.Parser yearMonthDay = Chars.onDatePattern("yyyy-MM-dd");

        Map<Frequency, nbbrd.io.text.Parser<LocalDateTime>> result = new EnumMap<>(Frequency.class);
        result.put(ANNUAL, Chars.onDatePattern("yyyy").orElse(Chars.onDatePattern("yyyy'-01'")).orElse(Chars.onDatePattern("yyyy'-A1'")));
        result.put(HALF_YEARLY, Chars.onYearFreqPos("S", 2).orElse(yearMonth));
        result.put(QUARTERLY, Chars.onYearFreqPos("Q", 4).orElse(yearMonth));
        result.put(MONTHLY, Chars.onYearFreqPos("M", 12).orElse(yearMonth));
        result.put(WEEKLY, yearMonthDay);
        result.put(DAILY, yearMonthDay);
        // FIXME: needs other pattern for time
        result.put(HOURLY, yearMonthDay);
        result.put(DAILY_BUSINESS, yearMonthDay);
        result.put(MINUTELY, yearMonthDay);
        result.put(UNDEFINED, yearMonth);
        return Collections.unmodifiableMap(result);
    }

    private Frequency parseSdmx20(Key.Builder key, Function<String, String> attributes) {
        String code = attributes.apply(TIME_FORMAT_CONCEPT);
        return code != null ? parseByTimeFormat(code) : Frequency.UNDEFINED;
    }
}
