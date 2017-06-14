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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
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
            this.periodParser = getParser(timeFormat);
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
    public Date getPeriod() {
        return period != null ? periodParser.parse(period) : null;
    }

    @Nullable
    public Double getValue() {
        return value != null ? Double.parseDouble(value) : null;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private interface DateParser {

        @Nullable
        Date parse(@Nonnull CharSequence input);

        @Nonnull
        @SuppressWarnings("null")
        default DateParser or(@Nonnull DateParser r) {
            DateParser l = this;
            return o -> {
                Date result = l.parse(o);
                return result != null ? result : r.parse(o);
            };
        }
    }

    @Nonnull
    private static DateParser getParser(@Nonnull TimeFormat format) {
        switch (format) {
            case YEARLY:
                return onStrictDatePattern("yyyy").or(onStrictDatePattern("yyyy'-01'")).or(onStrictDatePattern("yyyy'-A1'"));
            case HALF_YEARLY:
                return YearFreqPosParser.s().or(onStrictDatePattern("yyyy-MM"));
            case QUADRI_MONTHLY:
                return YearFreqPosParser.t().or(onStrictDatePattern("yyyy-MM"));
            case QUARTERLY:
                return YearFreqPosParser.q().or(onStrictDatePattern("yyyy-MM"));
            case MONTHLY:
                return YearFreqPosParser.m().or(onStrictDatePattern("yyyy-MM"));
            case WEEKLY:
                return onStrictDatePattern("yyyy-MM-dd");
            case DAILY:
                return onStrictDatePattern("yyyy-MM-dd");
            case HOURLY:
                return onStrictDatePattern("yyyy-MM-dd");
            case MINUTELY:
                return onStrictDatePattern("yyyy-MM-dd");
            default:
                return onStrictDatePattern("yyyy-MM");
        }
    }

    @Nonnull
    @SuppressWarnings("null")
    private static DateParser onStrictDatePattern(@Nonnull String datePattern) {
        DateFormat dateFormat = new SimpleDateFormat(datePattern, Locale.ROOT);
        dateFormat.setLenient(false);
        return o -> {
            try {
                String inputAsString = o.toString();
                Date result = dateFormat.parse(inputAsString);
                return result != null && inputAsString.equals(dateFormat.format(result)) ? result : null;
            } catch (ParseException ex) {
                return null;
            }
        };
    }

    private static final class YearFreqPosParser implements DateParser {

        private static final Pattern Q = Pattern.compile("(\\d+)-?Q(\\d+)");
        private static final Pattern M = Pattern.compile("(\\d+)-?M(\\d+)");
        private static final Pattern Y = Pattern.compile("(\\d+)-?Y(\\d+)");
        private static final Pattern S = Pattern.compile("(\\d+)-?S(\\d+)");
        private static final Pattern T = Pattern.compile("(\\d+)-?T(\\d+)");

        public static YearFreqPosParser q() {
            return new YearFreqPosParser(Q, 4);
        }

        public static YearFreqPosParser m() {
            return new YearFreqPosParser(M, 12);
        }

        public static YearFreqPosParser y() {
            return new YearFreqPosParser(Y, 1);
        }

        public static YearFreqPosParser s() {
            return new YearFreqPosParser(S, 2);
        }

        public static YearFreqPosParser t() {
            return new YearFreqPosParser(T, 3);
        }

        private final Pattern regex;
        private final int freq;
        private final Calendar cal;

        private YearFreqPosParser(Pattern regex, int freq) {
            this.regex = regex;
            this.freq = freq;
            this.cal = new GregorianCalendar();
        }

        @Override
        public Date parse(CharSequence input) {
            Matcher m = regex.matcher(input);
            return m.matches() ? toDate(Integer.parseInt(m.group(1)), freq, Integer.parseInt(m.group(2)) - 1) : null;
        }

        private Date toDate(int year, int freq, int pos) {
            if ((pos < 0) || (pos >= freq)) {
                return null;
            }
            int c = 12 / freq;
            int month = pos * c;
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }
    }
    //</editor-fold>
}
