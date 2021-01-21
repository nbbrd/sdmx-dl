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
import internal.util.rest.Jdk8RestClient;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.web.SdmxWebAuthenticator;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;

import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static sdmxdl.util.web.SdmxWebProperty.*;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class RestClients {

    public HttpRest.Client getRestClient(SdmxWebSource o, SdmxWebContext context) {
        return new Jdk8RestClient(
                HttpRest.Context
                        .builder()
                        .readTimeout(READ_TIMEOUT_PROPERTY.get(o.getProperties()))
                        .connectTimeout(CONNECT_TIMEOUT_PROPERTY.get(o.getProperties()))
                        .maxRedirects(MAX_REDIRECTS_PROPERTY.get(o.getProperties()))
                        .proxySelector(context.getProxySelector())
                        .sslSocketFactory(context.getSslSocketFactory())
                        .hostnameVerifier(context.getHostnameVerifier())
                        .listener(new DefaultEventListener(o, context.getEventListener()))
                        .authenticator(new DefaultAuthenticator(o, context.getAuthenticator()))
                        .preemptiveAuthentication(PREEMPTIVE_AUTHENTICATION_PROPERTY.get(o.getProperties()))
                        .build()
        );
    }

    public final List<String> CONNECTION_PROPERTIES = Collections.unmodifiableList(
            Arrays.asList(
                    CONNECT_TIMEOUT_PROPERTY.getKey(),
                    READ_TIMEOUT_PROPERTY.getKey(),
                    MAX_REDIRECTS_PROPERTY.getKey(),
                    PREEMPTIVE_AUTHENTICATION_PROPERTY.getKey()
            ));

    @lombok.AllArgsConstructor
    private static final class DefaultEventListener implements HttpRest.EventListener {

        @lombok.NonNull
        private final SdmxWebSource source;

        @lombok.NonNull
        private final SdmxWebListener listener;

        @Override
        public void onOpen(URL query, String mediaType, String langs, Proxy proxy, HttpRest.AuthScheme scheme) {
            if (listener.isEnabled()) {
                if (HttpRest.AuthScheme.NONE.equals(scheme)) {
                    listener.onSourceEvent(source, String.format("Querying '%s' with proxy '%s'", query, proxy));
                } else {
                    listener.onSourceEvent(source, String.format("Querying '%s' with proxy '%s' and auth '%s'", query, proxy, scheme));
                }
            }
        }

        @Override
        public void onRedirection(URL oldUrl, URL newUrl) {
            if (listener.isEnabled()) {
                listener.onSourceEvent(source, String.format("Redirecting to '%s'", newUrl));
            }
        }

        @Override
        public void onUnauthorized(URL url, HttpRest.AuthScheme oldScheme, HttpRest.AuthScheme newScheme) {
            if (listener.isEnabled()) {
                listener.onSourceEvent(source, String.format("Authenticating '%s' with '%s'", url, newScheme));
            }
        }
    }

    @lombok.AllArgsConstructor
    private static final class DefaultAuthenticator implements HttpRest.Authenticator {

        @lombok.NonNull
        private final SdmxWebSource source;

        @lombok.NonNull
        private final SdmxWebAuthenticator authenticator;

        @Override
        public @Nullable PasswordAuthentication getPasswordAuthentication(URL url) {
            return isSameAuthScope(url) ? authenticator.getPasswordAuthentication(source) : null;
        }

        @Override
        public void invalidate(@NonNull URL url) {
            if (isSameAuthScope(url)) {
                authenticator.invalidate(source);
            }
        }

        private boolean isSameAuthScope(URL url) {
            return url.getHost().equals(source.getEndpoint().getHost())
                    && url.getPort() == source.getEndpoint().getPort();
        }
    }
}
