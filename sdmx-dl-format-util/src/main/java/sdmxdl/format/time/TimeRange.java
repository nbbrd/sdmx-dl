package sdmxdl.format.time;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.SealedType;
import nbbrd.design.StaticFactoryMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalQuery;
import java.util.function.BiFunction;

@SealedType({
        TimeRange.DateRange.class,
        TimeRange.DateTimeRange.class
})
public abstract class TimeRange<T extends Temporal & Comparable<? super T>> {

    private TimeRange() {
    }

    abstract public T getStart();

    abstract public LocalDateTime toStartTime();

    @RepresentableAsString
    @lombok.Value(staticConstructor = "of")
    @lombok.EqualsAndHashCode(callSuper = false)
    public static class DateRange extends TimeRange<LocalDate> {

        @NonNull LocalDate start;
        @NonNull Period duration;

        @Override
        public LocalDateTime toStartTime() {
            return start.atStartOfDay();
        }

        @StaticFactoryMethod
        public static DateRange parse(CharSequence text) {
            return doParse(text, DateTimeFormatter.ISO_DATE, LocalDate::from, DateRange::new);
        }
    }

    @RepresentableAsString
    @lombok.Value(staticConstructor = "of")
    @lombok.EqualsAndHashCode(callSuper = false)
    public static class DateTimeRange extends TimeRange<LocalDateTime> {

        @NonNull LocalDateTime start;
        @NonNull Period duration;

        @Override
        public LocalDateTime toStartTime() {
            return start;
        }

        @StaticFactoryMethod
        public static DateTimeRange parse(CharSequence text) {
            return doParse(text, DateTimeFormatter.ISO_DATE_TIME, LocalDateTime::from, DateTimeRange::new);
        }
    }

    private static <T extends Temporal & Comparable<? super T>, R extends TimeRange<T>> R doParse(
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
        int intervalDesignatorIdx = indexOf(text, '/');
        if (intervalDesignatorIdx == -1) {
            throw new DateTimeParseException("Cannot find interval designator", text, 0);
        }
        return intervalDesignatorIdx;
    }

    @MightBePromoted
    private static int indexOf(CharSequence text, char c) {
        if (text instanceof String) {
            return ((String) text).indexOf(c);
        }
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }
}
