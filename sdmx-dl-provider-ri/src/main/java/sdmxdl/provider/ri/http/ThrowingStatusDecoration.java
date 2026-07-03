package sdmxdl.provider.ri.http;

import nbbrd.io.http.HttpClient;
import nbbrd.io.http.ext.ThrowingStatusDecorator;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

public final class ThrowingStatusDecoration implements HttpDecoration {

    @lombok.experimental.Delegate
    private final HttpDecoration support = HttpDecorationSupport.builder()
            .name("Throwing")
            .superFactory(ThrowingStatusDecoration::decorate)
            .build();

    private static HttpClient decorate(HttpFactory d, WebSource s, WebContext c) {
        return new ThrowingStatusDecorator(d.create(s, c), ThrowingStatusDecorator.DEFAULT_SHOULD_THROW);
    }
}
