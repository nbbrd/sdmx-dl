package sdmxdl.provider.ri.http;

import nbbrd.io.http.HttpClient;
import nbbrd.io.http.ext.ByteCountingDecorator;
import sdmxdl.EventListener;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.util.Locale;

/**
 * Decorator for {@link HttpClient} that tracks and reports the number of bytes downloaded.
 * <p>
 * This decorator wraps an HTTP client and monitors the byte count of all data read from HTTP responses.
 * Byte count information is reported via the {@link EventListener} associated with the web source.
 * </p>
 */
public final class ByteCountingDecoration implements HttpDecoration {

    /**
     * Delegates HTTP client decoration to the support implementation.
     */
    @lombok.experimental.Delegate
    private final HttpDecoration support = HttpDecorationSupport.builder()
            .name("ByteCounting")
            .superFactory(ByteCountingDecoration::decorate)
            .build();

    /**
     * Decorates an HTTP client with byte counting capabilities.
     * <p>
     * If an event listener is available from the web context, wraps the client with
     * a {@link ByteCountingDecorator} that tracks bytes read from all responses.
     * </p>
     *
     * @param d the HTTP client factory to create the base client
     * @param s the web source providing configuration
     * @param c the web context containing event listeners
     * @return an HTTP client, optionally wrapped with byte counting functionality
     */
    private static HttpClient decorate(HttpFactory d, WebSource s, WebContext c) {
        HttpClient original = d.create(s, c);
        EventListener onEvent = c.getEventListener(s);
        if (onEvent != null) {
            return new ByteCountingDecorator(original, bytes -> onEvent.accept(MARKER, "Read " + formatBytes(bytes), 1));
        }
        return original;
    }

    /**
     * Formats a byte count as a human-readable string.
     *
     * @param bytes the number of bytes to format
     * @return formatted byte string (e.g., "1.5MB", "512KB", "1024B")
     */
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024 * 1024) return String.format(Locale.ROOT, "%.1fKB", bytes / 1024.0);
        return String.format(Locale.ROOT, "%.1fMB", bytes / (1024.0 * 1024.0));
    }
}
