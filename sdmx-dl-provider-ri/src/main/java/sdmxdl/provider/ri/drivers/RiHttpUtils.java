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
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.EventListener;
import sdmxdl.Languages;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.provider.web.WebEvents;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Authenticator;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.WebContext;

import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
            PREEMPTIVE_AUTH_PROPERTY,
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
        return newClient(source, newContext(source, context));
    }

    public static @NonNull HttpClient newClient(@NonNull WebSource source, @NonNull HttpContext context) {
        HttpClient result = new DefaultHttpClient(context);
        File dumpFolder = DUMP_FOLDER_PROPERTY.get(source.getProperties());
        return dumpFolder != null ? newDumpingClient(context, result, dumpFolder) : result;
    }

    public static @NonNull HttpContext newContext(@NonNull WebSource source, @NonNull WebContext context) {
        Network network = context.getNetwork(source);
        return HttpContext
                .builder()
                .readTimeout(READ_TIMEOUT_PROPERTY.get(source.getProperties()))
                .connectTimeout(CONNECT_TIMEOUT_PROPERTY.get(source.getProperties()))
                .maxRedirects(MAX_REDIRECTS_PROPERTY.get(source.getProperties()))
                .preemptiveAuthentication(PREEMPTIVE_AUTH_PROPERTY.get(source.getProperties()))
                .proxySelector(network::getProxySelector)
                .sslSocketFactory(() -> network.getSSLFactory().getSSLSocketFactory())
                .hostnameVerifier(() -> network.getSSLFactory().getHostnameVerifier())
                .urlConnectionFactory(() -> network.getURLConnectionFactory()::openConnection)
                .listener(context.getOnEvent() != null ? new RiHttpEventListener(context.getOnEvent().asConsumer(source, "RI_HTTP")) : HttpEventListener.noOp())
                .authenticator(new RiHttpAuthenticator(source, context.getAuthenticators(), context.getOnEvent()))
                .userAgent(USER_AGENT_PROPERTY.get(source.getProperties()))
                .build();
    }

    private static DumpingClient newDumpingClient(HttpContext context, HttpClient client, File dumpFolder) {
        return new DumpingClient(dumpFolder.toPath(), client, file -> context.getListener().onEvent("Dumping " + file.toUri()));
    }

    @lombok.AllArgsConstructor
    private static final class RiHttpEventListener implements HttpEventListener {

        private final @NonNull Consumer<CharSequence> listener;

        @Override
        public void onOpen(@NonNull HttpRequest request, @NonNull Proxy proxy, @NonNull HttpAuthScheme scheme) {
            String message = WebEvents.onQuery(request.getMethod().name(), request.getQuery(), proxy);
            if (!HttpAuthScheme.NONE.equals(scheme)) {
                message += " with auth '" + scheme.name() + "'";
            }
            listener.accept(message);
        }

        @Override
        public void onSuccess(@NonNull Supplier<String> contentType) {
            listener.accept(String.format(Locale.ROOT, "Parsing '%s' content-type", contentType.get()));
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

        private final @Nullable EventListener<? super WebSource> listener;

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
                return authenticator.getPasswordAuthenticationOrNull(source);
            } catch (IOException ex) {
                if (listener != null) {
                    listener.accept(source, authenticator.getAuthenticatorId(), "Failed to get password authentication: " + ex.getMessage());
                }
                return null;
            }
        }

        private void invalidate(Authenticator authenticator) {
            try {
                authenticator.invalidateAuthentication(source);
            } catch (IOException ex) {
                if (listener != null) {
                    listener.accept(source, authenticator.getAuthenticatorId(), "Failed to invalidate password authentication: " + ex.getMessage());
                }
            }
        }
    }
}
