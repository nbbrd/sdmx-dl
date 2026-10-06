package sdmxdl.web;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.AccessReport;

/**
 * Overall diagnosis of a {@link HealthReport}, derived from its monitor status and access check.
 */
public enum HealthVerdict {

    /**
     * The source works as expected.
     */
    OK,

    /**
     * The source answers but with errors, or the monitor and the access check disagree.
     */
    DEGRADED,

    /**
     * The monitor says the source is up but it cannot be reached from here (proxy, firewall, SSL, ...).
     */
    LOCAL_ISSUE,

    /**
     * The monitor says the source is down.
     */
    REMOTE_OUTAGE,

    /**
     * The source cannot be reached and no monitor tells whether the cause is local or remote.
     */
    UNREACHABLE,

    /**
     * Not enough information to conclude.
     */
    UNKNOWN;

    /**
     * Derives a verdict from the results of the checks.
     *
     * @param monitor the monitor status, or {@code null} if not checked or unavailable
     * @param access  the access report, or {@code null} if not checked
     * @return a non-null verdict
     */
    public static @NonNull HealthVerdict of(@Nullable MonitorStatus monitor, @Nullable AccessReport access) {
        MonitorStatus status = monitor != null ? monitor : MonitorStatus.UNKNOWN;
        if (access == null) {
            switch (status) {
                case UP:
                    return OK;
                case DOWN:
                    return REMOTE_OUTAGE;
                default:
                    return UNKNOWN;
            }
        }
        if (access.isAccessible()) {
            return status == MonitorStatus.DOWN ? DEGRADED : OK;
        }
        if (access.isReachable()) {
            return DEGRADED;
        }
        switch (status) {
            case UP:
                return LOCAL_ISSUE;
            case DOWN:
                return REMOTE_OUTAGE;
            default:
                return UNREACHABLE;
        }
    }
}
