package sdmxdl.provider.ri.http;

import nbbrd.io.http.HttpClient;
import nbbrd.io.http.ext.CacheEventListener;
import nbbrd.io.http.ext.CacheStore;
import nbbrd.io.http.ext.CachingDecorator;
import sdmxdl.EventListener;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

public final class CachingDecoration implements HttpDecoration {

    @lombok.experimental.Delegate
    private final HttpDecoration support = HttpDecorationSupport.builder()
            .name("Caching")
            .superFactory(this::decorate)
            .build();

    private final CacheStore sharedStore = CacheStore.ofInMemory();

    private HttpClient decorate(HttpFactory d, WebSource s, WebContext c) {
        HttpClient original = d.create(s, c);
        EventListener onEvent = c.getEventListener(s);
        return CachingDecorator
                .builder()
                .decorated(original)
                .listener(
                        onEvent != null
                                ? CacheEventListener.basic(msg -> onEvent.accept(MARKER, msg))
                                : CacheEventListener.noOp()
                )
                .store(sharedStore)
                .build();
    }
}
