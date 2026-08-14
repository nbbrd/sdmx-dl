package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.http.ext.*;
import nbbrd.io.text.BooleanProperty;
import org.jspecify.annotations.Nullable;
import sdmxdl.EventListener;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static sdmxdl.web.spi.Driver.DRIVER_PROPERTY_PREFIX;

public final class RateLimitingDecoration implements HttpDecoration {

    /**
     * Property that enables (default) or disables client-side HTTP rate limiting.
     * <p>
     * When disabled, requests bypass the rate limiter entirely, losing both proactive
     * throttling and reactive {@code 429 Retry-After} back-off. This is mainly intended
     * for sources that enforce their own rate limiting.
     * </p>
     */
    @PropertyDefinition
    public static final BooleanProperty RATE_LIMITING_PROPERTY =
            BooleanProperty.of(DRIVER_PROPERTY_PREFIX + ".rateLimiting", true);

    private static final @lombok.NonNull RateLimiter DEFAULT_RATE_LIMITER = RateLimiter.unlimitedAdaptive(Duration.ofSeconds(120));

    @lombok.experimental.Delegate
    private final HttpDecoration support = HttpDecorationSupport.builder()
            .name("Rate-limiting")
            .property(RATE_LIMITING_PROPERTY)
            .superFactory(RateLimitingDecoration::decorate)
            .build();

    private static final RateLimiterRegistry REGISTRY = RateLimiterRegistry.of(() -> DEFAULT_RATE_LIMITER);

    // Per-host limiter overrides supplied by drivers that know a source-specific limit
    // (e.g. a server-declared rate); consulted before the shared default limiter.
    private static final Map<String, RateLimiter> OVERRIDES = new ConcurrentHashMap<>();

    /**
     * Registers a per-host {@link RateLimiter} to be used by this decoration instead of the
     * shared default, unless one is already registered for that host.
     * <p>
     * This lets a driver enforce a source-specific limit (for example, a server-declared rate)
     * while still benefiting from the correct pipeline order, where rate limiting is applied
     * <em>before</em> error statuses (such as {@code 429}) are turned into exceptions.
     * </p>
     *
     * @param host    the request host the limiter applies to
     * @param limiter the limiter to associate with the host
     */
    public static void putRateLimiterIfAbsent(@NonNull String host, @NonNull RateLimiter limiter) {
        OVERRIDES.putIfAbsent(host, limiter);
    }

    private static HttpClient decorate(HttpFactory d, WebSource s, WebContext c) {
        HttpClient original = d.createHttpClient(s, c);
        if (!RATE_LIMITING_PROPERTY.get(s.getProperties())) {
            return original;
        }
        return RateLimitingDecorator
                .builder()
                .decorated(original)
                .rateLimiterProvider(RateLimitingDecoration::resolveRateLimiter)
                .listener(toListener(c.getEventListener(s)))
                .build();
    }

    private static RateLimiter resolveRateLimiter(HttpRequest request) {
        String host = hostOf(request.getQuery());
        RateLimiter override = OVERRIDES.get(host);
        return override != null ? override : REGISTRY.forKey(host);
    }

    // Mirrors the host-extraction logic of RateLimiterProvider.perHost.
    private static String hostOf(URI uri) {
        String host = uri.getHost();
        if (host != null) {
            return host;
        }
        String authority = uri.getAuthority();
        return authority != null ? authority : uri.toString();
    }

    public static @NonNull RateLimitingListener toListener(@Nullable EventListener onEvent) {
        return onEvent != null ? new EventListenerAdapter(onEvent) : RateLimitingListener.noOp();
    }

    @lombok.AllArgsConstructor
    private static final class EventListenerAdapter implements RateLimitingListener {

        private final @NonNull EventListener onEvent;

        @Override
        public void onRateAdjusted(double oldRate, double newRate) {
            onEvent.accept(MARKER, "Rate adjusted from " + oldRate + " to " + newRate, 1);
        }

        @Override
        public void onRateLimited(@NonNull HttpRequest request, @NonNull Duration waitTime) {
            onEvent.accept(MARKER, "Rate limited request to " + request.getQuery() + ", waiting for " + waitTime, 1);
        }
    }
}
