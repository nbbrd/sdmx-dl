package sdmxdl.provider.connectors.drivers;

import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import lombok.NonNull;

import java.net.URISyntaxException;

@FunctionalInterface
interface SpecificSupplier {

    @NonNull
    RestSdmxClient get() throws URISyntaxException;
}
