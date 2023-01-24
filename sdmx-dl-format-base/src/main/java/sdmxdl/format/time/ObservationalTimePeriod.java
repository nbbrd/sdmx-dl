package sdmxdl.format.time;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
}
