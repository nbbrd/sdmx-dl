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

import internal.util.rest.RestClient;
import internal.util.rest.RestClientImpl;
import nbbrd.io.function.IOConsumer;
import sdmxdl.util.web.SdmxWebProperty;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class RestClients {

    public RestClient getRestClient(SdmxWebSource o, SdmxWebContext context) {
        return getRestClient(o, context, IOConsumer.noOp());
    }

    public RestClient getRestClient(SdmxWebSource o, SdmxWebContext context, IOConsumer<HttpURLConnection> validator) {
        return RestClientImpl.of(
                SdmxWebProperty.getReadTimeout(o.getProperties()),
                SdmxWebProperty.getConnectTimeout(o.getProperties()),
                SdmxWebProperty.getMaxRedirects(o.getProperties()),
                context.getProxySelector(),
                context.getSslSocketFactory(),
                new DefaultEventListener(o, context.getEventListener()),
                validator
        );
    }

    public final List<String> CONNECTION_PROPERTIES = Collections.unmodifiableList(
            Arrays.asList(
                    SdmxWebProperty.CONNECT_TIMEOUT_PROPERTY,
                    SdmxWebProperty.READ_TIMEOUT_PROPERTY,
                    SdmxWebProperty.MAX_REDIRECTS_PROPERTY
            ));

    @lombok.AllArgsConstructor
    private static final class DefaultEventListener implements RestClientImpl.EventListener {

        @lombok.NonNull
        private final SdmxWebSource source;

        @lombok.NonNull
        private final SdmxWebListener listener;

        @Override
        public void onOpenStream(URL query, String mediaType, String langs) {
            if (listener.isEnabled()) {
                listener.onSourceEvent(source, String.format("Querying '%s'", query));
            }
        }

        @Override
        public void onRedirection(URL oldUrl, URL newUrl) {
            if (listener.isEnabled()) {
                listener.onSourceEvent(source, String.format("Redirecting to '%s'", newUrl));
            }
        }

        @Override
        public void onProxy(URL query, Proxy proxy) {
            if (listener.isEnabled()) {
                listener.onSourceEvent(source, String.format("Using proxy '%s'", proxy));
            }
        }
    }
}
