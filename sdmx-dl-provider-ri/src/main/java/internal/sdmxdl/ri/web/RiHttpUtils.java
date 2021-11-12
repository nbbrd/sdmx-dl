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
package internal.sdmxdl.ri.web;

import internal.util.http.*;
import internal.util.http.ext.DumpingClient;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.BaseProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.About;
import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.SdmxMediaType;
import sdmxdl.util.web.SdmxWebEvents;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebAuthenticator;
import sdmxdl.web.spi.SdmxWebContext;

import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static sdmxdl.util.web.SdmxWebProperty.*;

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

    public static final MediaType GENERIC_XML_TYPE = MediaType.parse(SdmxMediaType.GENERIC_XML);
    public static final MediaType STRUCTURE_21_TYPE = MediaType.parse(SdmxMediaType.STRUCTURE_21);
    public static final MediaType GENERIC_DATA_21_TYPE = MediaType.parse(SdmxMediaType.GENERIC_DATA_21);
    public static final MediaType STRUCTURE_SPECIFIC_DATA_21_TYPE = MediaType.parse(SdmxMediaType.STRUCTURE_SPECIFIC_DATA_21);
    public static final MediaType STRUCTURE_SPECIFIC_DATA_20_TYPE = MediaType.parse(SdmxMediaType.STRUCTURE_SPECIFIC_DATA_20);
    public static final MediaType JSON_TYPE = MediaType.parse("application/json");
    public static final MediaType ZIP_TYPE = MediaType.parse("application/zip");

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

    public static @NonNull HttpClient newClient(@NonNull SdmxWebSource source, @NonNull SdmxWebContext context) {
        return newClient(newContext(source, context));
    }

    public static @NonNull HttpClient newClient(@NonNull HttpContext context) {
        HttpClient result = new DefaultHttpClient(context, HttpURLConnectionFactoryLoader.get());
        File dumpFile = SDMXDL_RI_WEB_DUMP_FOLDER_PROPERTY.get(System.getProperties());
        return dumpFile != null ? newDumpingClient(context, result, dumpFile) : result;
    }

    private static DumpingClient newDumpingClient(HttpContext context, HttpClient client, File dumpFile) {
        return new DumpingClient(dumpFile.toPath(), client, file -> context.getListener().onEvent("Dumping '" + file + "'"));
    }

    public static @NonNull HttpContext newContext(@NonNull SdmxWebSource source, @NonNull SdmxWebContext context) {
        return HttpContext
                .builder()
                .readTimeout(READ_TIMEOUT_PROPERTY.get(source.getProperties()))
                .connectTimeout(CONNECT_TIMEOUT_PROPERTY.get(source.getProperties()))
                .maxRedirects(MAX_REDIRECTS_PROPERTY.get(source.getProperties()))
                .preemptiveAuthentication(PREEMPTIVE_AUTHENTICATION_PROPERTY.get(source.getProperties()))
                .proxySelector(context.getNetwork()::getProxySelector)
                .sslSocketFactory(context.getNetwork()::getSslSocketFactory)
                .hostnameVerifier(context.getNetwork()::getHostnameVerifier)
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
        private final SdmxWebListener listener;

        @Override
        public void onOpen(HttpRequest request, Proxy proxy, HttpAuthScheme scheme) {
            Objects.requireNonNull(request);
            Objects.requireNonNull(proxy);
            Objects.requireNonNull(scheme);
            if (listener.isEnabled()) {
                String message = SdmxWebEvents.onQuery(request.getQuery(), proxy);
                if (!HttpAuthScheme.NONE.equals(scheme)) {
                    message += " with auth '" + scheme.name() + "'";
                }
                listener.onWebSourceEvent(source, message);
            }
        }

        @Override
        public void onSuccess(@NonNull MediaType mediaType) {
            Objects.requireNonNull(mediaType);
            if (listener.isEnabled()) {
                listener.onWebSourceEvent(source, String.format("Parsing '%s'", mediaType));
            }
        }

        @Override
        public void onRedirection(URL oldUrl, URL newUrl) {
            Objects.requireNonNull(oldUrl);
            Objects.requireNonNull(newUrl);
            if (listener.isEnabled()) {
                listener.onWebSourceEvent(source, SdmxWebEvents.onRedirection(oldUrl, newUrl));
            }
        }

        @Override
        public void onUnauthorized(URL url, HttpAuthScheme oldScheme, HttpAuthScheme newScheme) {
            Objects.requireNonNull(url);
            Objects.requireNonNull(oldScheme);
            Objects.requireNonNull(newScheme);
            if (listener.isEnabled()) {
                listener.onWebSourceEvent(source, String.format("Authenticating %s with '%s'", url, newScheme.name()));
            }
        }

        @Override
        public void onEvent(@NonNull String message) {
            Objects.requireNonNull(message);
            if (listener.isEnabled()) {
                listener.onWebSourceEvent(source, message);
            }
        }
    }

    @lombok.AllArgsConstructor
    private static final class RiHttpAuthenticator implements HttpAuthenticator {

        @lombok.NonNull
        private final SdmxWebSource source;

        @lombok.NonNull
        private final List<SdmxWebAuthenticator> authenticators;

        @lombok.NonNull
        private final SdmxWebListener listener;

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

        private PasswordAuthentication getPasswordAuthentication(SdmxWebAuthenticator authenticator) {
            try {
                return authenticator.getPasswordAuthentication(source);
            } catch (IOException ex) {
                listener.onWebSourceEvent(source, "Failed to get password authentication: " + ex.getMessage());
                return null;
            }
        }

        private void invalidate(SdmxWebAuthenticator authenticator) {
            try {
                authenticator.invalidate(source);
            } catch (IOException ex) {
                listener.onWebSourceEvent(source, "Failed to invalidate password authentication: " + ex.getMessage());
            }
        }
    }
}
