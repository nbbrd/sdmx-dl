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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import be.nbb.sdmx.facade.util.SafeParser;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SafeParsers {

    public final Map<Frequency, SafeParser> STANDARD_PARSERS = initStandardParsers();

    private Map<Frequency, SafeParser> initStandardParsers() {
        SafeParser yearMonth = SafeParser.onDatePattern("yyyy-MM");
        SafeParser yearMonthDay = SafeParser.onDatePattern("yyyy-MM-dd");

        Map<Frequency, SafeParser> result = new EnumMap<>(Frequency.class);
        result.put(Frequency.ANNUAL, SafeParser.onDatePattern("yyyy").or(SafeParser.onDatePattern("yyyy'-01'")).or(SafeParser.onDatePattern("yyyy'-A1'")));
        result.put(Frequency.HALF_YEARLY, SafeParser.onYearFreqPos("S", 2).or(yearMonth));
        result.put(Frequency.QUARTERLY, SafeParser.onYearFreqPos("Q", 4).or(yearMonth));
        result.put(Frequency.MONTHLY, SafeParser.onYearFreqPos("M", 12).or(yearMonth));
        result.put(Frequency.WEEKLY, yearMonthDay);
        result.put(Frequency.DAILY, yearMonthDay);
        // FIXME: needs other pattern for time
        result.put(Frequency.HOURLY, yearMonthDay);
        result.put(Frequency.DAILY_BUSINESS, yearMonthDay);
        result.put(Frequency.MINUTELY, yearMonthDay);
        result.put(Frequency.UNDEFINED, yearMonth);
        return Collections.unmodifiableMap(result);
    }

    public static final class Fallback<T> implements SafeParser<T> {

        private final SafeParser<T> first;
        private final SafeParser<T> second;

        public Fallback(SafeParser<T> first, SafeParser<T> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public T parse(CharSequence input) {
            T result = first.parse(input);
            return result != null ? result : second.parse(input);
        }
    }

    public static final class OnDateTimeFormatter implements SafeParser<LocalDateTime> {

        private final DateTimeFormatter dateFormat;

        public OnDateTimeFormatter(DateTimeFormatter dateFormat) {
            this.dateFormat = dateFormat;
        }

        @Override
        public LocalDateTime parse(CharSequence input) {
            try {
                return LocalDateTime.parse(input, dateFormat);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    public static final class YearFreqPos implements SafeParser<LocalDateTime> {

        private final Pattern regex;
        private final int freq;

        public YearFreqPos(String freqCode, int freq) {
            this.regex = Pattern.compile("(\\d+)-?" + freqCode + "(\\d+)");
            this.freq = freq;
        }

        @Override
        public LocalDateTime parse(CharSequence input) {
            Matcher m = regex.matcher(input);
            return m.matches() ? toDate(Integer.parseInt(m.group(1)), freq, Integer.parseInt(m.group(2)) - 1) : null;
        }

        private LocalDateTime toDate(int year, int freq, int pos) {
            return ((pos < 0) || (pos >= freq)) ? null : LocalDate.of(year, pos * (12 / freq) + 1, 1).atStartOfDay();
        }
    }

    public Double doubleOrNull(CharSequence input) {
        try {
            return Double.valueOf(input.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
