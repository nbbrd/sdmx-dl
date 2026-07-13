package sdmxdl.provider.connectors.drivers;

import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import lombok.NonNull;

import java.net.URI;
import java.util.Map;

@FunctionalInterface
interface GenericSupplier {

    @NonNull
    RestSdmxClient get(@NonNull URI endpoint, @NonNull Map<String, String> properties);
}
