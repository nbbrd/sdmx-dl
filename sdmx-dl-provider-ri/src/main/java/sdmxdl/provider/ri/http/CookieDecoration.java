package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpHeaders;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.http.HttpResponse;
import nbbrd.io.text.BooleanProperty;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static sdmxdl.web.spi.Driver.DRIVER_PROPERTY_PREFIX;

/**
 * Decoration that adds per-connection HTTP cookie handling.
 * <p>
 * Some sources sit behind an access gateway (e.g. MobilityGuard) that performs a cookie
 * challenge: the first request is answered with a redirect that sets a session cookie and only
 * returns the expected payload once that cookie is sent back. Since the underlying HTTP stack has
 * no cookie support, cookies are dropped across the redirect chain and the client ends up on the
 * challenge page instead of the actual resource.
 * <p>
 * This decoration stores cookies received in responses and replays them on subsequent requests,
 * mimicking the behavior of a web browser. It must be placed <em>inside</em> the redirect
 * decoration so that cookies are carried across redirect hops.
 * <p>
 * It is disabled by default (see {@link #COOKIE_PROPERTY}) since only a few sources require it,
 * and must be explicitly enabled per source.
 */
public final class CookieDecoration implements HttpDecoration {

    /**
     * Property that enables (default: disabled) per-connection HTTP cookie handling.
     * <p>
     * Only sources sitting behind a cookie-challenge gateway (e.g. MobilityGuard) need it; it is
     * therefore off by default and must be enabled per source.
     * </p>
     */
    @PropertyDefinition
    public static final BooleanProperty COOKIE_PROPERTY =
            BooleanProperty.of(DRIVER_PROPERTY_PREFIX + ".cookie", false);

    @lombok.experimental.Delegate
    private final HttpDecoration support = HttpDecorationSupport.builder()
            .name("Cookie")
            .property(COOKIE_PROPERTY)
            .superFactory(CookieDecoration::decorate)
            .build();

    private static HttpClient decorate(HttpFactory d, WebSource s, WebContext c) {
        HttpClient original = d.createHttpClient(s, c);
        if (!COOKIE_PROPERTY.get(s.getProperties())) {
            return original;
        }
        // Per-connection cookie store: browser-like acceptance to support cross-subdomain gateways.
        CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        return new CookieHttpClient(original, cookieManager);
    }

    @lombok.AllArgsConstructor
    private static final class CookieHttpClient implements HttpClient {

        @lombok.NonNull
        private final HttpClient delegate;

        @lombok.NonNull
        private final CookieHandler cookieHandler;

        @Override
        public @NonNull String getDescription() {
            return delegate.getDescription();
        }

        @Override
        public @NonNull HttpResponse send(@NonNull HttpRequest request) throws IOException {
            HttpRequest withCookies = addCookies(request);
            HttpResponse response = delegate.send(withCookies);
            storeCookies(withCookies.getQuery(), response);
            return response;
        }

        private HttpRequest addCookies(HttpRequest request) throws IOException {
            Map<String, List<String>> cookieHeaders = cookieHandler.get(request.getQuery(), request.getHeaders().getMap());
            HttpHeaders.Builder headers = request.getHeaders().toBuilder();
            boolean hasCookies = false;
            for (Map.Entry<String, List<String>> entry : cookieHeaders.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    // All cookies must be sent as a single header value separated by "; ".
                    // Adding them as multiple headers would cause the underlying
                    // HttpURLConnection#setRequestProperty to overwrite, keeping only one
                    // cookie and breaking cookie-challenge gateways (e.g. MobilityGuard).
                    headers.put(entry.getKey(), String.join("; ", entry.getValue()));
                    hasCookies = true;
                }
            }
            return hasCookies ? request.toBuilder().headers(headers.build()).build() : request;
        }

        private void storeCookies(URI uri, HttpResponse response) throws IOException {
            cookieHandler.put(uri, response.getHeaders().getMap());
        }
    }
}
