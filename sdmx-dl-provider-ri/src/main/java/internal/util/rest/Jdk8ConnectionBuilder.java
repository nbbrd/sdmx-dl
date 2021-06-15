package internal.util.rest;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Jdk8ConnectionBuilder implements DefaultClient.ConnectionBuilder {

    @lombok.Setter
    private URL query;

    @lombok.Setter
    private Proxy proxy;

    @lombok.Setter
    private int readTimeout;

    @lombok.Setter
    private int connectTimeout;

    private SSLSocketFactory sslSocketFactory;

    private HostnameVerifier hostnameVerifier;

    private Map<String, String> headers = new HashMap<>();

    @Override
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    @Override
    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    @Override
    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    @Override
    public DefaultClient.Connection open() throws IOException {
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
        headers.forEach(http::setRequestProperty);
        http.setInstanceFollowRedirects(false);

        return new Jdk8Connection(http);
    }

    @lombok.AllArgsConstructor
    static final class Jdk8Connection implements DefaultClient.Connection {

        @lombok.NonNull
        private final HttpURLConnection http;

        @Override
        public int getResponseCode() throws IOException {
            return http.getResponseCode();
        }

        @Override
        public String getHeaderField(String key) {
            return http.getHeaderField(key);
        }

        @Override
        public String getResponseMessage() throws IOException {
            return http.getResponseMessage();
        }

        @Override
        public Map<String, List<String>> getHeaders() {
            return http.getHeaderFields();
        }

        @Override
        public URL getQuery() {
            return http.getURL();
        }

        @Override
        public String getContentType() {
            return http.getContentType();
        }

        @Override
        public String getContentEncoding() {
            return http.getContentEncoding();
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
