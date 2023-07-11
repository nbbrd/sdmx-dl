package sdmxdl.provider.web;

import lombok.NonNull;
import sdmxdl.Connection;
import sdmxdl.DataflowRef;
import sdmxdl.LanguagePriorityList;
import sdmxdl.provider.Validator;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;

import static sdmxdl.provider.web.WebProperties.CACHE_TTL_PROPERTY;

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
    public @NonNull Connection connect(@NonNull SdmxWebSource source, @NonNull LanguagePriorityList languages, @NonNull WebContext context) throws IOException {
        return RestConnection.of(getClient(source, languages, context), dataflowRefValidator, false);
    }

    private RestClient getClient(SdmxWebSource source, LanguagePriorityList languages, WebContext context) throws IOException {
        return CachedRestClient.of(
                client.get(source, languages, context),
                context.getCache(source),
                CACHE_TTL_PROPERTY.get(source.getProperties()),
                source,
                languages);
    }
}
