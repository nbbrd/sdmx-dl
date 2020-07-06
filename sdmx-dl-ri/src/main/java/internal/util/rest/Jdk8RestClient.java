/*
 * Copyright 2017 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package internal.util.rest;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * @author Philippe Charles
 */
@lombok.Builder(builderClassName = "Builder")
public final class Jdk8RestClient implements RestClient {

    @NonNegative
    private final int readTimeout;

    @NonNegative
    private final int connectTimeout;

    @NonNegative
    private final int maxRedirects;

    @lombok.NonNull
    private final ProxySelector proxySelector;

    @lombok.NonNull
    private final SSLSocketFactory sslSocketFactory;

    @lombok.NonNull
    private final HostnameVerifier hostnameVerifier;

    @lombok.NonNull
    private final EventListener listener;

    @lombok.Singular
    private final Map<String, StreamDecoder> decoders;

    public static Builder builder() {
        return new Builder()
                .readTimeout(NO_TIMEOUT)
                .connectTimeout(NO_TIMEOUT)
                .maxRedirects(DEFAULT_MAX_REDIRECTS)
                .proxySelector(ProxySelector.getDefault())
                .sslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory())
                .hostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier())
                .listener(EventListener.noOp())
                .decoder("gzip", GZIPInputStream::new)
                .decoder("deflate", InflaterInputStream::new);
    }

    @Override
    public Response open(URL query, String mediaType, String langs) throws IOException {
        Objects.requireNonNull(query);
        Objects.requireNonNull(mediaType);
        Objects.requireNonNull(langs);
        return open(query, mediaType, langs, 0);
    }

    private Response open(URL query, String mediaType, String langs, int redirects) throws IOException {
        Proxy proxy = getProxy(query);

        listener.onOpen(query, mediaType, langs, proxy);

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
        http.setRequestProperty(ACCEPT_HEADER, mediaType);
        http.setRequestProperty(ACCEPT_LANGUAGE_HEADER, langs);
        http.addRequestProperty(ACCEPT_ENCODING_HEADER, getEncodingHeader());
        http.setInstanceFollowRedirects(false);

        switch (ResponseType.parse(http.getResponseCode())) {
            case REDIRECTION:
                return redirect(http, mediaType, langs, redirects);
            case SUCCESSFUL:
                return getBody(http);
            default:
                throw getError(http);
        }
    }

    private String getEncodingHeader() {
        return decoders.keySet().stream().collect(Collectors.joining(","));
    }

    private Proxy getProxy(URL url) throws IOException {
        List<Proxy> proxies = proxySelector.select(toURI(url));
        return proxies.isEmpty() ? Proxy.NO_PROXY : proxies.get(0);
    }

    private Response redirect(HttpURLConnection http, String mediaType, String langs, int redirects) throws IOException {
        URL oldUrl;
        URL newUrl;
        try {
            if (redirects == maxRedirects) {
                throw new IOException("Max redirection reached");
            }

            String location = http.getHeaderField(LOCATION_HEADER);
            if (location == null || location.isEmpty()) {
                throw new IOException("Missing redirection url");
            }

            oldUrl = http.getURL();
            newUrl = new URL(oldUrl, URLDecoder.decode(location, StandardCharsets.UTF_8.name()));
        } finally {
            http.disconnect();
        }

        if (isDowngradingProtocolOnRedirect(oldUrl, newUrl)) {
            throw new IOException("Downgrading protocol on redirect from '" + oldUrl + "' to '" + newUrl + "'");
        }

        listener.onRedirection(http.getURL(), newUrl);
        return open(newUrl, mediaType, langs, redirects + 1);
    }

    private Response getBody(HttpURLConnection connection) throws IOException {
        return new Jdk8Response(connection, decoders);
    }

    private IOException getError(HttpURLConnection http) throws IOException {
        try {
            return ResponseError.of(http);
        } finally {
            http.disconnect();
        }
    }

    public interface EventListener {

        void onOpen(URL query, String mediaType, String langs, Proxy proxy);

        void onRedirection(URL oldUrl, URL newUrl);

        static EventListener noOp() {
            return NO_OP_EVENT_LISTENER;
        }
    }

    public interface StreamDecoder {

        InputStream decode(InputStream stream) throws IOException;
    }

    @lombok.Getter
    public static final class ResponseError extends IOException {

        private static ResponseError of(HttpURLConnection http) throws IOException {
            return new ResponseError(http.getResponseCode(), http.getResponseMessage(), http.getHeaderFields());
        }

        private final int responseCode;
        private final String responseMessage;
        private final Map<String, List<String>> headerFields;

        public ResponseError(int responseCode, String responseMessage, Map<String, List<String>> headerFields) {
            super(responseCode + ": " + responseMessage);
            this.responseCode = responseCode;
            this.responseMessage = responseMessage;
            this.headerFields = headerFields;
        }
    }

    private enum ResponseType {
        INFORMATIONAL, SUCCESSFUL, REDIRECTION, CLIENT_ERROR, SERVER_ERROR, UNKNOWN;

        static ResponseType parse(int code) {
            switch (code / 100) {
                case 1:
                    return INFORMATIONAL;
                case 2:
                    return SUCCESSFUL;
                case 3:
                    return REDIRECTION;
                case 4:
                    return CLIENT_ERROR;
                case 5:
                    return SERVER_ERROR;
                default:
                    return UNKNOWN;
            }
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class Jdk8Response implements RestClient.Response {

        @lombok.NonNull
        private final HttpURLConnection conn;

        @lombok.NonNull
        private final Map<String, StreamDecoder> decoders;

        @Override
        public @NonNull String getContentType() {
            return conn.getContentType();
        }

        @Override
        public @NonNull InputStream getBody() throws IOException {
            String encoding = conn.getContentEncoding();
            return decoders.getOrDefault(encoding, o -> o).decode(conn.getInputStream());
        }

        @Override
        public void close() throws IOException {
            conn.disconnect();
        }
    }

    /**
     * https://en.wikipedia.org/wiki/Downgrade_attack
     *
     * @param oldUrl
     * @param newUrl
     * @return
     */
    static boolean isDowngradingProtocolOnRedirect(URL oldUrl, URL newUrl) {
        return "https".equalsIgnoreCase(oldUrl.getProtocol())
                && !"https".equalsIgnoreCase(newUrl.getProtocol());
    }

    static final String ACCEPT_HEADER = "Accept";
    static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    static final String LOCATION_HEADER = "Location";

    static final int DEFAULT_MAX_REDIRECTS = 20;
    static final int NO_TIMEOUT = 0;

    private static final EventListener NO_OP_EVENT_LISTENER = new EventListener() {
        @Override
        public void onOpen(URL query, String mediaType, String langs, Proxy proxy) {
        }

        @Override
        public void onRedirection(URL oldUrl, URL newUrl) {
        }
    };

    private static URI toURI(URL url) throws IOException {
        try {
            return url.toURI();
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }
}
