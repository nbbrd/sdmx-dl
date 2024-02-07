package sdmxdl.web.spi;

import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import sdmxdl.web.MonitorReport;
import sdmxdl.web.WebSource;

import java.io.IOException;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        loaderName = "internal.{{canonicalName}}Loader"
)
@ThreadSafe
public interface Monitor {

    @ServiceId(pattern = ServiceId.SCREAMING_SNAKE_CASE)
    @NonNull String getMonitorId();

    @NonNull String getMonitorUriScheme();

    @NonNull MonitorReport getReport(
            @NonNull WebSource source,
            @NonNull WebContext context
    ) throws IOException, IllegalArgumentException;
}
