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
package internal.util.http;

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
import static java.util.Collections.emptyMap;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class DefaultHttpClient implements HttpClient {

    @lombok.NonNull
    private final HttpContext context;

    @lombok.NonNull
    private final HttpURLConnectionFactory factory;

    @Override
    public @NonNull HttpResponse requestGET(@NonNull HttpRequest request) throws IOException {
        Objects.requireNonNull(request);
        return open(request, 0, getPreemptiveAuthScheme());
    }

    private HttpResponse open(HttpRequest request, int redirects, AuthSchemeHelper requestScheme) throws IOException {
        if (!isHttpProtocol(request.getQuery()) && !isHttpsProtocol(request.getQuery())) {
            throw new IOException("Unsupported protocol '" + request.getQuery().getProtocol() + "'");
        }

        if (!requestScheme.isSecureRequest(request.getQuery())) {
            throw new IOException("Insecure protocol for " + requestScheme + " auth on '" + request.getQuery() + "'");
        }

        Proxy proxy = getProxy(request.getQuery());

        HttpURLConnection connection = openConnection(request, requestScheme, proxy);

        switch (ResponseType.parse(connection.getResponseCode())) {
            case REDIRECTION:
                return redirect(connection, request, redirects);
            case SUCCESSFUL:
                return getBody(connection);
            case CLIENT_ERROR:
                return recoverClientError(connection, request, redirects, requestScheme);
            default:
                throw getError(connection);
        }
    }

    private HttpURLConnection openConnection(HttpRequest request, AuthSchemeHelper requestScheme, Proxy proxy) throws IOException {
        HttpURLConnection result = factory.openConnection(request.getQuery(), proxy);
        result.setReadTimeout(context.getReadTimeout());
        result.setConnectTimeout(context.getConnectTimeout());

        if (result instanceof HttpsURLConnection) {
            ((HttpsURLConnection) result).setSSLSocketFactory(context.getSslSocketFactory().get());
            ((HttpsURLConnection) result).setHostnameVerifier(context.getHostnameVerifier().get());
        }

        Map<String, List<String>> headers = new HttpHeadersBuilder()
                .put(HTTP_ACCEPT_HEADER, toAcceptHeader(request.getMediaTypes()))
                .put(HTTP_ACCEPT_LANGUAGE_HEADER, request.getLangs())
                .put(HTTP_ACCEPT_ENCODING_HEADER, getEncodingHeader())
                .put(HTTP_USER_AGENT_HEADER, context.getUserAgent())
                .put(requestScheme.getRequestHeaders(request.getQuery(), context.getAuthenticator()))
                .build();

        result.setRequestMethod("GET");
        result.setInstanceFollowRedirects(false);
        HttpHeadersBuilder.keyValues(headers)
                .forEach(header -> result.setRequestProperty(header.getKey(), header.getValue()));

        context.getListener().onOpen(request, proxy, requestScheme.authScheme);

        result.connect();

        return result;
    }

    private String getEncodingHeader() {
        return context.getDecoders()
                .stream()
                .map(StreamDecoder::getName)
                .collect(Collectors.joining(", "));
    }

    private AuthSchemeHelper getPreemptiveAuthScheme() {
        return context.isPreemptiveAuthentication() ? AuthSchemeHelper.BASIC : AuthSchemeHelper.NONE;
    }

    private Proxy getProxy(URL url) throws IOException {
        List<Proxy> proxies = context.getProxySelector().get().select(toURI(url));
        return proxies.isEmpty() ? Proxy.NO_PROXY : proxies.get(0);
    }

    private HttpResponse redirect(HttpURLConnection connection, HttpRequest request, int redirects) throws IOException {
        final URL oldUrl = request.getQuery();
        URL newUrl;
        try {
            if (redirects == context.getMaxRedirects()) {
                throw new IOException("Max redirection reached");
            }

            String location = connection.getHeaderField(HTTP_LOCATION_HEADER);
            if (location == null || location.isEmpty()) {
                throw new IOException("Missing redirection url");
            }

            newUrl = new URL(oldUrl, URLDecoder.decode(location, StandardCharsets.UTF_8.name()));
        } finally {
            doClose(connection);
        }

        if (isDowngradingProtocolOnRedirect(oldUrl, newUrl)) {
            throw new IOException("Downgrading protocol on redirect from '" + oldUrl + "' to '" + newUrl + "'");
        }

        context.getListener().onRedirection(oldUrl, newUrl);
        return open(request.toBuilder().query(newUrl).build(), redirects + 1, getPreemptiveAuthScheme());
    }

    private HttpResponse recoverClientError(HttpURLConnection connection, HttpRequest request, int redirects, AuthSchemeHelper requestScheme) throws IOException {
        if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            AuthSchemeHelper responseScheme = AuthSchemeHelper.parse(connection).orElse(AuthSchemeHelper.BASIC);
            if (!requestScheme.equals(responseScheme)) {
                context.getListener().onUnauthorized(connection.getURL(), requestScheme.authScheme, responseScheme.authScheme);
                return open(request, redirects + 1, responseScheme);
            }
            context.getAuthenticator().invalidate(connection.getURL());
        }

        throw getError(connection);
    }

    private HttpResponse getBody(HttpURLConnection connection) throws IOException {
        DefaultResponse result = new DefaultResponse(connection, context.getDecoders());
        context.getListener().onSuccess(result.getContentType());
        return result;
    }

    private IOException getError(HttpURLConnection connection) throws IOException {
        try {
            return new HttpResponseException(connection.getResponseCode(), connection.getResponseMessage(), connection.getHeaderFields());
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
        BASIC(HttpAuthScheme.BASIC) {
            @Override
            boolean isSecureRequest(URL url) {
                return isHttpsProtocol(url);
            }

            @Override
            Map<String, List<String>> getRequestHeaders(URL url, HttpAuthenticator authenticator) {
                PasswordAuthentication auth = authenticator.getPasswordAuthentication(url);
                return auth != null ? new HttpHeadersBuilder().put(HTTP_AUTHORIZATION_HEADER, getBasicAuthHeader(auth)).build() : emptyMap();
            }

            @Override
            boolean hasResponseHeader(HttpURLConnection http) throws IOException {
                String value = http.getHeaderField(HTTP_AUTHENTICATE_HEADER);
                return value != null && value.startsWith("Basic");
            }
        },
        NONE(HttpAuthScheme.NONE) {
            @Override
            boolean isSecureRequest(URL query) {
                return true;
            }

            @Override
            Map<String, List<String>> getRequestHeaders(URL query, HttpAuthenticator authenticator) {
                return emptyMap();
            }

            @Override
            boolean hasResponseHeader(HttpURLConnection http) {
                return false;
            }
        };

        private final HttpAuthScheme authScheme;

        abstract boolean isSecureRequest(URL query);

        abstract Map<String, List<String>> getRequestHeaders(URL query, HttpAuthenticator authenticator);

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
    private static final class DefaultResponse implements HttpResponse {

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
