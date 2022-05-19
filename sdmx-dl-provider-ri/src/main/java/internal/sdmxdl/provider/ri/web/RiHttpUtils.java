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
package internal.sdmxdl.provider.ri.web;

import internal.util.http.*;
import internal.util.http.ext.DumpingClient;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.BaseProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.About;
import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;
import sdmxdl.format.MediaType;
import sdmxdl.provider.web.WebEvents;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebAuthenticator;
import sdmxdl.web.spi.WebContext;

import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static sdmxdl.provider.web.WebProperties.*;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class RiHttpUtils {

    public static final List<String> CONNECTION_PROPERTIES = BaseProperty.keysOf(
            CONNECT_TIMEOUT_PROPERTY,
            READ_TIMEOUT_PROPERTY,
            MAX_REDIRECTS_PROPERTY,
            PREEMPTIVE_AUTHENTICATION_PROPERTY
    );

    // TODO: document these options?
    @VisibleForTesting
    static final Property<File> SDMXDL_RI_WEB_DUMP_FOLDER_PROPERTY =
            Property.of("sdmxdl.ri.web.dump.folder", null, Parser.onFile(), Formatter.onFile());

    @VisibleForTesting
    static final Property<String> HTTP_AGENT =
            Property.of("http.agent", About.NAME + "/" + About.VERSION, Parser.onString(), Formatter.onString());

    public static @NonNull HttpRequest newRequest(@NonNull URL query, @NonNull List<MediaType> mediaTypes, @NonNull LanguagePriorityList langs) {
        return HttpRequest
                .builder()
                .query(query)
                .mediaTypes(mediaTypes)
                .langs(langs.toString())
                .build();
    }

    public static @NonNull HttpClient newClient(@NonNull SdmxWebSource source, @NonNull WebContext context) {
        return newClient(newContext(source, context));
    }

    public static @NonNull HttpClient newClient(@NonNull HttpContext context) {
        HttpClient result = new DefaultHttpClient(context);
        File dumpFolder = SDMXDL_RI_WEB_DUMP_FOLDER_PROPERTY.get(System.getProperties());
        return dumpFolder != null ? newDumpingClient(context, result, dumpFolder) : result;
    }

    private static DumpingClient newDumpingClient(HttpContext context, HttpClient client, File dumpFolder) {
        return new DumpingClient(dumpFolder.toPath(), client, file -> context.getListener().onEvent("Dumping '" + file + "'"));
    }

    public static @NonNull HttpContext newContext(@NonNull SdmxWebSource source, @NonNull WebContext context) {
        return HttpContext
                .builder()
                .readTimeout(READ_TIMEOUT_PROPERTY.get(source.getProperties()))
                .connectTimeout(CONNECT_TIMEOUT_PROPERTY.get(source.getProperties()))
                .maxRedirects(MAX_REDIRECTS_PROPERTY.get(source.getProperties()))
                .preemptiveAuthentication(PREEMPTIVE_AUTHENTICATION_PROPERTY.get(source.getProperties()))
                .proxySelector(context.getNetwork()::getProxySelector)
                .sslSocketFactory(context.getNetwork()::getSSLSocketFactory)
                .hostnameVerifier(context.getNetwork()::getHostnameVerifier)
                .urlConnectionFactory(context.getNetwork()::getURLConnectionFactory)
                .listener(new RiHttpEventListener(source, context.getEventListener()))
                .authenticator(new RiHttpAuthenticator(source, context.getAuthenticators(), context.getEventListener()))
                .userAgent(HTTP_AGENT.get(System.getProperties()))
                .build();
    }

    @lombok.AllArgsConstructor
    private static final class RiHttpEventListener implements HttpEventListener {

        @lombok.NonNull
        private final SdmxWebSource source;

        @lombok.NonNull
        private final BiConsumer<? super SdmxWebSource, ? super String> listener;

        @Override
        public void onOpen(@NonNull HttpRequest request, @NonNull Proxy proxy, @NonNull HttpAuthScheme scheme) {
            if (listener != SdmxManager.NO_OP_EVENT_LISTENER) {
                String message = WebEvents.onQuery(request.getMethod().name(), request.getQuery(), proxy);
                if (!HttpAuthScheme.NONE.equals(scheme)) {
                    message += " with auth '" + scheme.name() + "'";
                }
                listener.accept(source, message);
            }
        }

        @Override
        public void onSuccess(@NonNull Supplier<String> contentType) {
            if (listener != SdmxManager.NO_OP_EVENT_LISTENER) {
                listener.accept(source, String.format("Parsing '%s' content-type", contentType.get()));
            }
        }

        @Override
        public void onRedirection(@NonNull URL oldUrl, @NonNull URL newUrl) {
            if (listener != SdmxManager.NO_OP_EVENT_LISTENER) {
                listener.accept(source, WebEvents.onRedirection(oldUrl, newUrl));
            }
        }

        @Override
        public void onUnauthorized(@NonNull URL url, @NonNull HttpAuthScheme oldScheme, @NonNull HttpAuthScheme newScheme) {
            if (listener != SdmxManager.NO_OP_EVENT_LISTENER) {
                listener.accept(source, String.format("Authenticating %s with '%s'", url, newScheme.name()));
            }
        }

        @Override
        public void onEvent(@NonNull String message) {
            if (listener != SdmxManager.NO_OP_EVENT_LISTENER) {
                listener.accept(source, message);
            }
        }
    }

    @lombok.AllArgsConstructor
    private static final class RiHttpAuthenticator implements HttpAuthenticator {

        @lombok.NonNull
        private final SdmxWebSource source;

        @lombok.NonNull
        private final List<WebAuthenticator> authenticators;

        @lombok.NonNull
        private final BiConsumer<? super SdmxWebSource, ? super String> listener;

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

        private PasswordAuthentication getPasswordAuthentication(WebAuthenticator authenticator) {
            try {
                return authenticator.getPasswordAuthentication(source);
            } catch (IOException ex) {
                listener.accept(source, "Failed to get password authentication: " + ex.getMessage());
                return null;
            }
        }

        private void invalidate(WebAuthenticator authenticator) {
            try {
                authenticator.invalidate(source);
            } catch (IOException ex) {
                listener.accept(source, "Failed to invalidate password authentication: " + ex.getMessage());
            }
        }
    }
}
