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
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    @lombok.NonNull
    private final Jdk8RestClient.Authenticator authenticator;

    private final boolean preemptiveAuthentication;

    public static Builder builder() {
        return new Builder()
                .readTimeout(NO_TIMEOUT)
                .connectTimeout(NO_TIMEOUT)
                .maxRedirects(DEFAULT_MAX_REDIRECTS)
                .proxySelector(ProxySelector.getDefault())
                .sslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory())
                .hostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier())
                .listener(EventListener.NONE)
                .decoder("gzip", StreamDecoder.GZIP)
                .decoder("deflate", StreamDecoder.DEFLATE)
                .authenticator(Authenticator.NONE)
                .preemptiveAuthentication(false);
    }

    @Override
    public Response open(URL query, String mediaType, String langs) throws IOException {
        Objects.requireNonNull(query);
        Objects.requireNonNull(mediaType);
        Objects.requireNonNull(langs);
        return open(query, mediaType, langs, 0, getPreemptiveAuthScheme());
    }

    public interface EventListener {

        void onOpen(@NonNull URL url, @NonNull String mediaType, @NonNull String langs, @NonNull Proxy proxy, @NonNull AuthScheme scheme);

        void onRedirection(@NonNull URL oldUrl, @NonNull URL newUrl);

        void onUnauthorized(@NonNull URL url, @NonNull AuthScheme oldScheme, @NonNull AuthScheme newScheme);

        EventListener NONE = new EventListener() {
            @Override
            public void onOpen(URL url, String mediaType, String langs, Proxy proxy, AuthScheme scheme) {
            }

            @Override
            public void onRedirection(URL oldUrl, URL newUrl) {
            }

            @Override
            public void onUnauthorized(URL url, AuthScheme oldScheme, AuthScheme newScheme) {
            }
        };
    }

    public interface StreamDecoder {

        @NonNull
        InputStream decode(@NonNull InputStream stream) throws IOException;

        StreamDecoder GZIP = GZIPInputStream::new;
        StreamDecoder DEFLATE = InflaterInputStream::new;
    }

    public interface Authenticator {

        @Nullable
        PasswordAuthentication getPasswordAuthentication(@NonNull URL url);

        Authenticator NONE = url -> null;
    }

    private Response open(URL query, String mediaType, String langs, int redirects, AuthScheme requestScheme) throws IOException {
        if (!requestScheme.isSecureRequest(query)) {
            throw new IOException("Insecure protocol for " + requestScheme + " auth on '" + query + "'");
        }

        Proxy proxy = getProxy(query);

        listener.onOpen(query, mediaType, langs, proxy, requestScheme);

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
        requestScheme.configureRequest(query, authenticator, http);

        switch (ResponseType.parse(http.getResponseCode())) {
            case REDIRECTION:
                return redirect(http, mediaType, langs, redirects);
            case SUCCESSFUL:
                return getBody(http);
            case CLIENT_ERROR:
                return recoverClientError(http, mediaType, langs, redirects, requestScheme);
            default:
                throw getError(http);
        }
    }

    private String getEncodingHeader() {
        return decoders.keySet().stream().collect(Collectors.joining(","));
    }

    private AuthScheme getPreemptiveAuthScheme() {
        return preemptiveAuthentication ? AuthScheme.BASIC : AuthScheme.NONE;
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
        return open(newUrl, mediaType, langs, redirects + 1, getPreemptiveAuthScheme());
    }

    private Response recoverClientError(HttpURLConnection http, String mediaType, String langs, int redirects, AuthScheme requestScheme) throws IOException {
        switch (http.getResponseCode()) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                AuthScheme responseScheme = AuthScheme.parse(http).orElse(AuthScheme.BASIC);
                if (!requestScheme.equals(responseScheme)) {
                    listener.onUnauthorized(http.getURL(), requestScheme, responseScheme);
                    return open(http.getURL(), mediaType, langs, redirects + 1, responseScheme);
                }
        }

        throw getError(http);
    }

    private Response getBody(HttpURLConnection connection) throws IOException {
        return new Jdk8Response(connection, decoders);
    }

    private IOException getError(HttpURLConnection http) throws IOException {
        try {
            return new ResponseError(http.getResponseCode(), http.getResponseMessage(), http.getHeaderFields());
        } finally {
            http.disconnect();
        }
    }

    public enum AuthScheme {
        BASIC {
            @Override
            boolean isSecureRequest(URL url) {
                return "https".equalsIgnoreCase(url.getProtocol());
            }

            @Override
            void configureRequest(URL url, Authenticator authenticator, HttpURLConnection http) {
                PasswordAuthentication auth = authenticator.getPasswordAuthentication(url);
                if (auth != null) {
                    http.addRequestProperty(AUTHORIZATION_HEADER, getBasicAuthHeader(auth));
                }
            }

            @Override
            boolean hasResponseHeader(HttpURLConnection http) {
                String header = http.getHeaderField(AUTHENTICATE_HEADER);
                return header != null && header.startsWith("Basic");
            }
        },
        NONE {
            @Override
            boolean isSecureRequest(URL query) {
                return true;
            }

            @Override
            void configureRequest(URL query, Authenticator authenticator, HttpURLConnection http) {
            }

            @Override
            boolean hasResponseHeader(HttpURLConnection http) {
                return false;
            }
        };

        abstract boolean isSecureRequest(URL query);

        abstract void configureRequest(URL query, Authenticator authenticator, HttpURLConnection http);

        abstract boolean hasResponseHeader(HttpURLConnection http);

        static Optional<AuthScheme> parse(HttpURLConnection http) {
            return Stream.of(AuthScheme.values())
                    .filter(authScheme -> authScheme.hasResponseHeader(http))
                    .findFirst();
        }

        private static String getBasicAuthHeader(PasswordAuthentication auth) {
            byte[] data = new StringBuilder()
                    .append(auth.getUserName())
                    .append(':')
                    .append(auth.getPassword())
                    .toString()
                    .getBytes(StandardCharsets.UTF_8);
            return "Basic " + Base64.getEncoder().encodeToString(data);
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
    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHENTICATE_HEADER = "WWW-Authenticate";

    static final int DEFAULT_MAX_REDIRECTS = 20;
    static final int NO_TIMEOUT = 0;

    private static URI toURI(URL url) throws IOException {
        try {
            return url.toURI();
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }
}
