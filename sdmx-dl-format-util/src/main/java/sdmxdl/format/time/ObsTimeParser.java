package sdmxdl.format.time;

import lombok.NonNull;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.List;
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

    static @NonNull ObsTimeParser of(@NonNull Parser<LocalDateTime> parser) {
        return (t, r) -> parser.parse(t);
    }

    static @NonNull ObsTimeParser ofAll(@NonNull List<ObsTimeParser> parsers) {
        return (t, r) -> {
            for (ObsTimeParser parser : parsers) {
                LocalDateTime result = parser.parseStartTime(t, r);
                if (result != null) {
                    return result;
                }
            }
            return null;
        };
    }

    static @NonNull ObsTimeParser onNull() {
        return (t, r) -> null;
    }

    static @NonNull ObsTimeParser onObservationalTimePeriod() {
        return TimeFormats.OBSERVATIONAL_TIME_PERIOD;
    }

    static @NonNull ObsTimeParser onStandardReporting(@NonNull StandardReportingFormat format) {
        return (t, r) -> StandardReportingFormat.parseStartTime(t, r, format);
    }

    static @NonNull ObsTimeParser onTimeRange(@NonNull Function<CharSequence, TimeRange<?>> format) {
        return of(Parser.of(format).andThen(timeRange -> timeRange != null ? timeRange.toStartTime() : null));
    }
}
