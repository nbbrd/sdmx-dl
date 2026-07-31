package sdmxdl.grpc;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import sdmxdl.web.SdmxWebManager;

@Singleton
public class SdmxWebManagerProducer {

    // Note: SdmxWebManager is final (@lombok.Value), so it cannot be proxied.
    // @Singleton is a pseudo-scope that needs no client proxy while still sharing a single instance.
    @Produces
    public SdmxWebManager sdmxWebManager() {
        return SdmxWebManager.ofServiceLoader().warmupAsync();
    }
}

