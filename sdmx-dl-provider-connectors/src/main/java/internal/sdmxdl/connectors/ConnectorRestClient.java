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
package internal.sdmxdl.connectors;

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.client.custom.DotStat;
import it.bancaditalia.oss.sdmx.event.OpenEvent;
import it.bancaditalia.oss.sdmx.event.RedirectionEvent;
import it.bancaditalia.oss.sdmx.event.RestSdmxEvent;
import it.bancaditalia.oss.sdmx.event.RestSdmxEventListener;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import lombok.AccessLevel;
import nbbrd.io.text.BaseProperty;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.util.parser.ObsFactories;
import sdmxdl.util.web.SdmxRestClient;
import sdmxdl.util.web.SdmxRestClientSupplier;
import sdmxdl.util.web.SdmxWebEvents;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static sdmxdl.util.web.SdmxWebProperty.*;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConnectorRestClient implements SdmxRestClient {

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

    public static @NonNull SdmxRestClientSupplier of(@NonNull SpecificSupplier supplier, @NonNull String defaultDialect) {
        return (source, context) -> {
            try {
                RestSdmxClient client = supplier.get();
                client.setEndpoint(source.getEndpoint());
                configure(client, source, context);
                return new ConnectorRestClient(source.getName(), client, ObsFactories.getObsFactory(context, source, defaultDialect));
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    public static @NonNull SdmxRestClientSupplier of(@NonNull GenericSupplier supplier, @NonNull String defaultDialect) {
        return (source, context) -> {
            RestSdmxClient client = supplier.get(source.getEndpoint(), source.getProperties());
            configure(client, source, context);
            return new ConnectorRestClient(source.getName(), client, ObsFactories.getObsFactory(context, source, defaultDialect));
        };
    }

    @lombok.NonNull
    private final String name;

    @lombok.NonNull
    private final RestSdmxClient connector;

    @lombok.NonNull
    private final ObsFactory dataFactory;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Dataflow> getFlows() throws IOException {
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
    public Dataflow getFlow(DataflowRef ref) throws IOException {
        try {
            return Connectors.toFlow(connector.getDataflow(ref.getId(), ref.getAgency(), ref.getVersion()));
        } catch (SdmxException ex) {
            throw wrap(ex, "Failed to get dataflow '%s' from '%s'", ref, name);
        }
    }

    @Override
    public DataStructure getStructure(DataStructureRef ref) throws IOException {
        try {
            return Connectors.toStructure(connector.getDataFlowStructure(Connectors.fromStructureRef(ref), true));
        } catch (SdmxException ex) {
            throw wrap(ex, "Failed to get datastructure '%s' from '%s'", ref, name);
        }
    }

    @Override
    public DataCursor getData(DataRef ref, DataStructure dsd) throws IOException {
        try {
            List<PortableTimeSeries<Double>> data = getData(connector, ref, dsd);
            return PortableTimeSeriesCursor.of(data, dataFactory, dsd);
        } catch (SdmxException ex) {
            if (Connectors.isNoResultMatchingQuery(ex)) {
                return DataCursor.empty();
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
    public DataStructureRef peekStructureRef(DataflowRef ref) {
        return connector instanceof DotStat ? DataStructureRef.of(ref.getAgency(), ref.getId(), ref.getVersion()) : null;
    }

    @Override
    public Duration ping() throws IOException {
        try {
            Clock clock = Clock.systemDefaultZone();
            Instant start = clock.instant();
            connector.getDataflows();
            return Duration.between(start, clock.instant());
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
                Connectors.fromStructure(dsd), ref.getKey().toString(),
                null, null,
                ref.getFilter().getDetail().equals(DataFilter.Detail.SERIES_KEYS_ONLY),
                null, false);
    }

    private static IOException wrap(SdmxException ex, String format, Object... args) {
        return new IOException(String.format(format, args), ex);
    }

    private static void configure(RestSdmxClient client, SdmxWebSource source, SdmxWebContext context) {
        client.setLanguages(Connectors.fromLanguages(context.getLanguages()));
        client.setConnectTimeout(CONNECT_TIMEOUT_PROPERTY.get(source.getProperties()));
        client.setReadTimeout(READ_TIMEOUT_PROPERTY.get(source.getProperties()));
        client.setProxySelector(context.getNetwork().getProxySelector());
        client.setSslSocketFactory(context.getNetwork().getSslSocketFactory());
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
        private final SdmxWebListener listener;

        @Override
        public void onSdmxEvent(RestSdmxEvent event) {
            if (listener.isEnabled()) {
                if (event instanceof RedirectionEvent) {
                    RedirectionEvent redirectionEvent = (RedirectionEvent) event;
                    listener.onWebSourceEvent(source, SdmxWebEvents.onRedirection(redirectionEvent.getUrl(), redirectionEvent.getRedirection()));
                } else if (event instanceof OpenEvent) {
                    OpenEvent openEvent = (OpenEvent) event;
                    listener.onWebSourceEvent(source, SdmxWebEvents.onQuery(openEvent.getUrl(), openEvent.getProxy()));
                }
            }
        }
    }

    static {
        ConnectorsConfigFix.fixConfiguration();
    }
}
