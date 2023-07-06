package sdmxdl.grpc;

import nbbrd.io.curl.CurlHttpURLConnection;
import sdmxdl.provider.web.SingleNetworkingSupport;
import sdmxdl.web.SdmxWebManager;

@lombok.experimental.UtilityClass
class GrpcWebFactory {

    public static SdmxWebManager loadManager() {
        return SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .networking(SingleNetworkingSupport.builder().id("CURL").urlConnectionFactoryOf(CurlHttpURLConnection::of).build())
                .build();
    }
}
