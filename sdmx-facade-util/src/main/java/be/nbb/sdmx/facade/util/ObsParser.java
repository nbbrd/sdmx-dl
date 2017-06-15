/*
 * Copyright 2015 National Bank of Belgium
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

import be.nbb.sdmx.facade.TimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
public final class ObsParser {

    private DateParser periodParser;
    private TimeFormat timeFormat;
    private String period;
    private String value;

    public ObsParser() {
        setTimeFormat(TimeFormat.UNDEFINED);
    }

    @Nonnull
    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    @Nonnull
    public void setTimeFormat(@Nonnull TimeFormat timeFormat) {
        if (this.timeFormat != timeFormat) {
            this.timeFormat = timeFormat;
            this.periodParser = PARSERS.get(timeFormat);
        }
    }

    @Nonnull
    public ObsParser clear() {
        this.period = null;
        this.value = null;
        return this;
    }

    @Nonnull
    public ObsParser periodString(@Nullable String period) {
        this.period = period;
        return this;
    }

    @Nonnull
    public ObsParser valueString(@Nullable String value) {
        this.value = value;
        return this;
    }

    @Nullable
    public LocalDateTime getPeriod() {
        return period != null ? periodParser.parse(period) : null;
    }

    @Nullable
    public Double getValue() {
        return value != null ? Double.parseDouble(value) : null;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private interface DateParser {

        @Nullable
        LocalDateTime parse(@Nonnull CharSequence input);

        @Nonnull
        @SuppressWarnings("null")
        default DateParser or(@Nonnull DateParser r) {
            return o -> {
                LocalDateTime result = parse(o);
                return result != null ? result : r.parse(o);
            };
        }
    }

    private static final Map<TimeFormat, DateParser> PARSERS = initParsers();

    private static Map<TimeFormat, DateParser> initParsers() {
        DateParser yearMonth = onPattern("yyyy-MM");
        DateParser yearMonthDay = onPattern("yyyy-MM-dd");

        Map<TimeFormat, DateParser> result = new EnumMap<>(TimeFormat.class);
        result.put(TimeFormat.YEARLY, onPattern("yyyy").or(onPattern("yyyy'-01'")).or(onPattern("yyyy'-A1'")));
        result.put(TimeFormat.HALF_YEARLY, YearFreqPosParser.S.or(yearMonth));
        result.put(TimeFormat.QUADRI_MONTHLY, YearFreqPosParser.T.or(yearMonth));
        result.put(TimeFormat.QUARTERLY, YearFreqPosParser.Q.or(yearMonth));
        result.put(TimeFormat.MONTHLY, YearFreqPosParser.M.or(yearMonth));
        result.put(TimeFormat.WEEKLY, yearMonthDay);
        result.put(TimeFormat.DAILY, yearMonthDay);
        // FIXME: needs other pattern for time
        result.put(TimeFormat.HOURLY, yearMonthDay);
        result.put(TimeFormat.MINUTELY, yearMonthDay);
        result.put(TimeFormat.UNDEFINED, yearMonth);
        return result;
    }

    @Nonnull
    @SuppressWarnings("null")
    private static DateParser onPattern(@Nonnull String datePattern) {
        DateTimeFormatter dateFormat = new DateTimeFormatterBuilder()
                .appendPattern(datePattern)
                .parseStrict()
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter(Locale.ROOT);
        return o -> {
            try {
                return LocalDateTime.parse(o, dateFormat);
            } catch (DateTimeParseException ex) {
                return null;
            }
        };
    }

    private enum YearFreqPosParser implements DateParser {

        Q("(\\d+)-?Q(\\d+)", 4),
        M("(\\d+)-?M(\\d+)", 12),
        Y("(\\d+)-?Y(\\d+)", 1),
        S("(\\d+)-?S(\\d+)", 2),
        T("(\\d+)-?T(\\d+)", 3);

        private final Pattern regex;
        private final int freq;

        private YearFreqPosParser(String regex, int freq) {
            this.regex = Pattern.compile(regex);
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
    //</editor-fold>
}
