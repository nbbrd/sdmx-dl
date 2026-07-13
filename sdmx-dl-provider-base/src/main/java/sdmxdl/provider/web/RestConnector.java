package sdmxdl.provider.web;

import lombok.NonNull;
import sdmxdl.Connection;
import sdmxdl.EventListener;
import sdmxdl.Languages;
import sdmxdl.web.WebSource;
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

    @Override
    public @NonNull Connection connect(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException {
        EventListener onEvent = context.getEventListener(source);
        RestClient restClient = buildClient(source, languages, context, onEvent);
        return RestConnection.of(restClient);
    }

    private RestClient buildClient(WebSource source, Languages languages, WebContext context, EventListener onEvent) throws IOException {
        return CachedRestClient.of(
                EventRestClient.of(client.get(source, languages, context), onEvent),
                context.getDriverCache(source),
                CACHE_TTL_PROPERTY.get(source.getProperties()),
                source,
                languages);
    }
}
