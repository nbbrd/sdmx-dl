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

import sdmxdl.DataCursor;
import sdmxdl.DataStructure;
import sdmxdl.DataStructureRef;
import sdmxdl.Dataflow;
import sdmxdl.DataflowRef;
import sdmxdl.util.parser.DataFactory;
import static sdmxdl.util.web.SdmxWebProperty.*;
import java.io.IOException;
import java.util.List;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.client.custom.DotStat;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import sdmxdl.util.web.SdmxWebClient;
import java.util.Arrays;
import java.util.Collections;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.util.web.DataRequest;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConnectorRestClient implements SdmxWebClient {

    @FunctionalInterface
    public interface SpecificSupplier {

        @NonNull
        RestSdmxClient get() throws URISyntaxException;
    }

    @FunctionalInterface
    public interface GenericSupplier {

        @NonNull
        RestSdmxClient get(@NonNull URI uri, @NonNull Map<?, ?> properties) throws URISyntaxException;
    }

    public static SdmxWebClient.@NonNull Supplier of(@NonNull SpecificSupplier supplier, @NonNull DataFactory dataFactory) {
        return (source, context) -> {
            try {
                RestSdmxClient client = supplier.get();
                client.setEndpoint(source.getEndpoint().toURI());
                configure(client, source.getProperties(), context);
                return new ConnectorRestClient(source.getName(), client, dataFactory);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    public static SdmxWebClient.@NonNull Supplier of(@NonNull GenericSupplier supplier, @NonNull DataFactory dataFactory) {
        return (source, context) -> {
            try {
                RestSdmxClient client = supplier.get(source.getEndpoint().toURI(), source.getProperties());
                configure(client, source.getProperties(), context);
                return new ConnectorRestClient(source.getName(), client, dataFactory);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    @lombok.NonNull
    private final String name;

    @lombok.NonNull
    private final RestSdmxClient connector;

    @lombok.NonNull
    private final DataFactory dataFactory;

    @Override
    public String getName() throws IOException {
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
    public DataCursor getData(DataRequest request, DataStructure dsd) throws IOException {
        try {
            List<PortableTimeSeries<Double>> data = getData(connector, request, dsd);
            return PortableTimeSeriesCursor.of(data, dataFactory, dsd);
        } catch (SdmxException ex) {
            if (Connectors.isNoResultMatchingQuery(ex)) {
                return DataCursor.empty();
            }
            throw wrap(ex, "Failed to get data '%s' from '%s'", request, name);
        }
    }

    @Override
    public boolean isSeriesKeysOnlySupported() throws IOException {
        return connector instanceof HasSeriesKeysOnlySupported
                && ((HasSeriesKeysOnlySupported) connector).isSeriesKeysOnlySupported();
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef ref) throws IOException {
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

    public static final List<String> CONNECTION_PROPERTIES = Collections.unmodifiableList(
            Arrays.asList(
                    CONNECT_TIMEOUT_PROPERTY,
                    READ_TIMEOUT_PROPERTY
            ));

    private static List<PortableTimeSeries<Double>> getData(RestSdmxClient connector, DataRequest request, DataStructure dsd) throws SdmxException {
        return connector.getTimeSeries(Connectors.fromFlowQuery(request.getFlowRef(), dsd.getRef()), Connectors.fromStructure(dsd), request.getKey().toString(), null, null, request.getFilter().isSeriesKeyOnly(), null, false);
    }

    private static IOException wrap(SdmxException ex, String format, Object... args) {
        return new IOException(String.format(format, args), ex);
    }

    private static void configure(RestSdmxClient client, Map<?, ?> info, SdmxWebContext context) {
        client.setLanguages(Connectors.fromLanguages(context.getLanguages()));
        client.setConnectTimeout(getConnectTimeout(info));
        client.setReadTimeout(getReadTimeout(info));
        client.setProxySelector(context.getProxySelector());
        client.setSslSocketFactory(context.getSslSocketFactory());
        // TODO: maxRedirections
    }

    static {
        ConnectorsConfigFix.fixConfiguration();
    }
}
