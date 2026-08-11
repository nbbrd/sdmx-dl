package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.curl.CurlHttpClient;
import nbbrd.io.http.ext.LoggingDecorator;
import nbbrd.io.http.ext.LoggingHandler;
import nbbrd.io.http.urlconnection.UrlConnectionHttpClient;
import org.jspecify.annotations.Nullable;
import sdmxdl.EventListener;
import sdmxdl.provider.Slow;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.WebContext;

import java.util.function.Function;

import static sdmxdl.provider.web.DriverProperties.*;

/**
 * Factory for creating {@link HttpClient} instances.
 * <p>
 * This factory builds HTTP clients with support for
 * authentication, SSL/TLS, proxies, and event/error listening. It configures clients based on
 * web source properties and context settings.
 * </p>
 */
public final class DefaultHttpFactory implements HttpFactory {

    @lombok.experimental.Delegate
    private final HttpFactory support = HttpFactorySupport
            .builder()
            .name("UrlConnectionHttpClientFactory")
            .property(CONNECT_TIMEOUT_PROPERTY)
            .property(READ_TIMEOUT_PROPERTY)
            .property(USER_AGENT_PROPERTY)
            .supplier(DefaultHttpFactory::newHttpClient)
            .build();

    /**
     * Creates a new {@link HttpClient} configured for the given web source and context.
     * <p>
     * Configures the client with:
     * <ul>
     *   <li>Network settings (timeouts, SSL, proxy configuration)</li>
     *   <li>Event and error listeners for monitoring requests</li>
     *   <li>User agent string</li>
     * </ul>
     * </p>
     *
     * @param source  the web source providing configuration and properties
     * @param context the web context containing network, authentication, and event listeners
     * @return a configured HTTP client for the specified source
     */
    @Slow
    @VisibleForTesting
    static HttpClient newHttpClient(@NonNull WebSource source, @NonNull WebContext context) {
        return newHttpClient(
                context.getNetwork(source),
                source.getProperties()::get,
                context.getEventListener(source)
        );
    }

    public static @NonNull HttpClient newHttpClient(
            @NonNull Network network,
            @NonNull Function<? super String, ? extends CharSequence> properties,
            @Nullable EventListener onEvent
    ) {
        HttpClient client = resolveHttpClient(network, properties);
        if (onEvent != null) {
            client = new LoggingDecorator(client, LoggingHandler.basic(message -> onEvent.accept(HttpDecoration.MARKER, message, 1)));
        }
        return client;
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private static @NonNull HttpClient resolveHttpClient(@NonNull Network network, @NonNull Function<? super String, ? extends CharSequence> properties) {
        switch (network.getUrlBackend()) {
            case Network.CURL_URL_BACKEND:
                return CurlHttpClient
                        .builder()
                        .connectTimeout(CONNECT_TIMEOUT_PROPERTY.get(properties))
                        .readTimeout(READ_TIMEOUT_PROPERTY.get(properties))
                        .proxySelector(network.getProxySelector())
                        .userAgent(USER_AGENT_PROPERTY.get(properties))
                        .followRedirects(false)
                        .build();
            default:
                return UrlConnectionHttpClient
                        .builder()
                        .connectTimeout(CONNECT_TIMEOUT_PROPERTY.get(properties))
                        .readTimeout(READ_TIMEOUT_PROPERTY.get(properties))
                        .proxySelector(network.getProxySelector())
                        .sslSocketFactory(network.getSSLFactory().getSSLSocketFactory())
                        .hostnameVerifier(network.getSSLFactory().getHostnameVerifier())
                        .userAgent(USER_AGENT_PROPERTY.get(properties))
                        .build();
        }
    }
}
