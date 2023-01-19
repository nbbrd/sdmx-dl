package sdmxdl.format.time;

import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.SealedType;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalQuery;
import java.util.function.BiFunction;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

/**
 * <pre>
 * 751 4.2.7 Distinct Range
 * 752 In the case that the reporting period does not fit into one of the prescribe periods
 * 753 above, a distinct time range can be used. The value of these ranges is based on the
 * 754 ISO 8601 time interval format of start/duration. Start can be expressed as either an
 * 755 ISO 8601 date or a date-time, and duration is expressed as an ISO 8601 duration.
 * 756 However, the duration can only be postive.
 * </pre>
 */
@SealedType({
        TimeRange.DateRange.class,
        TimeRange.DateTimeRange.class
})
public abstract class TimeRange<S extends Temporal & Comparable<? super S>, D extends TemporalAmount> implements ObservationalTimePeriod {

    private TimeRange() {
    }

    abstract public @NonNull S getStart();

    abstract public @NonNull D getDuration();

    abstract public @NonNull LocalDateTime toStartTime();

    @Override
    public @NonNull LocalDateTime toStartTime(@Nullable MonthDay reportingYearStartDay) {
        return toStartTime();
    }

    @RepresentableAsString
    @lombok.Value(staticConstructor = "of")
    @lombok.EqualsAndHashCode(callSuper = false)
    public static class DateRange extends TimeRange<LocalDate, Period> {

        @StaticFactoryMethod
        public static @NonNull DateRange parse(@NonNull CharSequence text) throws DateTimeParseException {
            return doParse(text, ISO_LOCAL_DATE, LocalDate::from, DateRange::new);
        }

        public static boolean isParsable(@Nullable CharSequence text) {
            return text != null
                    && text.length() > 10
                    && text.charAt(4) == '-'
                    && text.charAt(7) == '-'
                    && text.charAt(10) == '/';
        }

        @NonNull LocalDate start;

        @NonNull Period duration;

        @Override
        public String toString() {
            return ISO_LOCAL_DATE.format(start) + "/" + duration;
        }

        @Override
        public @NonNull LocalDateTime toStartTime() {
            return start.atStartOfDay();
        }
    }

    @RepresentableAsString
    @lombok.Value(staticConstructor = "of")
    @lombok.EqualsAndHashCode(callSuper = false)
    public static class DateTimeRange extends TimeRange<LocalDateTime, Period> {

        @StaticFactoryMethod
        public static @NonNull DateTimeRange parse(@NonNull CharSequence text) throws DateTimeParseException {
            return doParse(text, ISO_LOCAL_DATE_TIME, LocalDateTime::from, DateTimeRange::new);
        }

        public static boolean isParsable(@Nullable CharSequence text) {
            return text != null
                    && text.length() > 16
                    && text.charAt(4) == '-'
                    && text.charAt(7) == '-'
                    && text.charAt(10) == 'T'
                    && text.charAt(13) == ':';
        }

        @NonNull LocalDateTime start;

        @NonNull Period duration;

        @Override
        public String toString() {
            return ISO_LOCAL_DATE_TIME.format(start) + "/" + duration;
        }

        @Override
        public @NonNull LocalDateTime toStartTime() {
            return start;
        }
    }

    private static <T extends Temporal & Comparable<? super T>, R extends TimeRange<T, Period>> R doParse(
            CharSequence text,
            DateTimeFormatter formatter,
            TemporalQuery<T> query,
            BiFunction<T, Period, R> factory
    ) {
        int intervalDesignatorIdx = getIntervalDesignatorIndex(text);
        CharSequence left = text.subSequence(0, intervalDesignatorIdx);
        CharSequence right = text.subSequence(intervalDesignatorIdx + 1, text.length());
        return factory.apply(formatter.parse(left, query), Period.parse(right));
    }

    private static int getIntervalDesignatorIndex(CharSequence text) throws DateTimeParseException {
        int intervalDesignatorIdx = TimeFormats.indexOf(text, '/');
        if (intervalDesignatorIdx == -1) {
            throw new DateTimeParseException("Cannot find interval designator", text, 0);
        }
        return intervalDesignatorIdx;
    }
}
