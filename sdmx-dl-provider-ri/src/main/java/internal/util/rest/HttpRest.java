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
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class HttpRest {

    public interface Client {

        @NonNull
        Response requestGET(@NonNull URL query, @NonNull List<MediaType> mediaTypes, @NonNull String langs) throws IOException;
    }

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    public static class Context {

        static final int DEFAULT_MAX_REDIRECTS = 20;
        static final int NO_TIMEOUT = 0;

        @NonNegative
        @lombok.Builder.Default
        int readTimeout = NO_TIMEOUT;

        @NonNegative
        @lombok.Builder.Default
        int connectTimeout = NO_TIMEOUT;

        @NonNegative
        @lombok.Builder.Default
        int maxRedirects = DEFAULT_MAX_REDIRECTS;

        @lombok.NonNull
        @lombok.Builder.Default
        ProxySelector proxySelector = ProxySelector.getDefault();

        @lombok.NonNull
        @lombok.Builder.Default
        SSLSocketFactory sslSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();

        @lombok.NonNull
        @lombok.Builder.Default
        HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

        @lombok.NonNull
        @lombok.Builder.Default
        HttpRest.EventListener listener = HttpRest.EventListener.noOp();

        @lombok.Singular
        List<HttpRest.StreamDecoder> decoders;

        @lombok.NonNull
        @lombok.Builder.Default
        HttpRest.Authenticator authenticator = HttpRest.Authenticator.noOp();

        @lombok.Builder.Default
        boolean preemptiveAuthentication = false;

        public static Builder builder() {
            return new Builder()
                    .decoder(HttpRest.StreamDecoder.gzip())
                    .decoder(HttpRest.StreamDecoder.deflate());
        }

        public static final class Builder {
        }
    }

    public interface Response extends Closeable {

        @NonNull
        MediaType getContentType() throws IOException;

        @NonNull
        InputStream getBody() throws IOException;
    }

    public interface Authenticator {

        @Nullable
        PasswordAuthentication getPasswordAuthentication(@NonNull URL url);

        void invalidate(@NonNull URL url);

        @NonNull
        static Authenticator noOp() {
            return Authenticators.NONE;
        }
    }

    public enum AuthScheme {
        BASIC, NONE
    }

    public interface EventListener {

        void onOpen(@NonNull URL url, @NonNull List<MediaType> mediaTypes, @NonNull String langs, @NonNull Proxy proxy, @NonNull AuthScheme scheme);

        void onRedirection(@NonNull URL oldUrl, @NonNull URL newUrl);

        void onUnauthorized(@NonNull URL url, @NonNull AuthScheme oldScheme, @NonNull AuthScheme newScheme);

        @NonNull
        static EventListener noOp() {
            return EventListeners.NONE;
        }
    }

    public interface StreamDecoder {

        @NonNull
        String getName();

        @NonNull
        InputStream decode(@NonNull InputStream stream) throws IOException;

        @NonNull
        static StreamDecoder noOp() {
            return StreamDecoders.NONE;
        }

        @NonNull
        static StreamDecoder gzip() {
            return StreamDecoders.GZIP;
        }

        @NonNull
        static StreamDecoder deflate() {
            return StreamDecoders.DEFLATE;
        }
    }

    @lombok.Getter
    public static final class ResponseError extends IOException {

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

    private enum Authenticators implements Authenticator {
        NONE {
            @Override
            public @Nullable PasswordAuthentication getPasswordAuthentication(@NonNull URL url) {
                Objects.requireNonNull(url);
                return null;
            }

            @Override
            public void invalidate(@NonNull URL url) {
                Objects.requireNonNull(url);
            }
        }
    }

    private enum EventListeners implements EventListener {
        NONE {
            @Override
            public void onOpen(URL url, List<MediaType> mediaTypes, String langs, Proxy proxy, AuthScheme scheme) {
                Objects.requireNonNull(url);
                Objects.requireNonNull(mediaTypes);
                Objects.requireNonNull(proxy);
                Objects.requireNonNull(scheme);
            }

            @Override
            public void onRedirection(URL oldUrl, URL newUrl) {
                Objects.requireNonNull(oldUrl);
                Objects.requireNonNull(newUrl);
            }

            @Override
            public void onUnauthorized(URL url, AuthScheme oldScheme, AuthScheme newScheme) {
                Objects.requireNonNull(url);
                Objects.requireNonNull(oldScheme);
                Objects.requireNonNull(newScheme);
            }
        }
    }

    private enum StreamDecoders implements StreamDecoder {
        NONE {
            @Override
            public @NonNull InputStream decode(@NonNull InputStream stream) {
                Objects.requireNonNull(stream);
                return stream;
            }
        },
        GZIP {
            @Override
            public @NonNull InputStream decode(@NonNull InputStream stream) throws IOException {
                return new GZIPInputStream(stream);
            }
        },
        DEFLATE {
            @Override
            public @NonNull InputStream decode(@NonNull InputStream stream) {
                return new InflaterInputStream(stream);
            }
        };

        @Override
        public @NonNull String getName() {
            return name().toLowerCase();
        }
    }
}
