package sdmxdl.web.spi;

import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import sdmxdl.web.MonitorReport;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        loaderName = "internal.util.WebMonitoringLoader"
)
@ThreadSafe
public interface WebMonitoring {

    @NonNull
    String getUriScheme();

    @NonNull
    MonitorReport getReport(
            @NonNull SdmxWebSource source,
            @NonNull WebContext context
    ) throws IOException, IllegalArgumentException;
}
