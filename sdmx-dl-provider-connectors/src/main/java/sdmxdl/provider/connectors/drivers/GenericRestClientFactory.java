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

import java.util.List;
import java.util.function.Supplier;

import static sdmxdl.provider.connectors.drivers.ConnectorsRestClient.CONNECTORS_CONNECTION_PROPERTIES;

@lombok.Builder
final class GenericRestClientFactory implements RestClientFactory {

    private final @NonNull GenericSupplier supplier;

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
        RestSdmxClient client = supplier.get(source.getEndpoint(), source.getProperties());
        ConnectorsRestClient.configure(client, source, context);
        return new ConnectorsRestClient(HasMarker.of(source), client, obsFactory);
    }
}
