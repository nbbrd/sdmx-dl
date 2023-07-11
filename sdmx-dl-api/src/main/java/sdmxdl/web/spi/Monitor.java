package sdmxdl.web.spi;

import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import sdmxdl.Marker;
import sdmxdl.web.MonitorReport;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        loaderName = "internal.util.MonitorLoader"
)
@ThreadSafe
public interface Monitor {

    @ServiceId
    @NonNull String getMonitorId();

    @NonNull String getMonitorUriScheme();

    @NonNull MonitorReport getReport(
            @NonNull SdmxWebSource source,
            @NonNull WebContext context
    ) throws IOException, IllegalArgumentException;

    Marker MONITOR_MARKER = Marker.parse("MONITOR");
}
