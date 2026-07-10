package sdmxdl.provider.ri.http;

import nbbrd.io.http.HttpClient;
import nbbrd.io.http.UrlConnectionHttpClient;
import nbbrd.io.http.ext.RetryDecorator;
import nbbrd.io.http.ext.RetryListener;
import nbbrd.io.text.IntProperty;
import sdmxdl.EventListener;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import static sdmxdl.web.spi.Driver.DRIVER_PROPERTY_PREFIX;

public final class RetryDecoration implements HttpDecoration {

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
    private final HttpDecoration support = HttpDecorationSupport.builder()
            .name("Retry")
            .property(MAX_RETRIES_PROPERTY)
            .superFactory(RetryDecoration::decorate)
            .build();

    private static HttpClient decorate(HttpFactory d, WebSource s, WebContext c) {
        return new RetryDecorator(
                d.create(s, c),
                MAX_RETRIES_PROPERTY.get(s.getProperties()),
                toListener(c.getEventListener(s)));
    }

    private static RetryListener toListener(EventListener onEvent) {
        return onEvent != null
                ? (request, attempt, cause) -> onEvent.accept(MARKER, "Retrying request to " + request.getQuery() + " (attempt " + attempt + ") due to: " + cause, 1)
                : RetryListener.noOp();
    }
}
