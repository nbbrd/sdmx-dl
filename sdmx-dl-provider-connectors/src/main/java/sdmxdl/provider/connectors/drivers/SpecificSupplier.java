package sdmxdl.provider.connectors.drivers;

import it.bancaditalia.oss.sdmx.client.AbstractRestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.NonNull;

@FunctionalInterface
interface SpecificSupplier {

    @NonNull AbstractRestSdmxClient<?> get(@NonNull URI endpoint) throws SdmxException, URISyntaxException;
}
