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

import ioutil.IO;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
public final class RestClientImpl implements RestClient {

    private static final String ACCEPT_HEADER = "Accept";
    private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    private static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    private static final String LOCATION_HEADER = "Location";

    private final int readTimeout;
    private final int connectTimeout;
    private final int maxRedirects;

    @lombok.NonNull
    private final ProxySelector proxySelector;

    @lombok.NonNull
    private final SSLSocketFactory sslSocketFactory;

    @lombok.NonNull
    private final EventListener listener;

    @Override
    public InputStream openStream(URL query, String mediaType, String langs) throws IOException {
        listener.onOpenStream(query, mediaType, langs);
        return openStream(query, mediaType, langs, 0);
    }

    private InputStream openStream(URL query, String mediaType, String langs, int redirects) throws IOException {
        URLConnection conn = query.openConnection(getProxy(query));
        conn.setReadTimeout(readTimeout);
        conn.setConnectTimeout(connectTimeout);

        if (!(conn instanceof HttpURLConnection)) {
            throw new IOException("Unsupported connection type");
        }

        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
        }

        HttpURLConnection http = (HttpURLConnection) conn;
        http.setRequestMethod("GET");
        http.setRequestProperty(ACCEPT_HEADER, mediaType);
        http.setRequestProperty(ACCEPT_LANGUAGE_HEADER, langs);
        http.addRequestProperty(ACCEPT_ENCODING_HEADER, ContentEncoder.getEncodingHeader());

        switch (http.getResponseCode()) {
            case HttpURLConnection.HTTP_MOVED_PERM:
            case HttpURLConnection.HTTP_MOVED_TEMP:
            case HttpURLConnection.HTTP_SEE_OTHER:
            case 307: // Temporary Redirect
            case 308: // Permanent Redirect
                return redirect(http, mediaType, langs, redirects);
            case HttpURLConnection.HTTP_OK:
                return getBody(http);
            default:
                throw getError(http);
        }
    }

    private Proxy getProxy(URL url) throws IOException {
        try {
            List<Proxy> proxies = proxySelector.select(url.toURI());
            Proxy result = proxies.isEmpty() ? Proxy.NO_PROXY : proxies.get(0);
            listener.onProxy(url, result);
            return result;
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    private InputStream redirect(HttpURLConnection http, String mediaType, String langs, int redirects) throws IOException {
        URL oldUrl;
        URL newUrl;
        try {
            if (redirects == maxRedirects) {
                throw new IOException("Max redirections reached");
            }

            String newQuery = http.getHeaderField(LOCATION_HEADER);
            if (newQuery == null || newQuery.isEmpty()) {
                throw new IOException("Missing redirection url");
            }

            oldUrl = http.getURL();
            newUrl = new URL(newQuery);
        } finally {
            http.disconnect();
        }

        if (isDowngradingProtocolOnRedirect(oldUrl, newUrl)) {
            throw new IOException("Downgrading protocol on redirect from '" + oldUrl + "' to '" + newUrl + "'");
        }

        listener.onRedirection(http.getURL(), newUrl);
        return openStream(newUrl, mediaType, langs, redirects + 1);
    }

    private InputStream getBody(HttpURLConnection http) throws IOException {
        String encoding = http.getContentEncoding();
        InputStream body = ContentEncoder.getDecoder(encoding).applyWithIO(http.getInputStream());
        return new DisconnectingInputStream(body, http::disconnect);
    }

    private IOException getError(HttpURLConnection http) throws IOException {
        try {
            return ResponseError.of(http);
        } finally {
            http.disconnect();
        }
    }

    public interface EventListener {

        void onOpenStream(URL query, String mediaType, String langs);

        void onRedirection(URL oldUrl, URL newUrl);
        
        void onProxy(URL query, Proxy proxy);
    }

    @lombok.Getter
    public static final class ResponseError extends IOException {

        private static ResponseError of(HttpURLConnection http) throws IOException {
            return new ResponseError(http.getResponseCode(), http.getResponseMessage(), http.getHeaderFields());
        }

        private final int code;
        private final String message;
        private final Map<String, List<String>> headers;

        public ResponseError(int code, String message, Map<String, List<String>> headers) {
            super(code + ": " + message);
            this.code = code;
            this.message = message;
            this.headers = headers;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class DisconnectingInputStream extends InputStream {

        @lombok.experimental.Delegate(excludes = Closeable.class)
        private final InputStream delegate;

        private final Runnable onClose;

        @Override
        public void close() throws IOException {
            try {
                delegate.close();
            } finally {
                onClose.run();
            }
        }
    }

    @lombok.RequiredArgsConstructor
    @lombok.Getter
    private enum ContentEncoder {

        GZIP("gzip", GZIPInputStream::new),
        DEFLATE("deflate", InflaterInputStream::new);

        private final String name;
        private final IO.Function<InputStream, InputStream> decoder;

        static IO.Function<InputStream, InputStream> getDecoder(@Nullable String name) {
            return Stream.of(values())
                    .filter(o -> Objects.equals(name, o.name))
                    .map(o -> o.getDecoder())
                    .findAny()
                    .orElse(IO.Function.identity());
        }

        static String getEncodingHeader() {
            return Stream.of(ContentEncoder.values()).map(ContentEncoder::getName).collect(Collectors.joining(","));
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
}
