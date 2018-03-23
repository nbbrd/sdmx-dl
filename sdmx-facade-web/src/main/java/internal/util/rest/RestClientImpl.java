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
    private final int maxHop;

    @lombok.NonNull
    private final ProxySelector proxySelector;

    @lombok.NonNull
    private final SSLSocketFactory sslSocketFactory;

    @Override
    public InputStream openStream(URL query, String mediaType, String langs) throws IOException {
        return openStream(query, mediaType, langs, 0);
    }

    private InputStream openStream(URL query, String mediaType, String langs, int hop) throws IOException {
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
            case HttpURLConnection.HTTP_MULT_CHOICE:
            case HttpURLConnection.HTTP_SEE_OTHER:
                return redirect(http, mediaType, langs, hop);
            case HttpURLConnection.HTTP_OK:
                return getBody(http);
            default:
                throw getError(http);
        }
    }

    private Proxy getProxy(URL url) throws IOException {
        try {
            List<Proxy> proxies = proxySelector.select(url.toURI());
            return proxies.isEmpty() ? Proxy.NO_PROXY : proxies.get(0);
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    private InputStream redirect(HttpURLConnection http, String mediaType, String langs, int hop) throws IOException {
        try {
            if (hop == maxHop) {
                throw new IOException("Max hop reached");
            }
            String newQuery = http.getHeaderField(LOCATION_HEADER);
            if (newQuery == null || newQuery.isEmpty()) {
                throw new IOException("Missing redirection url");
            }
            return openStream(new URL(newQuery), mediaType, langs, hop + 1);
        } finally {
            http.disconnect();
        }
    }

    private InputStream getBody(HttpURLConnection http) throws IOException {
        String encoding = http.getContentEncoding();
        InputStream body = ContentEncoder.getDecoder(encoding).applyWithIO(http.getInputStream());
        return new DisconnectingInputStream(body, http::disconnect);
    }

    private IOException getError(HttpURLConnection http) throws IOException {
        try {
            System.out.println(http.getURL());
            return new IOException(http.getResponseCode() + ": " + http.getResponseMessage());
        } finally {
            http.disconnect();
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
}
