package sdmxdl.provider.connectors.drivers;

import it.bancaditalia.oss.sdmx.client.AbstractRestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import java.net.URI;
import java.util.Map;
import lombok.NonNull;

@FunctionalInterface
interface GenericSupplier {

    @NonNull AbstractRestSdmxClient<?> get(@NonNull URI endpoint, @NonNull Map<String, String> properties) throws SdmxException;
}
