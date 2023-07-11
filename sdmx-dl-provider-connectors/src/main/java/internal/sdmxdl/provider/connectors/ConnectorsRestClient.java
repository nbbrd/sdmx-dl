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
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.EventListener;
import sdmxdl.*;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.HasMarker;
import sdmxdl.provider.Marker;
import sdmxdl.provider.web.RestClient;
import sdmxdl.provider.web.RestClientSupplier;
import sdmxdl.provider.web.WebEvents;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.SSLFactory;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sdmxdl.provider.web.WebProperties.*;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConnectorsRestClient implements RestClient {

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

    public static @NonNull RestClientSupplier ofSpecific(@NonNull SpecificSupplier supplier) {
        return ofSpecific(supplier, ObsParser::newDefault);
    }

    public static @NonNull RestClientSupplier ofSpecific(@NonNull SpecificSupplier supplier, @NonNull Supplier<ObsParser> obsFactory) {
        return (source, languages, context) -> {
            try {
                RestSdmxClient client = supplier.get();
                client.setEndpoint(source.getEndpoint());
                configure(client, source, context);
                return new ConnectorsRestClient(HasMarker.of(source), client, obsFactory);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    public static @NonNull RestClientSupplier ofGeneric(@NonNull GenericSupplier supplier) {
        return ofGeneric(supplier, ObsParser::newDefault);
    }

    public static @NonNull RestClientSupplier ofGeneric(@NonNull GenericSupplier supplier, @NonNull Supplier<ObsParser> obsFactory) {
        return (source, languages, context) -> {
            RestSdmxClient client = supplier.get(source.getEndpoint(), source.getProperties());
            configure(client, source, context);
            return new ConnectorsRestClient(HasMarker.of(source), client, obsFactory);
        };
    }

    @lombok.NonNull
    private final Marker marker;

    @lombok.NonNull
    private final RestSdmxClient connector;

    @lombok.NonNull
    private final Supplier<ObsParser> dataFactory;

    @Override
    public @NonNull Marker getMarker() {
        return marker;
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
            throw wrap(ex, "Failed to get dataflows from '%s'", marker);
        }
    }

    @Override
    public @NonNull Dataflow getFlow(@NonNull DataflowRef ref) throws IOException {
        try {
            return Connectors.toFlow(connector.getDataflow(ref.getId(), ref.getAgency(), ref.getVersion()));
        } catch (SdmxException ex) {
            throw wrap(ex, "Failed to get dataflow '%s' from '%s'", ref, marker);
        }
    }

    @Override
    public @NonNull DataStructure getStructure(@NonNull DataStructureRef ref) throws IOException {
        try {
            return Connectors.toStructure(connector.getDataFlowStructure(Connectors.fromStructureRef(ref), true));
        } catch (SdmxException ex) {
            throw wrap(ex, "Failed to get datastructure '%s' from '%s'", ref, marker);
        }
    }

    @Override
    public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull DataStructure dsd) throws IOException {
        try {
            List<PortableTimeSeries<Double>> data = getData(connector, ref, dsd);
            return PortableTimeSeriesCursor.of(data, dataFactory, dsd).asStream();
        } catch (SdmxException ex) {
            if (Connectors.isNoResultMatchingQuery(ex)) {
                return Stream.empty();
            }
            throw wrap(ex, "Failed to get data '%s' from '%s'", ref, marker);
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
            throw wrap(ex, "Failed to get codelist '%s' from '%s'", ref, marker);
        }
    }

    @Override
    public @NonNull Set<Feature> getSupportedFeatures() {
        return connector instanceof HasDetailSupported
                && ((HasDetailSupported) connector).isDetailSupported()
                ? EnumSet.of(Feature.DATA_QUERY_ALL_KEYWORD, Feature.DATA_QUERY_DETAIL)
                : EnumSet.of(Feature.DATA_QUERY_ALL_KEYWORD);
    }

    @Override
    public void testClient() throws IOException {
        try {
            connector.getDataflows();
        } catch (SdmxException ex) {
            throw wrap(ex, "Failed to ping '%s' : '%s'", marker, ex.getMessage());
        }
    }

    public static final List<String> CONNECTORS_CONNECTION_PROPERTIES = BaseProperty.keysOf(
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
        return new IOException(String.format(Locale.ROOT, format, args), ex);
    }

    private static void configure(RestSdmxClient client, SdmxWebSource source, WebContext context) {
//        client.setLanguages(Connectors.fromLanguages(context.getLanguages()));
        client.setConnectTimeout(CONNECT_TIMEOUT_PROPERTY.get(source.getProperties()));
        client.setReadTimeout(READ_TIMEOUT_PROPERTY.get(source.getProperties()));
        Network network = context.getNetwork(source);
        client.setProxySelector(network.getProxySelector());
        SSLFactory sslFactory = network.getSSLFactory();
        client.setSslSocketFactory(sslFactory.getSSLSocketFactory());
        client.setHostnameVerifier(sslFactory.getHostnameVerifier());
        client.setMaxRedirects(MAX_REDIRECTS_PROPERTY.get(source.getProperties()));
        RestSdmxEventListener eventListener = new DefaultRestSdmxEventListener(source, context.getOnEvent(), client.getName());
        client.setRedirectionEventListener(eventListener);
        client.setOpenEventListener(eventListener);
    }

    @lombok.AllArgsConstructor
    private static final class DefaultRestSdmxEventListener implements RestSdmxEventListener {

        @NonNull
        private final SdmxWebSource source;

        private final @Nullable EventListener<? super SdmxWebSource> listener;

        @NonNull
        private final String marker;

        @Override
        public void onSdmxEvent(RestSdmxEvent event) {
            if (listener != null) {
                if (event instanceof RedirectionEvent) {
                    RedirectionEvent redirectionEvent = (RedirectionEvent) event;
                    listener.accept(source, marker, WebEvents.onRedirection(redirectionEvent.getUrl(), redirectionEvent.getRedirection()));
                } else if (event instanceof OpenEvent) {
                    OpenEvent openEvent = (OpenEvent) event;
                    listener.accept(source, marker, WebEvents.onQuery(openEvent.getUrl(), openEvent.getProxy()));
                }
            }
        }
    }

    static {
        ConnectorsConfigFix.fixConfiguration();
    }
}
