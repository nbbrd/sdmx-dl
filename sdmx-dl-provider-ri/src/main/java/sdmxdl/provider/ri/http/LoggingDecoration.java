package sdmxdl.provider.ri.http;

import nbbrd.io.http.HttpClient;
import nbbrd.io.http.ext.LoggingDecorator;
import nbbrd.io.http.ext.LoggingHandler;
import sdmxdl.EventListener;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

public final class LoggingDecoration implements HttpDecoration {

    @lombok.experimental.Delegate
    private final HttpDecoration support = HttpDecorationSupport.builder()
            .name("Logging")
            .superFactory(LoggingDecoration::decorate)
            .build();

    private static HttpClient decorate(HttpFactory d, WebSource s, WebContext c) {
        HttpClient original = d.createHttpClient(s, c);
        EventListener onEvent = c.getEventListener(s);
        if (onEvent != null) {
            return new LoggingDecorator(original, LoggingHandler.basic(message -> onEvent.accept(HttpDecoration.MARKER, message, 1)));
        }
        return original;
    }
}
