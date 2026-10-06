package sdmxdl.web;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.AccessReport;

/**
 * Health of a web source, as returned by {@link SdmxWebManager#checkHealth(WebHealthRequest)}.
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class HealthReport {

    @NonNull String source;

    @NonNull HealthVerdict verdict;

    /**
     * Report of the monitor, or {@code null} if not requested or unavailable.
     */
    @Nullable MonitorReport monitor;

    /**
     * Reason why the monitor report is unavailable, if requested.
     */
    @Nullable String monitorError;

    /**
     * Result of the access check, or {@code null} if not requested.
     */
    @Nullable AccessReport access;
}
