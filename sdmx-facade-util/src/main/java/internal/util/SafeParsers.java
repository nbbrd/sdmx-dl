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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import be.nbb.sdmx.facade.util.SafeParser;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SafeParsers {

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
