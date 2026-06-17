package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpHeaders;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.http.HttpResponse;
import sdmxdl.EventListener;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Decorator for {@link HttpClient} that tracks and reports the number of bytes downloaded.
 * <p>
 * This decorator wraps an HTTP client and monitors the byte count of all data read from HTTP responses.
 * Byte count information is reported via the {@link EventListener} associated with the web source.
 * </p>
 */
public final class ByteCountingHttpClientDecorator implements HttpClientDecorator {

    /**
     * Delegates HTTP client decoration to the support implementation.
     */
    @lombok.experimental.Delegate
    private final HttpClientDecorator support = HttpClientDecoratorSupport.builder()
            .name("ByteCounting")
            .superFactory(ByteCountingHttpClientDecorator::decorate)
            .build();

    /**
     * Decorates an HTTP client with byte counting capabilities.
     * <p>
     * If an event listener is available from the web context, wraps the client with
     * a {@link ByteCountingClient} that tracks bytes read from all responses.
     * </p>
     *
     * @param d the HTTP client factory to create the base client
     * @param s the web source providing configuration
     * @param c the web context containing event listeners
     * @return an HTTP client, optionally wrapped with byte counting functionality
     */
    private static HttpClient decorate(HttpClientFactory d, WebSource s, WebContext c) {
        HttpClient original = d.create(s, c);
        EventListener onEvent = c.getEventListener(s);
        if (onEvent != null) {
            return new ByteCountingClient(original, message -> onEvent.accept("RI_HTTP", message, 1));
        }
        return original;
    }

    @MightBePromoted
    @lombok.AllArgsConstructor
    private static final class ByteCountingClient implements HttpClient {

        /**
         * The underlying HTTP client to delegate requests to.
         */
        @NonNull
        private final HttpClient delegate;

        /**
         * Listener that receives byte count messages.
         */
        @NonNull
        private final Consumer<CharSequence> listener;

        @Override
        public @NonNull String getDescription() {
            return "Byte counting " + delegate.getDescription();
        }

        @Override
        public @NonNull HttpResponse send(@NonNull HttpRequest request) throws IOException {
            return new ByteCountingResponse(delegate.send(request), listener);
        }
    }

    /**
     * HTTP response wrapper that tracks the number of bytes read from the response body.
     * <p>
     * Monitors all bytes read through input stream access and reports the total count
     * via the listener when the response is closed.
     * </p>
     */
    @MightBePromoted
    @lombok.AllArgsConstructor
    private static final class ByteCountingResponse implements HttpResponse {

        /**
         * The underlying HTTP response to delegate to.
         */
        @NonNull
        private final HttpResponse delegate;

        /**
         * Listener that receives byte count messages.
         */
        @NonNull
        private final Consumer<CharSequence> listener;

        /**
         * Counter for tracking total bytes read from response body.
         */
        private final AtomicLong byteCount = new AtomicLong();

        @Override
        public @NonNull nbbrd.io.net.MediaType getContentType() throws IOException {
            return delegate.getContentType();
        }

        @Override
        public long getContentLength() throws IOException {
            return delegate.getContentLength();
        }

        @Override
        public @NonNull HttpHeaders getHeaders() throws IOException {
            return delegate.getHeaders();
        }

        /**
         * Returns the response body as an input stream with byte counting.
         *
         * @return an input stream that tracks bytes read through this method
         * @throws IOException if an I/O error occurs
         */
        @Override
        public @NonNull InputStream getBody() throws IOException {
            return new CountingInputStream(delegate.getBody(), byteCount);
        }

        /**
         * Returns the response body as a disconnecting input stream with byte counting.
         *
         * @return an input stream that tracks bytes read and disconnects on close
         * @throws IOException if an I/O error occurs
         */
        @Override
        public @NonNull InputStream asDisconnectingInputStream() throws IOException {
            return new CountingInputStream(delegate.asDisconnectingInputStream(), byteCount);
        }

        /**
         * Closes the response and reports the total bytes read if any were recorded.
         *
         * @throws IOException if an I/O error occurs while closing
         */
        @Override
        public void close() throws IOException {
            try {
                delegate.close();
            } finally {
                long bytes = byteCount.get();
                if (bytes > 0) {
                    listener.accept(String.format(Locale.ROOT, "Read %s", formatBytes(bytes)));
                }
            }
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

    /**
     * Input stream wrapper that counts bytes read.
     * <p>
     * Extends {@link FilterInputStream} to transparently count all bytes read from the
     * wrapped input stream, updating a shared {@link AtomicLong} counter.
     * </p>
     */
    @MightBePromoted
    private static final class CountingInputStream extends FilterInputStream {

        /**
         * Atomic counter for tracking bytes read.
         */
        private final AtomicLong counter;

        /**
         * Creates a counting input stream.
         *
         * @param in the input stream to wrap and count bytes from
         * @param counter the atomic counter to update with byte count
         */
        CountingInputStream(InputStream in, AtomicLong counter) {
            super(in);
            this.counter = counter;
        }

        /**
         * Reads a single byte and increments the counter.
         *
         * @return the byte read, or -1 if EOF is reached
         * @throws IOException if an I/O error occurs
         */
        @Override
        public int read() throws IOException {
            int result = super.read();
            if (result != -1) counter.incrementAndGet();
            return result;
        }

        /**
         * Reads bytes into the specified array and increments the counter by the number read.
         *
         * @param b the array to read into
         * @param off the offset in the array
         * @param len the maximum number of bytes to read
         * @return the number of bytes read, or -1 if EOF is reached
         * @throws IOException if an I/O error occurs
         */
        @Override
        @SuppressWarnings("NullableProblems")
        public int read(byte[] b, int off, int len) throws IOException {
            int result = super.read(b, off, len);
            if (result > 0) counter.addAndGet(result);
            return result;
        }
    }
}
