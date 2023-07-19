package sdmxdl.grpc;

import sdmxdl.web.SdmxWebManager;

@lombok.experimental.UtilityClass
class GrpcWebFactory {

    public static SdmxWebManager loadManager() {
        return SdmxWebManager.ofServiceLoader();
    }
}
