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
package internal.sdmxdl.provider.connectors;

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.event.OpenEvent;
import it.bancaditalia.oss.sdmx.event.RedirectionEvent;
import it.bancaditalia.oss.sdmx.event.RestSdmxEvent;
import it.bancaditalia.oss.sdmx.event.RestSdmxEventListener;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.io.text.BaseProperty;
import sdmxdl.*;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.web.RestClient;
import sdmxdl.provider.web.RestClientSupplier;
import sdmxdl.provider.web.WebEvents;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sdmxdl.provider.web.WebProperties.*;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConnectorRestClient implements RestClient {

    @FunctionalInterface
    public interface SpecificSupplier {

        @NonNull
        RestSdmxClient get() throws URISyntaxException;
    }

    @FunctionalInterface
    public interface GenericSupplier {

        @NonNull
        RestSdmxClient get(@NonNull URI endpoint, @NonNull Map<String, String> properties);
    }

    public static @NonNull RestClientSupplier of(@NonNull SpecificSupplier supplier, @NonNull Supplier<ObsParser> obsFactory) {
        return (source, context) -> {
            try {
                RestSdmxClient client = supplier.get();
                client.setEndpoint(source.getEndpoint());
                configure(client, source, context);
                return new ConnectorRestClient(source.getName(), client, obsFactory);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    public static @NonNull RestClientSupplier of(@NonNull GenericSupplier supplier, @NonNull Supplier<ObsParser> obsFactory) {
        return (source, context) -> {
            RestSdmxClient client = supplier.get(source.getEndpoint(), source.getProperties());
            configure(client, source, context);
            return new ConnectorRestClient(source.getName(), client, obsFactory);
        };
    }

    @lombok.NonNull
    private final String name;

    @lombok.NonNull
    private final RestSdmxClient connector;

    @lombok.NonNull
    private final Supplier<ObsParser> dataFactory;

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public @NonNull List<Dataflow> getFlows() throws IOException {
        try {
            return connector
                    .getDataflows()
                    .values()
                    .stream()
                    .map(Connectors::toFlow)
                    .collect(Collectors.toList());
        } catch (SdmxException ex) {
            throw wrap(ex, "Failed to get dataflows from '%s'", name);
        }
    }

    @Override
    public @NonNull Dataflow getFlow(@NonNull DataflowRef ref) throws IOException {
        try {
            return Connectors.toFlow(connector.getDataflow(ref.getId(), ref.getAgency(), ref.getVersion()));
        } catch (SdmxException ex) {
            throw wrap(ex, "Failed to get dataflow '%s' from '%s'", ref, name);
        }
    }

    @Override
    public @NonNull DataStructure getStructure(@NonNull DataStructureRef ref) throws IOException {
        try {
            return Connectors.toStructure(connector.getDataFlowStructure(Connectors.fromStructureRef(ref), true));
        } catch (SdmxException ex) {
            throw wrap(ex, "Failed to get datastructure '%s' from '%s'", ref, name);
        }
    }

    @Override
    public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull DataStructure dsd) throws IOException {
        try {
            List<PortableTimeSeries<Double>> data = getData(connector, ref, dsd);
            return PortableTimeSeriesCursor.of(data, dataFactory, dsd).toStream();
        } catch (SdmxException ex) {
            if (Connectors.isNoResultMatchingQuery(ex)) {
                return Stream.empty();
            }
            throw wrap(ex, "Failed to get data '%s' from '%s'", ref, name);
        }
    }

    @Override
    public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException {
        try {
            return Codelist
                    .builder()
                    .ref(ref)
                    .codes(connector.getCodes(ref.getId(), ref.getAgency(), ref.getVersion()))
                    .build();
        } catch (SdmxException ex) {
            throw wrap(ex, "Failed to get codelist '%s' from '%s'", ref, name);
        }
    }

    @Override
    public boolean isDetailSupported() {
        return connector instanceof HasDetailSupported
                && ((HasDetailSupported) connector).isDetailSupported();
    }

    @Override
    public void testClient() throws IOException {
        try {
            connector.getDataflows();
        } catch (SdmxException ex) {
            throw wrap(ex, "Failed to ping '%s' : '%s'", name, ex.getMessage());
        }
    }

    public static final List<String> CONNECTION_PROPERTIES = BaseProperty.keysOf(
            CONNECT_TIMEOUT_PROPERTY,
            READ_TIMEOUT_PROPERTY,
            MAX_REDIRECTS_PROPERTY
    );

    private static List<PortableTimeSeries<Double>> getData(RestSdmxClient connector, DataRef ref, DataStructure dsd) throws SdmxException {
        return connector.getTimeSeries(
                Connectors.fromFlowQuery(ref.getFlowRef(), dsd.getRef()),
                Connectors.fromStructure(dsd), ref.getQuery().getKey().toString(),
                null, null,
                ref.getQuery().getDetail().equals(DataDetail.SERIES_KEYS_ONLY),
                null, false);
    }

    private static IOException wrap(SdmxException ex, String format, Object... args) {
        return new IOException(String.format(format, args), ex);
    }

    private static void configure(RestSdmxClient client, SdmxWebSource source, WebContext context) {
        client.setLanguages(Connectors.fromLanguages(context.getLanguages()));
        client.setConnectTimeout(CONNECT_TIMEOUT_PROPERTY.get(source.getProperties()));
        client.setReadTimeout(READ_TIMEOUT_PROPERTY.get(source.getProperties()));
        client.setProxySelector(context.getNetwork().getProxySelector());
        client.setSslSocketFactory(context.getNetwork().getSSLSocketFactory());
        client.setHostnameVerifier(context.getNetwork().getHostnameVerifier());
        client.setMaxRedirects(MAX_REDIRECTS_PROPERTY.get(source.getProperties()));
        RestSdmxEventListener eventListener = new DefaultRestSdmxEventListener(source, context.getEventListener());
        client.setRedirectionEventListener(eventListener);
        client.setOpenEventListener(eventListener);
    }

    @lombok.AllArgsConstructor
    private static final class DefaultRestSdmxEventListener implements RestSdmxEventListener {

        @lombok.NonNull
        private final SdmxWebSource source;

        @lombok.NonNull
        private final BiConsumer<? super SdmxWebSource, ? super String> listener;

        @Override
        public void onSdmxEvent(RestSdmxEvent event) {
            if (listener != SdmxManager.NO_OP_EVENT_LISTENER) {
                if (event instanceof RedirectionEvent) {
                    RedirectionEvent redirectionEvent = (RedirectionEvent) event;
                    listener.accept(source, WebEvents.onRedirection(redirectionEvent.getUrl(), redirectionEvent.getRedirection()));
                } else if (event instanceof OpenEvent) {
                    OpenEvent openEvent = (OpenEvent) event;
                    listener.accept(source, WebEvents.onQuery(openEvent.getUrl(), openEvent.getProxy()));
                }
            }
        }
    }

    static {
        ConnectorsConfigFix.fixConfiguration();
    }
}
