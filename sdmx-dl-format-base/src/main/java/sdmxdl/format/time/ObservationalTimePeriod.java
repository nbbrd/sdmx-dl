package sdmxdl.format.time;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.Duration;
import sdmxdl.TimeInterval;

import java.time.LocalDateTime;
import java.time.MonthDay;

/**
 * <pre>
 * 559 4.2.2 Observational Time Period
 * 560 This is the superset of all time representations in SDMX. This allows for time to be
 * 561 expressed as any of the allowable formats
 * </pre>
 */
public interface ObservationalTimePeriod {

    @NonNull LocalDateTime toStartTime(@Nullable MonthDay reportingYearStartDay);

    @NonNull Duration getDuration();

    default @NonNull TimeInterval toTimeInterval(@Nullable MonthDay reportingYearStartDay) {
        return TimeInterval.of(toStartTime(reportingYearStartDay), getDuration());
    }
}
