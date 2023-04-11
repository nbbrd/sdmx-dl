package sdmxdl.format.time;

import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.SealedType;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.Duration;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

/**
 * <pre>
 * 568 4.2.4 Gregorian Time Period
 * 569 A Gregorian time period is always represented by a Gregorian year, year-month, or
 * 570 day. These are all based on ISO 8601 dates. The representation in SDMX-ML
 * 571 messages and the period covered by each of the Gregorian time periods are as
 * 572 follows:
 * 573
 * 574   Gregorian Year:
 * 575     Representation: xs:gYear (YYYY)
 * 576     Period: the start of January 1 to the end of December 31
 * 577   Gregorian Year Month:
 * 578     Representation: xs:gYearMonth (YYYY-MM)
 * 579     Period: the start of the first day of the month to end of the last day of the month
 * 580   Gregorian Day:
 * 581     Representation: xs:date (YYYY-MM-DD)
 * 582     Period: the start of the day (00:00:00) to the end of the day (23:59:59)
 * </pre>
 */
@SealedType({
        GregorianTimePeriod.Year.class,
        GregorianTimePeriod.YearMonth.class,
        GregorianTimePeriod.Day.class
})
public abstract class GregorianTimePeriod<T extends Temporal & Comparable<? super T>> implements BasicTimePeriod {

    private GregorianTimePeriod() {
    }

    abstract public @NonNull T getDate();

    abstract public @NonNull LocalDateTime toStartTime();

    @Override
    public @NonNull LocalDateTime toStartTime(@Nullable MonthDay reportingYearStartDay) {
        return toStartTime();
    }

    @RepresentableAsString
    @lombok.Value(staticConstructor = "of")
    @lombok.EqualsAndHashCode(callSuper = false)
    public static class Year extends GregorianTimePeriod<java.time.Year> {

        @StaticFactoryMethod
        public static @NonNull Year parse(@NonNull CharSequence text) throws DateTimeParseException {
            return new Year(STRICT_YEAR.parse(text, java.time.Year::from));
        }

        public static boolean isParsable(@Nullable CharSequence text) {
            return text != null
                    && text.length() == 4;
        }

        @NonNull java.time.Year date;

        @Override
        public String toString() {
            return STRICT_YEAR.format(date);
        }

        @Override
        public @NonNull LocalDateTime toStartTime() {
            return LocalDateTime.of(date.getValue(), 1, 1, 0, 0);
        }

        @Override
        public @NonNull Duration getDuration() {
            return P1Y;
        }

        private static final Duration P1Y = Duration.parse("P1Y");

        // JDK > 8 changed parsing behavior of Year#parse(CharSequence) to accept min 1 digit instead of 4
        private static final DateTimeFormatter STRICT_YEAR = DateTimeFormatter.ofPattern("uuuu", Locale.ROOT);
    }

    @RepresentableAsString
    @lombok.Value(staticConstructor = "of")
    @lombok.EqualsAndHashCode(callSuper = false)
    public static class YearMonth extends GregorianTimePeriod<java.time.YearMonth> {

        @StaticFactoryMethod
        public static @NonNull YearMonth parse(@NonNull CharSequence text) throws DateTimeParseException {
            return new YearMonth(java.time.YearMonth.parse(text));
        }

        public static boolean isParsable(@Nullable CharSequence text) {
            return text != null
                    && text.length() == 7
                    && text.charAt(4) == '-'
                    && Character.isDigit(text.charAt(5));
        }

        @NonNull java.time.YearMonth date;

        @Override
        public String toString() {
            return date.toString();
        }

        @Override
        public @NonNull LocalDateTime toStartTime() {
            return LocalDateTime.of(date.getYear(), date.getMonth(), 1, 0, 0);
        }

        @Override
        public @NonNull Duration getDuration() {
            return P1M;
        }

        private static final Duration P1M = Duration.parse("P1M");
    }

    @RepresentableAsString
    @lombok.Value(staticConstructor = "of")
    @lombok.EqualsAndHashCode(callSuper = false)
    public static class Day extends GregorianTimePeriod<java.time.LocalDate> {

        @StaticFactoryMethod
        public static @NonNull Day parse(@NonNull CharSequence text) throws DateTimeParseException {
            return new Day(ISO_LOCAL_DATE.parse(text, java.time.LocalDate::from));
        }

        public static boolean isParsable(@Nullable CharSequence text) {
            return text != null
                    && text.length() == 10
                    && text.charAt(4) == '-'
                    && text.charAt(7) == '-';
        }

        @NonNull java.time.LocalDate date;

        @Override
        public String toString() {
            return ISO_LOCAL_DATE.format(date);
        }

        @Override
        public @NonNull LocalDateTime toStartTime() {
            return date.atStartOfDay();
        }

        @Override
        public @NonNull Duration getDuration() {
            return P1D;
        }

        private static final Duration P1D = Duration.parse("P1D");
    }
}
