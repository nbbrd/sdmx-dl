/*
 * Copyright 2018 National Bank of Belgium
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
package sdmxdl.provider.ri.drivers;

import lombok.NonNull;
import nbbrd.io.http.*;
import nbbrd.io.http.ext.DumpingClient;
import nbbrd.io.net.MediaType;
import nbbrd.io.text.BaseProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.jspecify.annotations.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.Languages;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.provider.web.WebEvents;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Authenticator;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.WebCaching;
import sdmxdl.web.spi.WebContext;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static sdmxdl.provider.ri.drivers.AuthSchemes.BASIC_AUTH_SCHEME;
import static sdmxdl.provider.ri.drivers.AuthSchemes.MSAL_AUTH_SCHEME;
import static sdmxdl.provider.web.DriverProperties.*;
import static sdmxdl.web.spi.Driver.DRIVER_PROPERTY_PREFIX;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class RiHttpUtils {

    /**
     * Defines a folder where downloaded assets are copied. Default value is <code>null</code> and disables copying.
     */
    @PropertyDefinition
    public static final Property<File> DUMP_FOLDER_PROPERTY =
            Property.of(DRIVER_PROPERTY_PREFIX + ".dumpFolder", null, Parser.onFile(), Formatter.onFile());

    public static final List<String> RI_CONNECTION_PROPERTIES = BaseProperty.keysOf(
            CONNECT_TIMEOUT_PROPERTY,
            READ_TIMEOUT_PROPERTY,
            MAX_REDIRECTS_PROPERTY,
            AUTH_SCHEME_PROPERTY,
            USER_AGENT_PROPERTY,
            DUMP_FOLDER_PROPERTY
    );

    public static @NonNull HttpRequest newRequest(@NonNull URL query, @NonNull List<MediaType> mediaTypes, @NonNull Languages langs) {
        return HttpRequest
                .builder()
                .query(query)
                .mediaTypes(mediaTypes)
                .langs(langs.toString())
                .build();
    }

    public static @NonNull HttpClient newClient(@NonNull WebSource source, @NonNull WebContext context) {
        HttpContext httpContext = newContext(source, context);
        HttpClient result = newClient(source, httpContext);
        EventListener onEvent = context.getEventListener(source);
        return onEvent != null ? new ByteCountingClient(result, message -> onEvent.accept("RI_HTTP", message, 1)) : result;
    }

    public static @NonNull HttpClient newClient(@NonNull WebSource source, @NonNull HttpContext context) {
        HttpClient result = new DefaultHttpClient(context);
        File dumpFolder = DUMP_FOLDER_PROPERTY.get(source.getProperties());
        return dumpFolder != null ? newDumpingClient(context, result, dumpFolder) : result;
    }

    public static @NonNull HttpContext newContext(@NonNull WebSource source, @NonNull WebContext context) {
        Network network = context.getNetwork(source);
        EventListener onEvent = context.getEventListener(source);
        ErrorListener onError = context.getErrorListener(source);
        return HttpContext
                .builder()
                .readTimeout(READ_TIMEOUT_PROPERTY.get(source.getProperties()))
                .connectTimeout(CONNECT_TIMEOUT_PROPERTY.get(source.getProperties()))
                .maxRedirects(MAX_REDIRECTS_PROPERTY.get(source.getProperties()))
                .authScheme(toHttpAuthScheme(AUTH_SCHEME_PROPERTY.get(source.getProperties())))
                .proxySelector(network::getProxySelector)
                .sslSocketFactory(() -> network.getSSLFactory().getSSLSocketFactory())
                .hostnameVerifier(() -> network.getSSLFactory().getHostnameVerifier())
                .urlConnectionFactory(() -> network.getURLConnectionFactory()::openConnection)
                .listener(onEvent != null ? new RiHttpEventListener(message -> onEvent.accept("RI_HTTP", message, 1)) : HttpEventListener.noOp())
                .authenticator(new RiHttpAuthenticator(source, context.getAuthenticators(), context.getCaching(), onEvent, onError))
                .userAgent(USER_AGENT_PROPERTY.get(source.getProperties()))
                .build();
    }

    private static HttpAuthScheme toHttpAuthScheme(@Nullable String name) {
        if (name != null) {
            switch (name) {
                case BASIC_AUTH_SCHEME:
                    return HttpAuthScheme.BASIC;
                case MSAL_AUTH_SCHEME:
                    return HttpAuthScheme.BEARER;
            }
        }
        return HttpAuthScheme.NONE;
    }

    private static DumpingClient newDumpingClient(HttpContext context, HttpClient client, File dumpFolder) {
        return new DumpingClient(dumpFolder.toPath(), client, file -> context.getListener().onEvent("Dumping " + file.toUri()));
    }

    @lombok.AllArgsConstructor
    private static final class RiHttpEventListener implements HttpEventListener {

        private final @NonNull Consumer<CharSequence> listener;

        private long openTimestamp;

        RiHttpEventListener(@NonNull Consumer<CharSequence> listener) {
            this.listener = listener;
            this.openTimestamp = 0;
        }

        @Override
        public void onOpen(@NonNull HttpRequest request, @NonNull Proxy proxy, @NonNull HttpAuthScheme scheme) {
            openTimestamp = System.currentTimeMillis();
            String message = WebEvents.onQuery(request.getMethod().name(), request.getQuery(), proxy);
            if (!HttpAuthScheme.NONE.equals(scheme)) {
                message += " with auth '" + scheme.name() + "'";
            }
            listener.accept(message);
        }

        @Override
        public void onSuccess(@NonNull Supplier<String> contentType) {
            long elapsed = System.currentTimeMillis() - openTimestamp;
            listener.accept(String.format(Locale.ROOT, "Parsing '%s' content-type (%dms)", contentType.get(), elapsed));
        }

        @Override
        public void onRedirection(@NonNull URL oldUrl, @NonNull URL newUrl) {
            listener.accept(WebEvents.onRedirection(oldUrl, newUrl));
        }

        @Override
        public void onUnauthorized(@NonNull URL url, @NonNull HttpAuthScheme oldScheme, @NonNull HttpAuthScheme newScheme) {
            listener.accept(String.format(Locale.ROOT, "Authenticating %s with '%s'", url, newScheme.name()));
        }

        @Override
        public void onEvent(@NonNull String message) {
            listener.accept(message);
        }
    }

    @lombok.AllArgsConstructor
    private static final class RiHttpAuthenticator implements HttpAuthenticator {

        @lombok.NonNull
        private final WebSource source;

        @lombok.NonNull
        private final List<Authenticator> authenticators;

        private final @NonNull WebCaching caching;

        private final @Nullable EventListener onEvent;

        private final @Nullable ErrorListener onError;

        @Override
        public @Nullable PasswordAuthentication getPasswordAuthentication(URL url) {
            if (isDifferentAuthScope(url)) {
                return null;
            }
            return authenticators.stream()
                    .map(this::getPasswordAuthentication)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void invalidate(@NonNull URL url) {
            if (isDifferentAuthScope(url)) {
                return;
            }
            authenticators.forEach(this::invalidate);
        }

        private boolean isDifferentAuthScope(URL url) {
            return !url.getHost().equals(source.getEndpoint().getHost())
                    || url.getPort() != source.getEndpoint().getPort();
        }

        private PasswordAuthentication getPasswordAuthentication(Authenticator authenticator) {
            try {
                return authenticator.getPasswordAuthenticationOrNull(source, caching, onEvent, onError);
            } catch (IOException ex) {
                if (onEvent != null) {
                    onEvent.accept(authenticator.getAuthenticatorId(), "Failed to get password authentication: " + ex.getMessage());
                }
                return null;
            }
        }

        private void invalidate(Authenticator authenticator) {
            try {
                authenticator.invalidateAuthentication(source, caching, onEvent, onError);
            } catch (IOException ex) {
                if (onEvent != null) {
                    onEvent.accept(authenticator.getAuthenticatorId(), "Failed to invalidate password authentication: " + ex.getMessage());
                }
            }
        }
    }

    @lombok.AllArgsConstructor
    private static final class ByteCountingClient implements HttpClient {

        @lombok.NonNull
        private final HttpClient delegate;

        @lombok.NonNull
        private final Consumer<CharSequence> listener;

        @Override
        public @NonNull HttpResponse send(@NonNull HttpRequest request) throws IOException {
            return new ByteCountingResponse(delegate.send(request), listener);
        }
    }

    @lombok.AllArgsConstructor
    private static final class ByteCountingResponse implements HttpResponse {

        @lombok.NonNull
        private final HttpResponse delegate;

        @lombok.NonNull
        private final Consumer<CharSequence> listener;

        private final AtomicLong byteCount = new AtomicLong();

        @Override
        public @NonNull nbbrd.io.net.MediaType getContentType() throws IOException {
            return delegate.getContentType();
        }

        @Override
        public @NonNull InputStream getBody() throws IOException {
            return new CountingInputStream(delegate.getBody(), byteCount);
        }

        @Override
        public @NonNull InputStream asDisconnectingInputStream() throws IOException {
            return new CountingInputStream(delegate.asDisconnectingInputStream(), byteCount);
        }

        @Override
        public void close() throws IOException {
            try {
                delegate.close();
            } finally {
                long bytes = byteCount.get();
                if (bytes > 0) {
                    listener.accept(String.format(Locale.ROOT, "Read %s", formatBytes(bytes)));
                }
            }
        }

        private static String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + "B";
            if (bytes < 1024 * 1024) return String.format(Locale.ROOT, "%.1fKB", bytes / 1024.0);
            return String.format(Locale.ROOT, "%.1fMB", bytes / (1024.0 * 1024.0));
        }
    }

    private static final class CountingInputStream extends FilterInputStream {

        private final AtomicLong counter;

        CountingInputStream(InputStream in, AtomicLong counter) {
            super(in);
            this.counter = counter;
        }

        @Override
        public int read() throws IOException {
            int result = super.read();
            if (result != -1) counter.incrementAndGet();
            return result;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int result = super.read(b, off, len);
            if (result > 0) counter.addAndGet(result);
            return result;
        }
    }
}
