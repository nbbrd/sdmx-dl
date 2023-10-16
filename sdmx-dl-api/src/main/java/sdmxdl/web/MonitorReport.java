package sdmxdl.web;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class MonitorReport {

    @NonNull String source;

    @lombok.Builder.Default
    @NonNull MonitorStatus status = MonitorStatus.UNKNOWN;

    @Nullable Double uptimeRatio;

    @Nullable Long averageResponseTime;
}
