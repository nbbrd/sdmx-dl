package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.http.*;
import nbbrd.io.text.IntProperty;
import org.jspecify.annotations.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.provider.web.WebEvents;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Authenticator;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.WebCaching;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static sdmxdl.provider.ri.drivers.AuthSchemes.BASIC_AUTH_SCHEME;
import static sdmxdl.provider.ri.drivers.AuthSchemes.MSAL_AUTH_SCHEME;
import static sdmxdl.provider.web.DriverProperties.*;
import static sdmxdl.web.spi.Driver.DRIVER_PROPERTY_PREFIX;

/**
 * Factory for creating {@link UrlConnectionHttpClient} instances.
 * <p>
 * This factory builds HTTP clients using Java's {@link java.net.URLConnection} API, with support for
 * authentication, SSL/TLS, proxies, and event/error listening. It configures clients based on
 * web source properties and context settings.
 * </p>
 */
public final class UrlConnectionHttpFactory implements HttpFactory {

    /**
     * Property defining the maximum number of automatic retries for transient network errors.
     * <p>
     * When an HTTP request encounters transient network failures (such as temporary connection issues,
     * timeouts, or temporary server errors), the client can automatically retry the request up to
     * this many times before reporting a failure to the caller.
     * </p>
     * <p>
     * Default value: 3 retries
     * </p>
     *
     * @see UrlConnectionHttpClient for retry behavior details
     */
    @PropertyDefinition
    public static final IntProperty MAX_RETRIES_PROPERTY =
            IntProperty.of(DRIVER_PROPERTY_PREFIX + ".maxRetries", 3);

    @lombok.experimental.Delegate
    private final HttpFactory support = HttpFactorySupport
            .builder()
            .name("UrlConnectionHttpClientFactory")
            .property(CONNECT_TIMEOUT_PROPERTY)
            .property(READ_TIMEOUT_PROPERTY)
            .property(MAX_REDIRECTS_PROPERTY)
            .property(AUTH_SCHEME_PROPERTY)
            .property(USER_AGENT_PROPERTY)
            .property(MAX_RETRIES_PROPERTY)
            .supplier(UrlConnectionHttpFactory::newUrlConnectionHttpClient)
            .build();

    /**
     * Creates a new {@link UrlConnectionHttpClient} configured for the given web source and context.
     * <p>
     * Configures the client with:
     * <ul>
     *   <li>Network settings (timeouts, SSL, proxy configuration)</li>
     *   <li>Authentication (based on configured scheme)</li>
     *   <li>Event and error listeners for monitoring requests</li>
     *   <li>User agent string</li>
     * </ul>
     * </p>
     *
     * @param source  the web source providing configuration and properties
     * @param context the web context containing network, authentication, and event listeners
     * @return a configured HTTP client for the specified source
     */
    @VisibleForTesting
    static UrlConnectionHttpClient newUrlConnectionHttpClient(@NonNull WebSource source, @NonNull WebContext context) {
        Network network = context.getNetwork(source);
        EventListener onEvent = context.getEventListener(source);
        ErrorListener onError = context.getErrorListener(source);
        return UrlConnectionHttpClient
                .builder()
                .connectTimeout(CONNECT_TIMEOUT_PROPERTY.get(source.getProperties()))
                .readTimeout(READ_TIMEOUT_PROPERTY.get(source.getProperties()))
                .maxRedirects(MAX_REDIRECTS_PROPERTY.get(source.getProperties()))
                .authScheme(toHttpAuthScheme(AUTH_SCHEME_PROPERTY.get(source.getProperties())))
                .proxySelector(network.getProxySelector())
                .sslSocketFactory(network.getSSLFactory().getSSLSocketFactory())
                .hostnameVerifier(network.getSSLFactory().getHostnameVerifier())
                .urlConnectionFactory(network.getURLConnectionFactory()::openConnection)
                .listener(onEvent != null ? new RiHttpEventListener(message -> onEvent.accept("RI_HTTP", message, 1)) : UrlConnectionListener.noOp())
                .authenticator(new RiHttpAuthenticator(source, context.getAuthenticators(), context.getCaching(), onEvent, onError))
                .userAgent(USER_AGENT_PROPERTY.get(source.getProperties()))
                .maxRetries(MAX_RETRIES_PROPERTY.get(source.getProperties()))
                .build();
    }

    /**
     * Converts a string authentication scheme name to the corresponding {@link HttpAuthScheme}.
     * <p>
     * Maps provider-specific scheme names to standard HTTP authentication schemes:
     * <ul>
     *   <li>"Basic" → {@link HttpAuthScheme#BASIC}</li>
     *   <li>"MSAL" → {@link HttpAuthScheme#BEARER}</li>
     *   <li>Other values → {@link HttpAuthScheme#NONE}</li>
     * </ul>
     * </p>
     *
     * @param name the authentication scheme name, may be null
     * @return the corresponding HTTP authentication scheme, never null
     */
    private static HttpAuthScheme toHttpAuthScheme(@Nullable String name) {
        if (name != null) {
            switch (name) {
                case BASIC_AUTH_SCHEME:
                    return HttpAuthScheme.BASIC;
                case MSAL_AUTH_SCHEME:
                    return HttpAuthScheme.BEARER;
            }
        }
        return HttpAuthScheme.NONE;
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
         * @param scheme  the authentication scheme being used
         */
        @Override
        public void onOpen(@NonNull HttpRequest request, @NonNull Proxy proxy, @NonNull HttpAuthScheme scheme) {
            openTimestamp = System.currentTimeMillis();
            String message = WebEvents.onQuery(request.getMethod().name(), request.getQuery(), proxy);
            if (!HttpAuthScheme.NONE.equals(scheme)) {
                message += " with auth '" + scheme.name() + "'";
            }
            listener.accept(message);
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
         * Reports an HTTP redirect from one URL to another.
         *
         * @param oldUrl the original URL requested
         * @param newUrl the new URL after redirection
         */
        @Override
        public void onRedirection(@NonNull URL oldUrl, @NonNull URL newUrl) {
            listener.accept(WebEvents.onRedirection(oldUrl, newUrl));
        }

        /**
         * Reports a change in authentication scheme during a request.
         *
         * @param url       the URL requiring authentication
         * @param oldScheme the previously attempted authentication scheme
         * @param newScheme the new authentication scheme being tried
         */
        @Override
        public void onUnauthorized(@NonNull URL url, @NonNull HttpAuthScheme oldScheme, @NonNull HttpAuthScheme newScheme) {
            listener.accept(String.format(Locale.ROOT, "Authenticating %s with '%s'", url, newScheme.name()));
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

    /**
     * HTTP authenticator that delegates to multiple {@link Authenticator} implementations.
     * <p>
     * Manages password authentication and authentication invalidation across multiple providers,
     * ensuring requests are only authenticated for the correct host and port. Reports errors
     * through event and error listeners as appropriate.
     * </p>
     */
    @lombok.AllArgsConstructor
    private static final class RiHttpAuthenticator implements HttpAuthenticator {

        /**
         * The web source being authenticated for.
         */
        @lombok.NonNull
        private final WebSource source;

        /**
         * List of authenticators to delegate to.
         */
        @lombok.NonNull
        private final List<Authenticator> authenticators;

        /**
         * Caching layer for authentication credentials.
         */
        private final @NonNull WebCaching caching;

        /**
         * Optional listener for reporting authentication events.
         */
        private final @Nullable EventListener onEvent;

        /**
         * Optional listener for reporting authentication errors.
         */
        private final @Nullable ErrorListener onError;

        /**
         * Retrieves password authentication for the specified URL.
         * <p>
         * Checks that the URL matches the source endpoint, then delegates to the first
         * authenticator that can provide credentials.
         * </p>
         *
         * @param url the URL requiring authentication
         * @return password authentication if available, null otherwise
         */
        @Override
        public @Nullable PasswordAuthentication getPasswordAuthentication(@NonNull URL url) {
            if (isDifferentAuthScope(url)) {
                return null;
            }
            return authenticators.stream()
                    .map(this::getPasswordAuthentication)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        /**
         * Invalidates cached authentication credentials for the specified URL.
         * <p>
         * Checks that the URL matches the source endpoint, then delegates invalidation
         * to all configured authenticators.
         * </p>
         *
         * @param url the URL whose authentication should be invalidated
         */
        @Override
        public void invalidate(@NonNull URL url) {
            if (isDifferentAuthScope(url)) {
                return;
            }
            authenticators.forEach(this::invalidate);
        }

        /**
         * Checks whether the given URL has a different authentication scope than the configured source.
         * <p>
         * Compares the host and port of the URL with the source endpoint to ensure authentication
         * is only applied to the correct target.
         * </p>
         *
         * @param url the URL to check
         * @return true if the URL is in a different auth scope, false otherwise
         */
        private boolean isDifferentAuthScope(URL url) {
            return !url.getHost().equals(source.getEndpoint().getHost())
                    || url.getPort() != source.getEndpoint().getPort();
        }

        /**
         * Attempts to retrieve password authentication from a single authenticator.
         * <p>
         * Calls the authenticator's authentication method and catches any exceptions,
         * logging them through the event listener if available.
         * </p>
         *
         * @param authenticator the authenticator to query
         * @return password authentication if available, null if not or if an error occurs
         */
        private PasswordAuthentication getPasswordAuthentication(Authenticator authenticator) {
            try {
                return authenticator.getPasswordAuthenticationOrNull(source, caching, onEvent, onError);
            } catch (IOException ex) {
                if (onEvent != null) {
                    onEvent.accept(authenticator.getAuthenticatorId(), "Failed to get password authentication: " + ex.getMessage());
                }
                return null;
            }
        }

        /**
         * Invalidates cached authentication credentials from a single authenticator.
         * <p>
         * Calls the authenticator's invalidation method and catches any exceptions,
         * logging them through the event listener if available.
         * </p>
         *
         * @param authenticator the authenticator to invalidate credentials in
         */
        private void invalidate(Authenticator authenticator) {
            try {
                authenticator.invalidateAuthentication(source, caching, onEvent, onError);
            } catch (IOException ex) {
                if (onEvent != null) {
                    onEvent.accept(authenticator.getAuthenticatorId(), "Failed to invalidate password authentication: " + ex.getMessage());
                }
            }
        }
    }
}
