package sdmxdl.web.spi;

import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.web.SdmxWebMonitorReport;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        loaderName = "internal.util.SdmxWebMonitoringLoader"
)
@ThreadSafe
public interface SdmxWebMonitoring {

    @NonNull
    String getProviderName();

    @NonNull
    SdmxWebMonitorReport getReport(
            @NonNull SdmxWebSource source,
            @NonNull SdmxWebContext context
    ) throws IOException, IllegalArgumentException;
}
