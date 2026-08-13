package sdmxdl.provider.ri.http;

import nbbrd.io.http.HttpClient;
import nbbrd.io.http.ext.CacheEventListener;
import nbbrd.io.http.ext.CacheStore;
import nbbrd.io.http.ext.CachingDecorator;
import nbbrd.io.text.BooleanProperty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.EventListener;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.provider.Slow;
import sdmxdl.provider.caching.DiskCache;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;

import static sdmxdl.web.spi.Driver.DRIVER_PROPERTY_PREFIX;

public final class CachingDecoration implements HttpDecoration {

    /**
     * Property that enables (default) or disables client-side HTTP caching.
     * <p>
     * When disabled, requests bypass the shared HTTP cache entirely, so every request
     * is forwarded to the underlying source. This is mainly intended for sources that
     * should always be queried live or for troubleshooting.
     * </p>
     */
    @PropertyDefinition
    public static final BooleanProperty HTTP_CACHING_PROPERTY =
            BooleanProperty.of(DRIVER_PROPERTY_PREFIX + ".httpCaching", true);

    @lombok.experimental.Delegate
    private final HttpDecoration support = HttpDecorationSupport.builder()
            .name("Caching")
            .property(HTTP_CACHING_PROPERTY)
            .superFactory(this::decorate)
            .build();

    @lombok.Getter(value = lombok.AccessLevel.PRIVATE, lazy = true)
    private final CacheStore sharedStore = initSharedStore();

    private HttpClient decorate(HttpFactory d, WebSource s, WebContext c) {
        HttpClient original = d.createHttpClient(s, c);
        if (!HTTP_CACHING_PROPERTY.get(s.getProperties())) {
            return original;
        }
        CacheStore cacheStore = getSharedStore();
        if (cacheStore != null) {
            return CachingDecorator
                    .builder()
                    .decorated(original)
                    .listener(toListener(c.getEventListener(s)))
                    .store(cacheStore)
                    .build();
        }
        return original;
    }

    private static @NonNull CacheEventListener toListener(@Nullable EventListener onEvent) {
        return onEvent != null
                ? CacheEventListener.basic(msg -> onEvent.accept(MARKER, msg, 1))
                : CacheEventListener.noOp();
    }

    @Slow
    private static @Nullable CacheStore initSharedStore() {
        try {
            return CacheStore.ofDisk(DiskCache.SDMXDL_TMP_DIR.resolve("http"), 100 * 1000 * 1000);
        } catch (IOException ignore) {
            return null;
        }
    }
}
