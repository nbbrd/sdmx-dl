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
import nbbrd.io.Resource;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
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

    public static @NonNull Client newClient(@NonNull Context context) {
        Client result = new DefaultClient(context, getBackend());
        File dumpFile = getDumpFile();
        return dumpFile != null ? new DumpingClient(dumpFile.toPath(), result, file -> context.getListener().onEvent("Dumping '" + file + "'")) : result;
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

        @lombok.Builder.Default
        String userAgent = null;

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

        void onSuccess(@NonNull MediaType mediaType);

        void onRedirection(@NonNull URL oldUrl, @NonNull URL newUrl);

        void onUnauthorized(@NonNull URL url, @NonNull AuthScheme oldScheme, @NonNull AuthScheme newScheme);

        void onEvent(@NonNull String message);

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
            public void onSuccess(@NonNull MediaType mediaType) {
                Objects.requireNonNull(mediaType);
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

            @Override
            public void onEvent(@NonNull String message) {
                Objects.requireNonNull(message);
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

    // TODO: document these options?
    private static final String SDMXDL_RI_WEB_BACKEND = "sdmxdl.ri.web.backend";
    private static final String SDMXDL_RI_WEB_DUMP_FOLDER = "sdmxdl.ri.web.dump.folder";

    private static final IwrConnectionFactory IWR_CONNECTION_FACTORY = new IwrConnectionFactory();
    private static final CurlConnectionFactory CURL_CONNECTION_FACTORY = new CurlConnectionFactory(false);
    private static final Jdk8ConnectionFactory JDK_8_CONNECTION_FACTORY = new Jdk8ConnectionFactory();

    private static DefaultClient.ConnectionFactory getBackend() {
        switch (System.getProperty(SDMXDL_RI_WEB_BACKEND, "")) {
            case "iwr":
                return IWR_CONNECTION_FACTORY;
            case "curl":
                return CURL_CONNECTION_FACTORY;
            default:
                return JDK_8_CONNECTION_FACTORY;
        }
    }

    private File getDumpFile() {
        String result = System.getProperty(SDMXDL_RI_WEB_DUMP_FOLDER);
        return result != null ? new File(result) : null;
    }

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static final class DumpingClient implements Client {

        @lombok.NonNull
        private final Path folder;

        @lombok.NonNull
        private final Client delegate;

        @lombok.NonNull
        private final Consumer<? super Path> onDump;

        @Override
        public @NonNull Response requestGET(@NonNull URL query, @NonNull List<MediaType> mediaTypes, @NonNull String langs) throws IOException {
            return new DumpingResponse(folder, delegate.requestGET(query, mediaTypes, langs), onDump);
        }
    }

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static final class DumpingResponse implements Response {

        @lombok.NonNull
        private final Path folder;

        @lombok.NonNull
        private final Response delegate;

        @lombok.NonNull
        private final Consumer<? super Path> onDump;

        @Override
        public @NonNull MediaType getContentType() throws IOException {
            return delegate.getContentType();
        }

        @Override
        public @NonNull InputStream getBody() throws IOException {
            InputStream inputStream = delegate.getBody();
            try {
                OutputStream outputStream = getDumpStream();
                return new TeeInputStream(inputStream, outputStream);
            } catch (IOException ex) {
                Resource.ensureClosed(ex, inputStream);
                throw ex;
            }
        }

        private OutputStream getDumpStream() throws IOException {
            Files.createDirectories(folder);
            Path dump = Files.createTempFile(folder, "body", ".tmp");
            onDump.accept(dump);
            return Files.newOutputStream(dump);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
