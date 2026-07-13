package sdmxdl.provider.connectors.drivers;

import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import lombok.NonNull;
import nbbrd.io.text.BaseProperty;
import sdmxdl.Languages;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.HasMarker;
import sdmxdl.provider.PropertiesSupport;
import sdmxdl.provider.web.RestClient;
import sdmxdl.provider.web.RestClientFactory;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Supplier;

import static sdmxdl.provider.connectors.drivers.ConnectorsRestClient.CONNECTORS_CONNECTION_PROPERTIES;

@lombok.Builder
final class SpecificRestClientFactory implements RestClientFactory {

    private final @NonNull SpecificSupplier supplier;

    @lombok.Builder.Default
    private final @NonNull Supplier<ObsParser> obsFactory = ObsParser::newDefault;

    @lombok.Singular
    private final @NonNull List<BaseProperty> properties;

    @Override
    public @NonNull List<BaseProperty> getRestClientProperties() {
        return PropertiesSupport.merge(CONNECTORS_CONNECTION_PROPERTIES, properties);
    }

    @Override
    public @NonNull RestClient createRestClient(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) {
        try {
            RestSdmxClient client = supplier.get();
            client.setEndpoint(source.getEndpoint());
            ConnectorsRestClient.configure(client, source, context);
            return new ConnectorsRestClient(HasMarker.of(source), client, obsFactory);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
}
