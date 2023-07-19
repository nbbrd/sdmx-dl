package internal.sdmxdl.desktop;

import sdmxdl.web.SdmxWebManager;

public class DesktopWebFactory {

    static {
        System.setProperty("enableRngDriver", "true");
    }

    public static SdmxWebManager loadManager() {
        return SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .onEvent((source, marker, msg) -> System.out.println(source.getId() + ": " + msg))
                .build();
    }
}
