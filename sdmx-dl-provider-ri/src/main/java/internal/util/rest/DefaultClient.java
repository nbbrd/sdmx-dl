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

import nbbrd.design.VisibleForTesting;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static internal.util.rest.HttpRest.*;
import static java.util.Collections.emptyMap;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
final class DefaultClient implements Client {

    public interface ConnectionFactory {

        Connection open(
                URL query,
                Proxy proxy,
                int readTimeout,
                int connectTimeout,
                SSLSocketFactory sslSocketFactory,
                HostnameVerifier hostnameVerifier,
                Map<String, List<String>> headers
        ) throws IOException;
    }

    public interface Connection extends Closeable {

        int NOT_VALID_CODE = -1;

        @NonNull URL getQuery();

        int getStatusCode() throws IOException;

        @Nullable String getStatusMessage() throws IOException;

        @NonNull Optional<String> getHeaderFirstValue(@NonNull String name) throws IOException;

        @NonNull Map<String, List<String>> getHeaders() throws IOException;

        @NonNull InputStream getInputStream() throws IOException;
    }

    @lombok.NonNull
    private final HttpRest.Context context;

    @lombok.NonNull
    private final ConnectionFactory factory;

    @Override
    public Response requestGET(URL query, List<MediaType> mediaTypes, String langs) throws IOException {
        Objects.requireNonNull(query);
        Objects.requireNonNull(mediaTypes);
        Objects.requireNonNull(langs);
        return open(query, mediaTypes, langs, 0, getPreemptiveAuthScheme());
    }

    private Response open(URL query, List<MediaType> mediaTypes, String langs, int redirects, AuthSchemeHelper requestScheme) throws IOException {
        if (!"http".equals(query.getProtocol()) && !"https".equals(query.getProtocol())) {
            throw new IOException("Unsupported protocol '" + query.getProtocol() + "'");
        }

        if (!requestScheme.isSecureRequest(query)) {
            throw new IOException("Insecure protocol for " + requestScheme + " auth on '" + query + "'");
        }

        Proxy proxy = getProxy(query);

        context.getListener().onOpen(query, mediaTypes, langs, proxy, requestScheme.authScheme);

        Connection connection = factory.open(
                query,
                proxy,
                context.getReadTimeout(),
                context.getConnectTimeout(),
                context.getSslSocketFactory(),
                context.getHostnameVerifier(),
                new HttpHeadersBuilder()
                        .put(ACCEPT_HEADER, toAcceptHeader(mediaTypes))
                        .put(ACCEPT_LANGUAGE_HEADER, langs)
                        .put(ACCEPT_ENCODING_HEADER, getEncodingHeader())
                        .put(USER_AGENT_HEADER, context.getUserAgent())
                        .put(requestScheme.getRequestHeaders(query, context.getAuthenticator()))
                        .build()
        );

        switch (ResponseType.parse(connection.getStatusCode())) {
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

    private Response redirect(Connection connection, List<MediaType> mediaTypes, String langs, int redirects) throws IOException {
        URL oldUrl;
        URL newUrl;
        try {
            if (redirects == context.getMaxRedirects()) {
                throw new IOException("Max redirection reached");
            }

            Optional<String> location = connection.getHeaderFirstValue(LOCATION_HEADER).filter(o -> !o.isEmpty());
            if (!location.isPresent()) {
                throw new IOException("Missing redirection url");
            }

            oldUrl = connection.getQuery();
            newUrl = new URL(oldUrl, URLDecoder.decode(location.get(), StandardCharsets.UTF_8.name()));
        } finally {
            connection.close();
        }

        if (isDowngradingProtocolOnRedirect(oldUrl, newUrl)) {
            throw new IOException("Downgrading protocol on redirect from '" + oldUrl + "' to '" + newUrl + "'");
        }

        context.getListener().onRedirection(connection.getQuery(), newUrl);
        return open(newUrl, mediaTypes, langs, redirects + 1, getPreemptiveAuthScheme());
    }

    private Response recoverClientError(Connection connection, List<MediaType> mediaTypes, String langs, int redirects, AuthSchemeHelper requestScheme) throws IOException {
        if (connection.getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            AuthSchemeHelper responseScheme = AuthSchemeHelper.parse(connection).orElse(AuthSchemeHelper.BASIC);
            if (!requestScheme.equals(responseScheme)) {
                context.getListener().onUnauthorized(connection.getQuery(), requestScheme.authScheme, responseScheme.authScheme);
                return open(connection.getQuery(), mediaTypes, langs, redirects + 1, responseScheme);
            }
            context.getAuthenticator().invalidate(connection.getQuery());
        }

        throw getError(connection);
    }

    private Response getBody(Connection connection) throws IOException {
        DefaultResponse result = new DefaultResponse(connection, context.getDecoders());
        context.getListener().onSuccess(result.getContentType());
        return result;
    }

    private IOException getError(Connection connection) throws IOException {
        try {
            return new ResponseError(connection.getStatusCode(), connection.getStatusMessage(), connection.getHeaders());
        } finally {
            connection.close();
        }
    }

    @lombok.AllArgsConstructor
    private enum AuthSchemeHelper {
        BASIC(AuthScheme.BASIC) {
            @Override
            boolean isSecureRequest(URL url) {
                return "https".equalsIgnoreCase(url.getProtocol());
            }

            @Override
            Map<String, List<String>> getRequestHeaders(URL url, HttpRest.Authenticator authenticator) {
                PasswordAuthentication auth = authenticator.getPasswordAuthentication(url);
                return auth != null ? new HttpHeadersBuilder().put(AUTHORIZATION_HEADER, getBasicAuthHeader(auth)).build() : emptyMap();
            }

            @Override
            boolean hasResponseHeader(Connection http) throws IOException {
                return http.getHeaderFirstValue(AUTHENTICATE_HEADER)
                        .map(value -> value.startsWith("Basic"))
                        .isPresent();
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
            boolean hasResponseHeader(Connection http) {
                return false;
            }
        };

        private final AuthScheme authScheme;

        abstract boolean isSecureRequest(URL query);

        abstract Map<String, List<String>> getRequestHeaders(URL query, HttpRest.Authenticator authenticator);

        abstract boolean hasResponseHeader(Connection http) throws IOException;

        static Optional<AuthSchemeHelper> parse(Connection http) {
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
    private static final class DefaultResponse implements Response {

        @lombok.NonNull
        private final DefaultClient.Connection conn;

        @lombok.NonNull
        private final List<StreamDecoder> decoders;

        @Override
        public @NonNull MediaType getContentType() throws IOException {
            Optional<String> contentType = conn.getHeaderFirstValue(CONTENT_TYPE_HEADER);
            if (!contentType.isPresent()) {
                throw new IOException("Content type not known");
            }
            try {
                return MediaType.parse(contentType.get());
            } catch (IllegalArgumentException ex) {
                throw new IOException("Invalid content type '" + contentType.get() + "'", ex);
            }
        }

        @Override
        public @NonNull InputStream getBody() throws IOException {
            String encodingOrNull = conn.getHeaderFirstValue(CONTENT_ENCODING_HEADER).orElse(null);
            return decoders
                    .stream()
                    .filter(decoder -> decoder.getName().equals(encodingOrNull))
                    .findFirst()
                    .orElse(StreamDecoder.noOp())
                    .decode(conn.getInputStream());
        }

        @Override
        public void close() throws IOException {
            conn.close();
        }
    }

    @VisibleForTesting
    static @NonNull String toAcceptHeader(@NonNull List<MediaType> mediaTypes) {
        return mediaTypes.stream().map(MediaType::toString).collect(Collectors.joining(", "));
    }

    @VisibleForTesting
    static final String ACCEPT_HEADER = "Accept";

    @VisibleForTesting
    static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";

    @VisibleForTesting
    static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";

    @VisibleForTesting
    static final String LOCATION_HEADER = "Location";

    @VisibleForTesting
    static final String AUTHORIZATION_HEADER = "Authorization";

    @VisibleForTesting
    static final String AUTHENTICATE_HEADER = "WWW-Authenticate";

    @VisibleForTesting
    static final String USER_AGENT_HEADER = "User-Agent";

    @VisibleForTesting
    static final String CONTENT_TYPE_HEADER = "Content-Type";

    @VisibleForTesting
    static final String CONTENT_ENCODING_HEADER = "Content-Encoding";

    /**
     * https://en.wikipedia.org/wiki/Downgrade_attack
     *
     * @param oldUrl
     * @param newUrl
     * @return
     */
    @VisibleForTesting
    static boolean isDowngradingProtocolOnRedirect(URL oldUrl, URL newUrl) {
        return "https".equalsIgnoreCase(oldUrl.getProtocol())
                && !"https".equalsIgnoreCase(newUrl.getProtocol());
    }

    private static URI toURI(URL url) throws IOException {
        try {
            return url.toURI();
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }
}
