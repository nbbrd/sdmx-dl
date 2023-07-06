package internal.sdmxdl.desktop;

import nbbrd.io.curl.CurlHttpURLConnection;
import sdmxdl.provider.web.SingleNetworkingSupport;
import sdmxdl.web.SdmxWebManager;

public class DesktopWebFactory {

    static {
        System.setProperty("enableRngDriver", "true");
    }

    public static SdmxWebManager loadManager() {
        return SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .networking(SingleNetworkingSupport.builder().id("CURL").urlConnectionFactoryOf(CurlHttpURLConnection::of).build())
                .eventListener((source, msg) -> System.out.println(source.getId() + ": " + msg))
                .build();
    }
}
