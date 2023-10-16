package internal.sdmxdl.desktop;

import sdmxdl.provider.ri.drivers.SourceProperties;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DesktopWebFactory {

    static {
        System.setProperty("enableRngDriver", "true");
        System.setProperty("enableFileDriver", "true");
        System.setProperty("enablePxWebDriver", "true");
    }

    public static SdmxWebManager loadManager() {
        return SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .onEvent((source, marker, msg) -> System.out.println(source.getId() + ": " + msg))
                .customSources(getCustomSources())
                .build();
    }

    private static List<WebSource> getCustomSources() {
        try {
            return SourceProperties.loadCustomSources();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
