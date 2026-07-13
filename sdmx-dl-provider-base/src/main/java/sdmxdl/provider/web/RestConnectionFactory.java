package sdmxdl.provider.web;

import lombok.NonNull;
import nbbrd.io.text.BaseProperty;
import sdmxdl.Connection;
import sdmxdl.EventListener;
import sdmxdl.Languages;
import sdmxdl.provider.PropertiesSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.util.List;

import static sdmxdl.provider.web.DriverProperties.CACHE_TTL_PROPERTY;

@lombok.AllArgsConstructor(staticName = "of")
public final class RestConnectionFactory implements ConnectionFactory {

    @lombok.NonNull
    private final RestClientFactory supplier;

    @Override
    public @NonNull List<BaseProperty> getConnectionProperties() {
        return PropertiesSupport.merge(supplier.getRestClientProperties(), CACHE_TTL_PROPERTY);
    }

    @Override
    public @NonNull Connection connect(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException {
        RestClientDecorator lazyClient = new LazyRestClientDecorator(() -> supplier.createRestClient(source, languages, context));

        EventListener eventListener = context.getEventListener(source);
        RestClientDecorator loggedClient = eventListener != null ? new EventRestClientDecorator(lazyClient, eventListener) : lazyClient;

        RestClientDecorator cachedClient = CachedRestClientDecorator.of(
                loggedClient,
                context.getDriverCache(source),
                CACHE_TTL_PROPERTY.get(source.getProperties()),
                source,
                languages);

        return RestConnection.of(cachedClient);
    }
}
