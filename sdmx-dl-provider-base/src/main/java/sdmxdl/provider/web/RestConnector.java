package sdmxdl.provider.web;

import lombok.NonNull;
import sdmxdl.Connection;
import sdmxdl.DataflowRef;
import sdmxdl.Languages;
import sdmxdl.provider.Validator;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;

import static sdmxdl.provider.web.DriverProperties.CACHE_TTL_PROPERTY;

@lombok.Builder
public final class RestConnector implements WebConnector {

    public static @NonNull RestConnector of(@NonNull RestClientSupplier client) {
        return RestConnector.builder().client(client).build();
    }

    @lombok.NonNull
    private final RestClientSupplier client;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Validator<DataflowRef> dataflowRefValidator = WebValidators.DEFAULT_DATAFLOW_REF_VALIDATOR;

    @Override
    public @NonNull Connection connect(@NonNull SdmxWebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException {
        return RestConnection.of(getClient(source, languages, context), dataflowRefValidator, false);
    }

    private RestClient getClient(SdmxWebSource source, Languages languages, WebContext context) throws IOException {
        return CachedRestClient.of(
                client.get(source, languages, context),
                context.getDriverCache(source),
                CACHE_TTL_PROPERTY.get(source.getProperties()),
                source,
                languages);
    }
}
