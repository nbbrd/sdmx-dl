package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.http.curl.CurlHttpClient;
import nbbrd.io.http.urlconnection.UrlConnectionHttpClient;
import nbbrd.io.http.urlconnection.UrlConnectionListener;
import org.jspecify.annotations.Nullable;
import sdmxdl.EventListener;
import sdmxdl.provider.Slow;
import sdmxdl.provider.web.WebEvents;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.WebContext;

import java.net.Proxy;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
                        .listener(onEvent != null ? new RiHttpEventListener(message -> onEvent.accept(HttpDecoration.MARKER, message, 1)) : UrlConnectionListener.noOp())
                        .userAgent(USER_AGENT_PROPERTY.get(properties))
                        .build();
        }
    }

    /**
     * Listener that reports HTTP connection events and timing information.
     * <p>
     * Monitors opening connections, successful responses, redirections, and authentication attempts,
     * forwarding these events to the underlying listener with human-readable messages.
     * </p>
     */
    @lombok.AllArgsConstructor
    private static final class RiHttpEventListener implements UrlConnectionListener {

        /**
         * Consumer for event messages.
         */
        private final @NonNull Consumer<CharSequence> listener;

        /**
         * Timestamp when the request was opened (milliseconds).
         */
        private long openTimestamp;

        /**
         * Creates an event listener with an initial timestamp.
         *
         * @param listener the consumer to receive formatted event messages
         */
        RiHttpEventListener(@NonNull Consumer<CharSequence> listener) {
            this.listener = listener;
            this.openTimestamp = 0;
        }

        /**
         * Reports the opening of an HTTP connection.
         * <p>
         * Records the current timestamp and reports the HTTP method, URL, proxy settings,
         * and authentication scheme being used.
         * </p>
         *
         * @param request the HTTP request being sent
         * @param proxy   the proxy configuration, if any
         */
        @Override
        public void onOpen(@NonNull HttpRequest request, @NonNull Proxy proxy) {
            openTimestamp = System.currentTimeMillis();
            listener.accept(WebEvents.onQuery(request.getMethod().name(), request.getQuery(), proxy));
        }

        /**
         * Reports successful content type parsing, including elapsed time.
         *
         * @param contentType supplier providing the resolved content type
         */
        @Override
        public void onSuccess(@NonNull Supplier<String> contentType) {
            long elapsed = System.currentTimeMillis() - openTimestamp;
            listener.accept(String.format(Locale.ROOT, "Parsing '%s' content-type (%dms)", contentType.get(), elapsed));
        }

        /**
         * Reports a generic event message.
         *
         * @param message the event message to report
         */
        @Override
        public void onEvent(@NonNull String message) {
            listener.accept(message);
        }
    }
}
