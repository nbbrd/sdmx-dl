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

import be.nbb.sdmx.facade.Frequency;
import internal.util.SafeParsers;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public interface SafeParser<T> {

    @Nullable
    T parse(@Nonnull CharSequence input);

    @Nonnull
    default SafeParser<T> or(@Nonnull SafeParser<T> r) {
        return new SafeParsers.Fallback(this, r);
    }

    @Nonnull
    default SafeParser<T> or(@Nonnull T value) {
        return new SafeParsers.Fallback(this, o -> value);
    }

    @Nonnull
    @SuppressWarnings("null")
    static <T> SafeParser<T> onNull() {
        return o -> null;
    }

    @Nonnull
    static SafeParser<LocalDateTime> onDatePattern(@Nonnull String pattern) {
        DateTimeFormatter dateFormat = new DateTimeFormatterBuilder()
                .appendPattern(pattern)
                .parseStrict()
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter(Locale.ROOT);
        return new SafeParsers.OnDateTimeFormatter(dateFormat);
    }

    @Nonnull
    static SafeParser<LocalDateTime> onYearFreqPos(@Nonnull String freqCode, @Nonnegative int freq) {
        return new SafeParsers.YearFreqPos(freqCode, freq);
    }

    @Nonnull
    static SafeParser<LocalDateTime> onStandardFreq(@Nonnull Frequency freq) {
        return SafeParsers.STANDARD_PARSERS.get(freq);
    }

    @Nonnull
    static SafeParser<Double> onDouble() {
        return SafeParsers::doubleOrNull;
    }
}
