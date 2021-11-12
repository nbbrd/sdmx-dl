package sdmxdl.util.parser;

import nbbrd.io.text.Parser;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.Frequency;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@lombok.experimental.UtilityClass
public class PeriodParsers {

    @NonNull
    public Parser<LocalDateTime> onStandardFreq(@NonNull Frequency freq) {
        switch (freq) {
            case ANNUAL:
                return ANNUAL;
            case HALF_YEARLY:
                return HALF_YEARLY;
            case QUARTERLY:
                return QUARTERLY;
            case MONTHLY:
                return MONTHLY;
            case WEEKLY:
                return WEEKLY;
            case DAILY:
                return DAILY;
            case HOURLY:
                return HOURLY;
            case DAILY_BUSINESS:
                return DAILY_BUSINESS;
            case MINUTELY:
                return MINUTELY;
            case UNDEFINED:
                return UNDEFINED;
            default:
                throw new RuntimeException();
        }
    }

    private final Parser<LocalDateTime> YEAR = onDatePattern("yyyy");
    private final Parser<LocalDateTime> YEAR_MONTH = onDatePattern("yyyy-MM");
    private final Parser<LocalDateTime> YEAR_MONTH_DAY = onDatePattern("yyyy-MM-dd");

    private final Parser<LocalDateTime> ANNUAL = YEAR.orElse(onDatePattern("yyyy'-01'")).orElse(onDatePattern("yyyy'-A1'"));
    private final Parser<LocalDateTime> HALF_YEARLY = onYearFreqPos("S", 2).orElse(YEAR_MONTH);
    private final Parser<LocalDateTime> QUARTERLY = onYearFreqPos("Q", 4).orElse(YEAR_MONTH);
    private final Parser<LocalDateTime> MONTHLY = onYearFreqPos("M", 12).orElse(YEAR_MONTH);
    private final Parser<LocalDateTime> WEEKLY = YEAR_MONTH_DAY;
    private final Parser<LocalDateTime> DAILY = YEAR_MONTH_DAY;
    // FIXME: needs other pattern for time
    private final Parser<LocalDateTime> HOURLY = YEAR_MONTH_DAY;
    private final Parser<LocalDateTime> DAILY_BUSINESS = YEAR_MONTH_DAY;
    private final Parser<LocalDateTime> MINUTELY = YEAR_MONTH_DAY;
    private final Parser<LocalDateTime> UNDEFINED = onDatePattern("yyyy[-MM[-dd]]");

    @NonNull
    public Parser<LocalDateTime> onDatePattern(@NonNull String pattern) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern(pattern)
                .parseStrict()
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter(Locale.ROOT);
        return Parser.onDateTimeFormatter(formatter, LocalDateTime::from);
    }

    @NonNull
    public Parser<LocalDateTime> onYearFreqPos(@NonNull String freqCode, @NonNegative int freq) {
        return new YearFreqPos(freqCode, freq);
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
            if (input == null) {
                return null;
            }
            Matcher m = regex.matcher(input);
            return m.matches() ? toDate(Integer.parseInt(m.group(1)), freq, Integer.parseInt(m.group(2)) - 1) : null;
        }

        private LocalDateTime toDate(int year, int freq, int pos) {
            return ((pos < 0) || (pos >= freq)) ? null : LocalDate.of(year, pos * (12 / freq) + 1, 1).atStartOfDay();
        }
    }
}
