package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.ext.AuthScheme;
import nbbrd.io.http.ext.AuthenticatingDecorator;
import nbbrd.io.http.ext.AuthenticatingListener;
import nbbrd.io.http.ext.Authenticator;
import org.jspecify.annotations.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import static sdmxdl.provider.ri.drivers.AuthSchemes.BASIC_AUTH_SCHEME;
import static sdmxdl.provider.ri.drivers.AuthSchemes.MSAL_AUTH_SCHEME;
import static sdmxdl.provider.web.DriverProperties.AUTH_SCHEME_PROPERTY;

public final class AuthenticatingDecoration implements HttpDecoration {

    @lombok.experimental.Delegate
    private final HttpDecoration support = HttpDecorationSupport.builder()
            .name("Authenticating")
            .property(AUTH_SCHEME_PROPERTY)
            .superFactory(AuthenticatingDecoration::decorate)
            .build();

    private static HttpClient decorate(HttpFactory d, WebSource s, WebContext c) {
        EventListener onEvent = c.getEventListener(s);
        ErrorListener onError = c.getErrorListener(s);
        return new AuthenticatingDecorator(
                d.create(s, c),
                new RiHttpAuthenticator(s, c.getAuthenticators(), c.getCaching(), onEvent, onError),
                toHttpAuthScheme(AUTH_SCHEME_PROPERTY.get(s.getProperties())),
                toListener(onEvent));
    }

    private static AuthenticatingListener toListener(EventListener onEvent) {
        return onEvent != null
                ? (uri, oldScheme, newScheme) -> onEvent.accept(MARKER, "Authentication scheme changed for " + uri + ": " + oldScheme + " → " + newScheme)
                : AuthenticatingListener.noOp();
    }

    /**
     * Converts a string authentication scheme name to the corresponding {@link AuthScheme}.
     * <p>
     * Maps provider-specific scheme names to standard HTTP authentication schemes:
     * <ul>
     *   <li>"Basic" → {@link AuthScheme#BASIC}</li>
     *   <li>"MSAL" → {@link AuthScheme#BEARER}</li>
     *   <li>Other values → {@link AuthScheme#NONE}</li>
     * </ul>
     * </p>
     *
     * @param name the authentication scheme name, may be null
     * @return the corresponding HTTP authentication scheme, never null
     */
    private static AuthScheme toHttpAuthScheme(@Nullable String name) {
        if (name != null) {
            switch (name) {
                case BASIC_AUTH_SCHEME:
                    return AuthScheme.BASIC;
                case MSAL_AUTH_SCHEME:
                    return AuthScheme.BEARER;
            }
        }
        return AuthScheme.NONE;
    }

    /**
     * HTTP authenticator that delegates to multiple {@link sdmxdl.web.spi.Authenticator} implementations.
     * <p>
     * Manages password authentication and authentication invalidation across multiple providers,
     * ensuring requests are only authenticated for the correct host and port. Reports errors
     * through event and error listeners as appropriate.
     * </p>
     */
    @lombok.AllArgsConstructor
    private static final class RiHttpAuthenticator implements Authenticator {

        /**
         * The web source being authenticated for.
         */
        @lombok.NonNull
        private final WebSource source;

        /**
         * List of authenticators to delegate to.
         */
        @lombok.NonNull
        private final List<sdmxdl.web.spi.Authenticator> authenticators;

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
         * @param uri the URL requiring authentication
         * @return password authentication if available, null otherwise
         */
        @Override
        public @Nullable PasswordAuthentication getPasswordAuthentication(@NonNull URI uri) {
            if (isDifferentAuthScope(uri)) {
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
         * @param uri the URL whose authentication should be invalidated
         */
        @Override
        public void invalidate(@NonNull URI uri) {
            if (isDifferentAuthScope(uri)) {
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
         * @param uri the URL to check
         * @return true if the URL is in a different auth scope, false otherwise
         */
        private boolean isDifferentAuthScope(URI uri) {
            return !uri.getHost().equals(source.getEndpoint().getHost())
                    || uri.getPort() != source.getEndpoint().getPort();
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
        private PasswordAuthentication getPasswordAuthentication(sdmxdl.web.spi.Authenticator authenticator) {
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
        private void invalidate(sdmxdl.web.spi.Authenticator authenticator) {
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
