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
        Connection connection = RestConnection.of(restClient);
        if (restClient instanceof EventRestClient) {
            EventRestClient eventClient = (EventRestClient) restClient;
            return new SummaryConnection(connection, eventClient);
        }
        return connection;
    }

    private RestClient buildClient(WebSource source, Languages languages, WebContext context, EventListener onEvent) throws IOException {
        RestClient result = CachedRestClient.of(
                client.get(source, languages, context),
                context.getDriverCache(source),
                CACHE_TTL_PROPERTY.get(source.getProperties()),
                source,
                languages);
        return EventRestClient.of(result, onEvent);
    }

    @lombok.RequiredArgsConstructor
    private static final class SummaryConnection implements Connection {

        @lombok.NonNull
        @lombok.experimental.Delegate(types = Connection.class, excludes = AutoCloseable.class)
        private final Connection delegate;

        @lombok.NonNull
        private final EventRestClient eventClient;

        @Override
        public void close() throws IOException {
            try {
                delegate.close();
            } finally {
                eventClient.emitSummary();
            }
        }
    }
}
