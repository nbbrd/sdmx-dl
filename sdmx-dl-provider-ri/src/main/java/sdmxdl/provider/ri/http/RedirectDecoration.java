package sdmxdl.provider.ri.http;

import nbbrd.io.http.HttpClient;
import nbbrd.io.http.ext.RedirectDecorator;
import nbbrd.io.http.ext.RedirectListener;
import sdmxdl.EventListener;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import static sdmxdl.provider.web.DriverProperties.MAX_REDIRECTS_PROPERTY;

public final class RedirectDecoration implements HttpDecoration {

    @lombok.experimental.Delegate
    private final HttpDecoration support = HttpDecorationSupport.builder()
            .name("Redirect")
            .property(MAX_REDIRECTS_PROPERTY)
            .superFactory(RedirectDecoration::decorate)
            .build();

    private static HttpClient decorate(HttpFactory d, WebSource s, WebContext c) {
        return new RedirectDecorator(
                d.createHttpClient(s, c),
                MAX_REDIRECTS_PROPERTY.get(s.getProperties()),
                toListener(c.getEventListener(s)));
    }

    private static RedirectListener toListener(EventListener onEvent) {
        return onEvent != null
                ? (oldUri, newUri) -> onEvent.accept(MARKER, "Redirecting request from " + oldUri + " to " + newUri, 1)
                : RedirectListener.noOp();
    }
}
