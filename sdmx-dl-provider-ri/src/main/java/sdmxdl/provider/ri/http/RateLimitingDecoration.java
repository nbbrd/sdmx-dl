package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.http.ext.*;
import sdmxdl.EventListener;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.time.Duration;

public final class RateLimitingDecoration implements HttpDecoration {

    private static final @lombok.NonNull RateLimiter DEFAULT_RATE_LIMITER = RateLimiter.unlimitedAdaptive(Duration.ofSeconds(120));

    @lombok.experimental.Delegate
    private final HttpDecoration support = HttpDecorationSupport.builder()
            .name("Rate-limiting")
            .superFactory(RateLimitingDecoration::decorate)
            .build();

    private static final RateLimiterRegistry REGISTRY = RateLimiterRegistry.of(() -> DEFAULT_RATE_LIMITER);

    private static HttpClient decorate(HttpFactory d, WebSource s, WebContext c) {
        return RateLimitingDecorator
                .builder()
                .decorated(d.createHttpClient(s, c))
                .rateLimiterProvider(RateLimiterProvider.perHost(REGISTRY))
                .listener(toListener(c.getEventListener(s)))
                .build();
    }

    private static RateLimitingListener toListener(EventListener onEvent) {
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
