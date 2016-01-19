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
        timeFormat(TimeFormat.UNDEFINED);
    }

    @Nonnull
    public ObsParser timeFormat(@Nonnull TimeFormat timeFormat) {
        if (this.timeFormat != timeFormat) {
            this.timeFormat = timeFormat;
            this.periodParser = getParser(timeFormat);
        }
        return this;
    }

    @Nonnull
    public ObsParser clear() {
        this.period = null;
        this.value = null;
        return this;
    }

    @Nonnull
    public ObsParser withPeriod(@Nullable String period) {
        this.period = period;
        return this;
    }

    @Nonnull
    public ObsParser withValue(@Nullable String value) {
        this.value = value;
        return this;
    }

    @Nonnull
    public TimeFormat getTimeFormat() {
        return timeFormat;
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
    private static abstract class DateParser {

        @Nullable
        abstract public Date parse(@Nonnull CharSequence input);

        @Nonnull
        public DateParser or(@Nonnull final DateParser r) {
            final DateParser l = this;
            return new DateParser() {
                @Override
                public Date parse(CharSequence input) {
                    Date result = l.parse(input);
                    return result != null ? result : r.parse(input);
                }
            };
        }
    }

    @Nonnull
    private static DateParser getParser(@Nonnull TimeFormat format) {
        switch (format) {
            case YEARLY:
                return onStrictDatePattern("yyyy").or(onStrictDatePattern("yyyy'-01'")).or(onStrictDatePattern("yyyy'-A1'"));
            case HALF_YEARLY:
                return new YearFreqPosParser().or(onStrictDatePattern("yyyy-MM"));
            case QUADRI_MONTHLY:
                return new YearFreqPosParser().or(onStrictDatePattern("yyyy-MM"));
            case QUARTERLY:
                return new YearFreqPosParser().or(onStrictDatePattern("yyyy-MM"));
            case MONTHLY:
                return new YearFreqPosParser().or(onStrictDatePattern("yyyy-MM"));
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

    private static abstract class FailSafeParser extends DateParser {

        @Override
        public Date parse(CharSequence input) {
            try {
                return doParse(input);
            } catch (Exception ex) {
                return null;
            }
        }

        abstract Date doParse(CharSequence input) throws Exception;
    }

    @Nonnull
    private static DateParser onStrictDatePattern(@Nonnull String datePattern) {
        final DateFormat dateFormat = new SimpleDateFormat(datePattern, Locale.ROOT);
        dateFormat.setLenient(false);
        return new FailSafeParser() {
            @Override
            protected Date doParse(CharSequence input) throws Exception {
                String inputAsString = input.toString();
                Date result = dateFormat.parse(inputAsString);
                return result != null && inputAsString.equals(dateFormat.format(result)) ? result : null;
            }
        };
    }

    private static final class YearFreqPosParser extends FailSafeParser {

        private final Calendar cal = new GregorianCalendar();

        @Override
        protected Date doParse(CharSequence input) throws Exception {
            Matcher m = REGEX.matcher(input);
            return m.matches() ? toDate(toInt(m.group(YEAR)), toFreq(m.group(FREQ)), toInt(m.group(POS))) : null;
        }

        private Date toDate(int year, int freq, int pos) throws IllegalArgumentException {
            if ((pos < 0) || (pos >= freq)) {
                throw new IllegalArgumentException();
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

        private static final Pattern REGEX = Pattern.compile("(\\d+)-?([QMYST])(\\d+)");
        private static final int YEAR = 1, FREQ = 2, POS = 3;

        private static int toInt(String input) {
            return Integer.parseInt(input);
        }

        private static int toFreq(String input) {
            switch (input) {
                case "Q":
                    return 4;
                case "M":
                    return 12;
                case "Y":
                    return 1;
                case "S":
                    return 2;
                case "T":
                    return 3;
                default:
                    return 0;
            }
        }
    }
    //</editor-fold>
}
