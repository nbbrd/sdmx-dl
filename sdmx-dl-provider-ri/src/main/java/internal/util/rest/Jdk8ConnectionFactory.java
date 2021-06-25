package internal.util.rest;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class Jdk8ConnectionFactory implements DefaultClient.ConnectionFactory {

    @Override
    public DefaultClient.Connection open(URL query, Proxy proxy, int readTimeout, int connectTimeout,
                                         SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier,
                                         Map<String, List<String>> headers) throws IOException {
        URLConnection conn = query.openConnection(proxy);
        conn.setReadTimeout(readTimeout);
        conn.setConnectTimeout(connectTimeout);

        if (!(conn instanceof HttpURLConnection)) {
            throw new IOException("Unsupported connection type");
        }

        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
            ((HttpsURLConnection) conn).setHostnameVerifier(hostnameVerifier);
        }

        HttpURLConnection http = (HttpURLConnection) conn;
        http.setRequestMethod("GET");
        http.setInstanceFollowRedirects(false);
        HttpHeadersBuilder.keyValues(headers)
                .forEach(header -> http.setRequestProperty(header.getKey(), header.getValue()));

        return new Jdk8Connection(http);
    }

    @lombok.AllArgsConstructor
    static final class Jdk8Connection implements DefaultClient.Connection {

        @lombok.NonNull
        private final HttpURLConnection http;

        @Override
        public int getStatusCode() throws IOException {
            return http.getResponseCode();
        }

        @Override
        public @Nullable String getStatusMessage() throws IOException {
            return http.getResponseMessage();
        }

        @Override
        public @NonNull Optional<String> getHeaderFirstValue(@NonNull String name) {
            return Optional.ofNullable(http.getHeaderField(name));
        }

        @Override
        public @NonNull Map<String, List<String>> getHeaders() {
            return http.getHeaderFields();
        }

        @Override
        public URL getQuery() {
            return http.getURL();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return http.getInputStream();
        }

        @Override
        public void close() {
            http.disconnect();
        }
    }
}
