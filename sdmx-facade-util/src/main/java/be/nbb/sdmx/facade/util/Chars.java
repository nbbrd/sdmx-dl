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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Chars {

    public interface Parser<T> {

        @Nullable
        T parse(@Nonnull CharSequence input);

        @Nonnull
        default Parser<T> or(@Nonnull Parser<T> r) {
            return new Fallback(this, r);
        }

        @Nonnull
        default Parser<T> or(@Nonnull T value) {
            return new Fallback(this, o -> value);
        }

        @Nonnull
        @SuppressWarnings("null")
        static <T> Parser<T> onNull() {
            return o -> null;
        }

        @Nonnull
        static Parser<LocalDateTime> onDatePattern(@Nonnull String pattern) {
            DateTimeFormatter dateFormat = new DateTimeFormatterBuilder()
                    .appendPattern(pattern)
                    .parseStrict()
                    .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                    .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter(Locale.ROOT);
            return new OnDateTimeFormatter(dateFormat);
        }

        @Nonnull
        static Parser<LocalDateTime> onYearFreqPos(@Nonnull String freqCode, @Nonnegative int freq) {
            return new YearFreqPos(freqCode, freq);
        }

        @Nonnull
        static Parser<Double> onStandardDouble() {
            return Chars::doubleOrNull;
        }
    }

    private static final class Fallback<T> implements Parser<T> {

        private final Parser<T> first;
        private final Parser<T> second;

        public Fallback(Parser<T> first, Parser<T> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public T parse(CharSequence input) {
            T result = first.parse(input);
            return result != null ? result : second.parse(input);
        }
    }

    private static final class OnDateTimeFormatter implements Parser<LocalDateTime> {

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

    private static final class YearFreqPos implements Parser<LocalDateTime> {

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

    private Double doubleOrNull(CharSequence input) {
        try {
            return Double.valueOf(input.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
