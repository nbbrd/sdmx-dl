package sdmxdl.provider.ri.http;

import nbbrd.design.VisibleForTesting;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpResponse;
import nbbrd.io.http.ext.MetricsDecorator;
import nbbrd.io.http.ext.MetricsEvent;
import sdmxdl.EventListener;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.util.Locale;

/**
 * {@link HttpDecoration} that reports per-request metrics (status code, bytes
 * downloaded, time to first byte, and total duration) to the source event
 * listener in a human-readable format.
 *
 * <p>The decorator is a no-op when no event listener is registered for the
 * source, so it adds no overhead in that case.</p>
 */
public final class MetricsDecoration implements HttpDecoration {

    @lombok.experimental.Delegate
    private final HttpDecoration support = HttpDecorationSupport.builder()
            .name("Metrics")
            .superFactory(MetricsDecoration::decorate)
            .build();

    private static HttpClient decorate(HttpFactory d, WebSource s, WebContext c) {
        HttpClient original = d.createHttpClient(s, c);
        EventListener onEvent = c.getEventListener(s);
        if (onEvent != null) {
            return new MetricsDecorator(original, metrics -> onEvent.accept(MARKER, formatEvent(metrics), 1));
        }
        return original;
    }

    /**
     * Formats a {@link MetricsEvent} into a concise, human-readable string.
     *
     * <p>Format: {@code <status> | <bytes> | ttfb=<networkTime> total=<totalTime>}</p>
     * <p>Example: {@code 200 | 1.5MB | ttfb=342ms total=1.2s}</p>
     *
     * <p>The URI and request method are intentionally omitted because they are
     * already reported by other decorators.</p>
     *
     * @param event the metrics event to format
     * @return formatted metrics string
     */
    @VisibleForTesting
    static String formatEvent(MetricsEvent event) {
        StringBuilder sb = new StringBuilder();
        int status = event.getResponseStatusCode();
        sb.append(status != HttpResponse.NO_STATUS_CODE ? status : "???");
        sb.append(" | ").append(formatBytes(event.getResponseBytesRead()));
        sb.append(" | ttfb=").append(formatDuration(event.getNetworkNanos()));
        sb.append(" total=").append(formatDuration(event.getTotalNanos()));
        return sb.toString();
    }

    /**
     * Formats a byte count as a human-readable string.
     *
     * <p>Uses binary prefixes (1 KB = 1024 B):</p>
     * <ul>
     *   <li>&lt; 1 KB → {@code "NB"} (e.g., {@code "512B"})</li>
     *   <li>&lt; 1 MB → {@code "N.NKB"} (e.g., {@code "1.5KB"})</li>
     *   <li>≥ 1 MB → {@code "N.NMB"} (e.g., {@code "2.3MB"})</li>
     * </ul>
     *
     * @param bytes the number of bytes to format; must be non-negative
     * @return formatted byte string
     */
    @VisibleForTesting
    static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024 * 1024) return String.format(Locale.ROOT, "%.1fKB", bytes / 1024.0);
        return String.format(Locale.ROOT, "%.1fMB", bytes / (1024.0 * 1024.0));
    }

    /**
     * Formats a nanosecond duration as a human-readable string.
     *
     * <ul>
     *   <li>&lt; 1 ms → {@code "Nµs"} (e.g., {@code "500µs"})</li>
     *   <li>&lt; 1 s → {@code "Nms"} (e.g., {@code "342ms"})</li>
     *   <li>≥ 1 s → {@code "N.Ns"} (e.g., {@code "1.2s"})</li>
     * </ul>
     *
     * @param nanos the duration in nanoseconds; must be non-negative
     * @return formatted duration string
     */
    @VisibleForTesting
    static String formatDuration(long nanos) {
        if (nanos < 1_000_000) return nanos / 1_000 + "µs";
        if (nanos < 1_000_000_000) return String.format(Locale.ROOT, "%.0fms", nanos / 1_000_000.0);
        return String.format(Locale.ROOT, "%.1fs", nanos / 1_000_000_000.0);
    }
}
