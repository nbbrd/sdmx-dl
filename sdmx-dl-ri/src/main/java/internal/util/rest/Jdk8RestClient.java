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

import org.checkerframework.checker.nullness.qual.NonNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class Jdk8RestClient implements HttpRest.Client {

    @lombok.NonNull
    private final HttpRest.Context context;

    @Override
    public HttpRest.Response requestGET(URL query, String mediaType, String langs) throws IOException {
        Objects.requireNonNull(query);
        Objects.requireNonNull(mediaType);
        Objects.requireNonNull(langs);
        return open(query, mediaType, langs, 0, getPreemptiveAuthScheme());
    }

    private HttpRest.Response open(URL query, String mediaType, String langs, int redirects, AuthSchemeHelper requestScheme) throws IOException {
        if (!requestScheme.isSecureRequest(query)) {
            throw new IOException("Insecure protocol for " + requestScheme + " auth on '" + query + "'");
        }

        Proxy proxy = getProxy(query);

        context.getListener().onOpen(query, mediaType, langs, proxy, requestScheme.authScheme);

        URLConnection conn = query.openConnection(proxy);
        conn.setReadTimeout(context.getReadTimeout());
        conn.setConnectTimeout(context.getConnectTimeout());

        if (!(conn instanceof HttpURLConnection)) {
            throw new IOException("Unsupported connection type");
        }

        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection) conn).setSSLSocketFactory(context.getSslSocketFactory());
            ((HttpsURLConnection) conn).setHostnameVerifier(context.getHostnameVerifier());
        }

        HttpURLConnection http = (HttpURLConnection) conn;
        http.setRequestMethod("GET");
        http.setRequestProperty(ACCEPT_HEADER, mediaType);
        http.setRequestProperty(ACCEPT_LANGUAGE_HEADER, langs);
        http.addRequestProperty(ACCEPT_ENCODING_HEADER, getEncodingHeader());
        http.setInstanceFollowRedirects(false);
        requestScheme.configureRequest(query, context.getAuthenticator(), http);

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
        return context
                .getDecoders()
                .stream()
                .map(HttpRest.StreamDecoder::getName)
                .collect(Collectors.joining(","));
    }

    private AuthSchemeHelper getPreemptiveAuthScheme() {
        return context.isPreemptiveAuthentication() ? AuthSchemeHelper.BASIC : AuthSchemeHelper.NONE;
    }

    private Proxy getProxy(URL url) throws IOException {
        List<Proxy> proxies = context.getProxySelector().select(toURI(url));
        return proxies.isEmpty() ? Proxy.NO_PROXY : proxies.get(0);
    }

    private HttpRest.Response redirect(HttpURLConnection http, String mediaType, String langs, int redirects) throws IOException {
        URL oldUrl;
        URL newUrl;
        try {
            if (redirects == context.getMaxRedirects()) {
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

        context.getListener().onRedirection(http.getURL(), newUrl);
        return open(newUrl, mediaType, langs, redirects + 1, getPreemptiveAuthScheme());
    }

    private HttpRest.Response recoverClientError(HttpURLConnection http, String mediaType, String langs, int redirects, AuthSchemeHelper requestScheme) throws IOException {
        switch (http.getResponseCode()) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                AuthSchemeHelper responseScheme = AuthSchemeHelper.parse(http).orElse(AuthSchemeHelper.BASIC);
                if (!requestScheme.equals(responseScheme)) {
                    context.getListener().onUnauthorized(http.getURL(), requestScheme.authScheme, responseScheme.authScheme);
                    return open(http.getURL(), mediaType, langs, redirects + 1, responseScheme);
                }
                context.getAuthenticator().invalidate(http.getURL());
        }

        throw getError(http);
    }

    private HttpRest.Response getBody(HttpURLConnection connection) throws IOException {
        return new Jdk8Response(connection, context.getDecoders());
    }

    private IOException getError(HttpURLConnection http) throws IOException {
        try {
            return new HttpRest.ResponseError(http.getResponseCode(), http.getResponseMessage(), http.getHeaderFields());
        } finally {
            http.disconnect();
        }
    }

    @lombok.AllArgsConstructor
    private enum AuthSchemeHelper {
        BASIC(HttpRest.AuthScheme.BASIC) {
            @Override
            boolean isSecureRequest(URL url) {
                return "https".equalsIgnoreCase(url.getProtocol());
            }

            @Override
            void configureRequest(URL url, HttpRest.Authenticator authenticator, HttpURLConnection http) {
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
        NONE(HttpRest.AuthScheme.NONE) {
            @Override
            boolean isSecureRequest(URL query) {
                return true;
            }

            @Override
            void configureRequest(URL query, HttpRest.Authenticator authenticator, HttpURLConnection http) {
            }

            @Override
            boolean hasResponseHeader(HttpURLConnection http) {
                return false;
            }
        };

        private final HttpRest.AuthScheme authScheme;

        abstract boolean isSecureRequest(URL query);

        abstract void configureRequest(URL query, HttpRest.Authenticator authenticator, HttpURLConnection http);

        abstract boolean hasResponseHeader(HttpURLConnection http);

        static Optional<AuthSchemeHelper> parse(HttpURLConnection http) {
            return Stream.of(AuthSchemeHelper.values())
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
    private static final class Jdk8Response implements HttpRest.Response {

        @lombok.NonNull
        private final HttpURLConnection conn;

        @lombok.NonNull
        private final List<HttpRest.StreamDecoder> decoders;

        @Override
        public @NonNull String getContentType() {
            return conn.getContentType();
        }

        @Override
        public @NonNull InputStream getBody() throws IOException {
            String encoding = conn.getContentEncoding();
            return decoders
                    .stream()
                    .filter(decoder -> decoder.getName().equals(encoding))
                    .findFirst()
                    .orElse(HttpRest.StreamDecoder.noOp())
                    .decode(conn.getInputStream());
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


    private static URI toURI(URL url) throws IOException {
        try {
            return url.toURI();
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }
}
