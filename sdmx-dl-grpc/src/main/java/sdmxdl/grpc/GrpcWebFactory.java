package sdmxdl.grpc;

import sdmxdl.provider.ri.web.SourceProperties;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@lombok.experimental.UtilityClass
class GrpcWebFactory {

    public static SdmxWebManager loadManager() {
        return SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .customSources(getCustomSources())
                .build();
    }

    private static List<SdmxWebSource> getCustomSources() {
        try {
            return SourceProperties.loadCustomSources();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
