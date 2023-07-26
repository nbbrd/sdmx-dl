package internal.sdmxdl.desktop;

import sdmxdl.provider.ri.web.SourceProperties;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DesktopWebFactory {

    static {
        System.setProperty("enableRngDriver", "true");
    }

    public static SdmxWebManager loadManager() {
        return SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .onEvent((source, marker, msg) -> System.out.println(source.getId() + ": " + msg))
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
