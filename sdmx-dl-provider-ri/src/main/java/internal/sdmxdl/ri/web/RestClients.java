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

import internal.util.rest.HttpRest;
import internal.util.rest.MediaType;
import nbbrd.design.VisibleForTesting;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.About;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebAuthenticator;
import sdmxdl.web.spi.SdmxWebContext;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static sdmxdl.util.web.SdmxWebProperty.*;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class RestClients {

    public static final List<String> CONNECTION_PROPERTIES = Collections.unmodifiableList(
            Arrays.asList(
                    CONNECT_TIMEOUT_PROPERTY.getKey(),
                    READ_TIMEOUT_PROPERTY.getKey(),
                    MAX_REDIRECTS_PROPERTY.getKey(),
                    PREEMPTIVE_AUTHENTICATION_PROPERTY.getKey()
            ));

    public static HttpRest.@NonNull Context getRestContext(@NonNull SdmxWebSource o, @NonNull SdmxWebContext context) {
        return HttpRest.Context
                .builder()
                .readTimeout(READ_TIMEOUT_PROPERTY.get(o.getProperties()))
                .connectTimeout(CONNECT_TIMEOUT_PROPERTY.get(o.getProperties()))
                .maxRedirects(MAX_REDIRECTS_PROPERTY.get(o.getProperties()))
                .preemptiveAuthentication(PREEMPTIVE_AUTHENTICATION_PROPERTY.get(o.getProperties()))
                .proxySelector(context.getProxySelector())
                .sslSocketFactory(context.getSslSocketFactory())
                .hostnameVerifier(context.getHostnameVerifier())
                .listener(new DefaultEventListener(o, context.getEventListener()))
                .authenticator(new DefaultAuthenticator(o, context.getAuthenticators(), context.getEventListener()))
                .userAgent(System.getProperty(HTTP_AGENT, DEFAULT_USER_AGENT))
                .build();
    }

    @VisibleForTesting
    static final String HTTP_AGENT = "http.agent";

    private static final String DEFAULT_USER_AGENT = About.NAME + "/" + About.VERSION;

    @lombok.AllArgsConstructor
    private static final class DefaultEventListener implements HttpRest.EventListener {

        @lombok.NonNull
        private final SdmxWebSource source;

        @lombok.NonNull
        private final SdmxWebListener listener;

        @Override
        public void onOpen(URL query, List<MediaType> mediaTypes, String langs, Proxy proxy, HttpRest.AuthScheme scheme) {
            Objects.requireNonNull(query);
            Objects.requireNonNull(mediaTypes);
            Objects.requireNonNull(langs);
            Objects.requireNonNull(proxy);
            Objects.requireNonNull(scheme);
            if (listener.isEnabled()) {
                if (HttpRest.AuthScheme.NONE.equals(scheme)) {
                    listener.onWebSourceEvent(source, String.format("Querying %s with proxy '%s'", query, proxy));
                } else {
                    listener.onWebSourceEvent(source, String.format("Querying %s with proxy '%s' and auth '%s'", query, proxy, scheme.name()));
                }
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
                listener.onWebSourceEvent(source, String.format("Redirecting to %s", newUrl));
            }
        }

        @Override
        public void onUnauthorized(URL url, HttpRest.AuthScheme oldScheme, HttpRest.AuthScheme newScheme) {
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
    private static final class DefaultAuthenticator implements HttpRest.Authenticator {

        @lombok.NonNull
        private final SdmxWebSource source;

        @lombok.NonNull
        private final List<SdmxWebAuthenticator> authenticators;

        @lombok.NonNull
        private final SdmxWebListener listener;

        @Override
        public @Nullable PasswordAuthentication getPasswordAuthentication(URL url) {
            if (!isSameAuthScope(url)) {
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
            if (!isSameAuthScope(url)) {
                return;
            }
            authenticators.forEach(this::invalidate);
        }

        private boolean isSameAuthScope(URL url) {
            return url.getHost().equals(source.getEndpoint().getHost())
                    && url.getPort() == source.getEndpoint().getPort();
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
