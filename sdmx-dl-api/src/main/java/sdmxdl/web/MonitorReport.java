package sdmxdl.web;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class MonitorReport {

    @NonNull String source;

    @lombok.Builder.Default
    @NonNull MonitorStatus status = MonitorStatus.UNKNOWN;

    @Nullable Double uptimeRatio;

    @Nullable Long averageResponseTime;
}
