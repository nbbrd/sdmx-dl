package sdmxdl.web;

import java.io.IOException;
import java.net.URI;
import org.jspecify.annotations.Nullable;

/**
 * Signals that a web source answered a request with an error status code.
 *
 * <p>Drivers throw it from {@link sdmxdl.Connection#testConnection()} to report that a source is
 * reachable but failing.
 */
public final class HttpStatusException extends IOException {

    private final int statusCode;

    private final @Nullable URI uri;

    public HttpStatusException(int statusCode, @Nullable URI uri, @Nullable Throwable cause) {
        super("HTTP " + statusCode + (uri != null ? " on '" + uri + "'" : ""), cause);
        this.statusCode = statusCode;
        this.uri = uri;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public @Nullable URI getUri() {
        return uri;
    }
}
