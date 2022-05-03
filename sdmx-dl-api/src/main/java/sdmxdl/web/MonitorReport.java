package sdmxdl.web;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URL;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class MonitorReport {

    @lombok.NonNull
    String source;

    @lombok.NonNull
    @lombok.Builder.Default
    MonitorStatus status = MonitorStatus.UNKNOWN;

    @Nullable
    @lombok.Builder.Default
    Double uptimeRatio = null;

    @Nullable
    @lombok.Builder.Default
    Long averageResponseTime = null;

    @Nullable
    @lombok.Builder.Default
    URL webReport = null;
}
