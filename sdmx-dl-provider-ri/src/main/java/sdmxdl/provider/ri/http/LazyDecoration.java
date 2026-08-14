package sdmxdl.provider.ri.http;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.http.HttpResponse;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Decorator for {@link HttpClient} that implements lazy initialization.
 * <p>
 * This decorator wraps an HTTP client factory and defers the actual client instantiation
 * until the first request is made. This is useful for reducing startup overhead when
 * multiple clients may not be immediately used.
 * </p>
 */
public final class LazyDecoration implements HttpDecoration {

    /**
     * Delegates HTTP client decoration to the support implementation.
     */
    @lombok.experimental.Delegate
    private final HttpDecoration support = HttpDecorationSupport.builder()
            .name("Lazy")
            .superFactory(LazyDecoration::decorate)
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
    private static HttpClient decorate(HttpFactory d, WebSource s, WebContext c) {
        return new LazyHttpClient(() -> d.createHttpClient(s, c));
    }

    @lombok.AllArgsConstructor
    private static final class LazyHttpClient implements HttpClient {

        private final Supplier<HttpClient> delegateSupplier;

        @lombok.Getter(value = AccessLevel.PRIVATE, lazy = true)
        private final HttpClient delegate = delegateSupplier.get();

        @Override
        public @NonNull String getDescription() {
            return "Lazy " + getDelegate().getDescription();
        }

        @Override
        public @NonNull HttpResponse send(@NonNull HttpRequest request) throws IOException {
            return getDelegate().send(request);
        }
    }
}
