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

import internal.util.http.HttpHeadersBuilder;
import internal.util.http.HttpURLConnectionFactory;
import nbbrd.design.VisibleForTesting;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static internal.util.http.HttpConstants.*;
import static internal.util.rest.HttpRest.*;
import static java.util.Collections.emptyMap;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
final class DefaultClient implements Client {

    @lombok.NonNull
    private final HttpRest.Context context;

    @lombok.NonNull
    private final HttpURLConnectionFactory factory;

    @Override
    public Response requestGET(URL query, List<MediaType> mediaTypes, String langs) throws IOException {
        Objects.requireNonNull(query);
        Objects.requireNonNull(mediaTypes);
        Objects.requireNonNull(langs);
        return open(query, mediaTypes, langs, 0, getPreemptiveAuthScheme());
    }

    private Response open(URL query, List<MediaType> mediaTypes, String langs, int redirects, AuthSchemeHelper requestScheme) throws IOException {
        if (!isHttpProtocol(query) && !isHttpsProtocol(query)) {
            throw new IOException("Unsupported protocol '" + query.getProtocol() + "'");
        }

        if (!requestScheme.isSecureRequest(query)) {
            throw new IOException("Insecure protocol for " + requestScheme + " auth on '" + query + "'");
        }

        Proxy proxy = getProxy(query);

        context.getListener().onOpen(query, mediaTypes, langs, proxy, requestScheme.authScheme);

        HttpURLConnection connection = openConnection(query, mediaTypes, langs, requestScheme, proxy);

        switch (ResponseType.parse(connection.getResponseCode())) {
            case REDIRECTION:
                return redirect(connection, mediaTypes, langs, redirects);
            case SUCCESSFUL:
                return getBody(connection);
            case CLIENT_ERROR:
                return recoverClientError(connection, mediaTypes, langs, redirects, requestScheme);
            default:
                throw getError(connection);
        }
    }

    private HttpURLConnection openConnection(URL query, List<MediaType> mediaTypes, String langs, AuthSchemeHelper requestScheme, Proxy proxy) throws IOException {
        HttpURLConnection result = factory.openConnection(query, proxy);
        result.setReadTimeout(context.getReadTimeout());
        result.setConnectTimeout(context.getConnectTimeout());

        if (result instanceof HttpsURLConnection) {
            ((HttpsURLConnection) result).setSSLSocketFactory(context.getSslSocketFactory());
            ((HttpsURLConnection) result).setHostnameVerifier(context.getHostnameVerifier());
        }

        Map<String, List<String>> headers = new HttpHeadersBuilder()
                .put(HTTP_ACCEPT_HEADER, toAcceptHeader(mediaTypes))
                .put(HTTP_ACCEPT_LANGUAGE_HEADER, langs)
                .put(HTTP_ACCEPT_ENCODING_HEADER, getEncodingHeader())
                .put(HTTP_USER_AGENT_HEADER, context.getUserAgent())
                .put(requestScheme.getRequestHeaders(query, context.getAuthenticator()))
                .build();

        result.setRequestMethod("GET");
        result.setInstanceFollowRedirects(false);
        HttpHeadersBuilder.keyValues(headers)
                .forEach(header -> result.setRequestProperty(header.getKey(), header.getValue()));

        result.connect();

        return result;
    }

    private String getEncodingHeader() {
        return context.getDecoders()
                .stream()
                .map(HttpRest.StreamDecoder::getName)
                .collect(Collectors.joining(", "));
    }

    private AuthSchemeHelper getPreemptiveAuthScheme() {
        return context.isPreemptiveAuthentication() ? AuthSchemeHelper.BASIC : AuthSchemeHelper.NONE;
    }

    private Proxy getProxy(URL url) throws IOException {
        List<Proxy> proxies = context.getProxySelector().select(toURI(url));
        return proxies.isEmpty() ? Proxy.NO_PROXY : proxies.get(0);
    }

    private Response redirect(HttpURLConnection connection, List<MediaType> mediaTypes, String langs, int redirects) throws IOException {
        URL oldUrl;
        URL newUrl;
        try {
            if (redirects == context.getMaxRedirects()) {
                throw new IOException("Max redirection reached");
            }

            String location = connection.getHeaderField(HTTP_LOCATION_HEADER);
            if (location == null || location.isEmpty()) {
                throw new IOException("Missing redirection url");
            }

            oldUrl = connection.getURL();
            newUrl = new URL(oldUrl, URLDecoder.decode(location, StandardCharsets.UTF_8.name()));
        } finally {
            doClose(connection);
        }

        if (isDowngradingProtocolOnRedirect(oldUrl, newUrl)) {
            throw new IOException("Downgrading protocol on redirect from '" + oldUrl + "' to '" + newUrl + "'");
        }

        context.getListener().onRedirection(connection.getURL(), newUrl);
        return open(newUrl, mediaTypes, langs, redirects + 1, getPreemptiveAuthScheme());
    }

    private Response recoverClientError(HttpURLConnection connection, List<MediaType> mediaTypes, String langs, int redirects, AuthSchemeHelper requestScheme) throws IOException {
        if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            AuthSchemeHelper responseScheme = AuthSchemeHelper.parse(connection).orElse(AuthSchemeHelper.BASIC);
            if (!requestScheme.equals(responseScheme)) {
                context.getListener().onUnauthorized(connection.getURL(), requestScheme.authScheme, responseScheme.authScheme);
                return open(connection.getURL(), mediaTypes, langs, redirects + 1, responseScheme);
            }
            context.getAuthenticator().invalidate(connection.getURL());
        }

        throw getError(connection);
    }

    private Response getBody(HttpURLConnection connection) throws IOException {
        DefaultResponse result = new DefaultResponse(connection, context.getDecoders());
        context.getListener().onSuccess(result.getContentType());
        return result;
    }

    private IOException getError(HttpURLConnection connection) throws IOException {
        try {
            return new ResponseError(connection.getResponseCode(), connection.getResponseMessage(), connection.getHeaderFields());
        } finally {
            doClose(connection);
        }
    }

    private static void doClose(HttpURLConnection connection) throws IOException {
        try {
            connection.disconnect();
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    @lombok.AllArgsConstructor
    private enum AuthSchemeHelper {
        BASIC(AuthScheme.BASIC) {
            @Override
            boolean isSecureRequest(URL url) {
                return isHttpsProtocol(url);
            }

            @Override
            Map<String, List<String>> getRequestHeaders(URL url, HttpRest.Authenticator authenticator) {
                PasswordAuthentication auth = authenticator.getPasswordAuthentication(url);
                return auth != null ? new HttpHeadersBuilder().put(HTTP_AUTHORIZATION_HEADER, getBasicAuthHeader(auth)).build() : emptyMap();
            }

            @Override
            boolean hasResponseHeader(HttpURLConnection http) throws IOException {
                String value = http.getHeaderField(HTTP_AUTHENTICATE_HEADER);
                return value != null && value.startsWith("Basic");
            }
        },
        NONE(AuthScheme.NONE) {
            @Override
            boolean isSecureRequest(URL query) {
                return true;
            }

            @Override
            Map<String, List<String>> getRequestHeaders(URL query, HttpRest.Authenticator authenticator) {
                return emptyMap();
            }

            @Override
            boolean hasResponseHeader(HttpURLConnection http) {
                return false;
            }
        };

        private final AuthScheme authScheme;

        abstract boolean isSecureRequest(URL query);

        abstract Map<String, List<String>> getRequestHeaders(URL query, HttpRest.Authenticator authenticator);

        abstract boolean hasResponseHeader(HttpURLConnection http) throws IOException;

        static Optional<AuthSchemeHelper> parse(HttpURLConnection http) {
            return Stream.of(AuthSchemeHelper.values())
                    .filter(authScheme -> {
                        try {
                            return authScheme.hasResponseHeader(http);
                        } catch (IOException exception) {
                            throw new UncheckedIOException(exception);
                        }
                    })
                    .findFirst();
        }

        private static String getBasicAuthHeader(PasswordAuthentication auth) {
            String basicAuth = auth.getUserName() + ':' + String.valueOf(auth.getPassword());
            return "Basic " + toBase64(basicAuth);
        }

        private static String toBase64(String input) {
            return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class DefaultResponse implements Response {

        @lombok.NonNull
        private final HttpURLConnection conn;

        @lombok.NonNull
        private final List<StreamDecoder> decoders;

        @Override
        public @NonNull MediaType getContentType() throws IOException {
            String contentType = conn.getHeaderField(HTTP_CONTENT_TYPE_HEADER);
            if (contentType == null) {
                throw new IOException("Content type not known");
            }
            try {
                return MediaType.parse(contentType);
            } catch (IllegalArgumentException ex) {
                throw new IOException("Invalid content type '" + contentType + "'", ex);
            }
        }

        @Override
        public @NonNull InputStream getBody() throws IOException {
            String encodingOrNull = conn.getHeaderField(HTTP_CONTENT_ENCODING_HEADER);
            return decoders
                    .stream()
                    .filter(decoder -> decoder.getName().equals(encodingOrNull))
                    .findFirst()
                    .orElse(StreamDecoder.noOp())
                    .decode(conn.getInputStream());
        }

        @Override
        public void close() throws IOException {
            doClose(conn);
        }
    }

    @VisibleForTesting
    static @NonNull String toAcceptHeader(@NonNull List<MediaType> mediaTypes) {
        return mediaTypes.stream().map(MediaType::toString).collect(Collectors.joining(", "));
    }

    private static URI toURI(URL url) throws IOException {
        try {
            return url.toURI();
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }
}
