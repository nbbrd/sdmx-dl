package sdmxdl.provider.ri.http;

import nbbrd.io.http.HttpClient;
import nbbrd.io.http.ext.LazyHttpClient;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

/**
 * Decorator for {@link HttpClient} that implements lazy initialization.
 * <p>
 * This decorator wraps an HTTP client factory and defers the actual client instantiation
 * until the first request is made. This is useful for reducing startup overhead when
 * multiple clients may not be immediately used.
 * </p>
 */
public final class LazyHttpClientDecorator implements HttpClientDecorator {

    /**
     * Delegates HTTP client decoration to the support implementation.
     */
    @lombok.experimental.Delegate
    private final HttpClientDecorator support = HttpClientDecoratorSupport.builder()
            .name("Lazy")
            .superFactory(LazyHttpClientDecorator::decorate)
            .build();

    /**
     * Decorates an HTTP client factory with lazy initialization.
     * <p>
     * Wraps the client creation in a {@link LazyHttpClient} that delays instantiation
     * until the first use, allowing for deferred initialization of the underlying client.
     * </p>
     *
     * @param d the HTTP client factory to lazily instantiate
     * @param s the web source providing configuration
     * @param c the web context containing runtime configuration
     * @return a lazy HTTP client that creates the underlying client on first use
     */
    private static HttpClient decorate(HttpClientFactory d, WebSource s, WebContext c) {
        return new LazyHttpClient(() -> d.create(s, c));
    }
}
