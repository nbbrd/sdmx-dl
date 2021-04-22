package sdmxdl.web;

import org.checkerframework.checker.nullness.qual.Nullable;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class SdmxWebMonitorReport {

    @lombok.NonNull
    SdmxWebStatus status;

    @Nullable
    Double uptimeRatio;

    @Nullable
    Double averageResponseTime;

    public static final SdmxWebMonitorReport EMPTY = new SdmxWebMonitorReport(SdmxWebStatus.UNKNOWN, null, null);
}
