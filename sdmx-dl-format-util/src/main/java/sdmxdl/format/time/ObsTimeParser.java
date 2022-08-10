package sdmxdl.format.time;

import lombok.NonNull;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.function.Function;

@FunctionalInterface
public interface ObsTimeParser {

    @Nullable LocalDateTime parseStartTime(@Nullable CharSequence text, @Nullable MonthDay reportingYearStartDay);

    default @NonNull ObsTimeParser orElse(@NonNull ObsTimeParser other) {
        return (t, r) -> {
            LocalDateTime result = parseStartTime(t, r);
            return result != null ? result : other.parseStartTime(t, r);
        };
    }

    static @NonNull ObsTimeParser onNull() {
        return (t, r) -> null;
    }

    static @NonNull ObsTimeParser onParser(@NonNull Parser<LocalDateTime> parser) {
        return (t, r) -> parser.parse(t);
    }

    static @NonNull ObsTimeParser onStandardReporting(@NonNull StandardReportingFormat format) {
        return (t, r) -> {
            StandardReportingPeriod result = StandardReportingPeriod.parseOrNull(t);
            return result != null && result.isCompatibleWith(format) ? result.toStartDate(format, r).atStartOfDay() : null;
        };
    }

    static @NonNull ObsTimeParser onTimeRange(@NonNull Function<? super CharSequence, ? extends TimeRange<?>> parser) {
        return (t, r) -> {
            try {
                TimeRange<?> result = parser.apply(t);
                return result != null ? result.toStartTime() : null;
            } catch (Throwable ex) {
                return null;
            }
        };
    }
}
