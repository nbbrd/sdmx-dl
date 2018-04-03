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
package internal.connectors;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.DataQueryDetail;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.parser.ObsParser;
import static internal.web.SdmxWebProperty.*;
import be.nbb.sdmx.facade.util.NoOpCursor;
import java.io.IOException;
import java.util.List;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.client.custom.DotStat;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import internal.web.SdmxWebClient;
import java.util.Arrays;
import java.util.Collections;
import be.nbb.sdmx.facade.web.spi.SdmxWebContext;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConnectorRestClient implements SdmxWebClient {

    @FunctionalInterface
    public interface SpecificSupplier {

        @Nonnull
        RestSdmxClient get() throws URISyntaxException;
    }

    @FunctionalInterface
    public interface GenericSupplier {

        @Nonnull
        RestSdmxClient get(@Nonnull URI uri, @Nonnull Map<?, ?> properties) throws URISyntaxException;
    }

    @Nonnull
    public static SdmxWebClient.Supplier of(@Nonnull SpecificSupplier supplier) {
        return (source, langs, context) -> {
            try {
                RestSdmxClient client = supplier.get();
                client.setEndpoint(source.getEndpoint().toURI());
                configure(client, source.getProperties(), langs, context);
                return new ConnectorRestClient(source.getName(), client);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    @Nonnull
    public static SdmxWebClient.Supplier of(@Nonnull GenericSupplier supplier) {
        return (source, langs, context) -> {
            try {
                RestSdmxClient client = supplier.get(source.getEndpoint().toURI(), source.getProperties());
                configure(client, source.getProperties(), langs, context);
                return new ConnectorRestClient(source.getName(), client);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    @lombok.NonNull
    private final String name;

    @lombok.NonNull
    private final RestSdmxClient connector;

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
    public DataCursor getData(DataflowRef flowRef, DataQuery query, DataStructure dsd) throws IOException {
        try {
            return connector instanceof HasDataCursor
                    ? getCursor((HasDataCursor) connector, flowRef, dsd, query)
                    : getAdaptedCursor(connector, flowRef, dsd, query);
        } catch (SdmxException ex) {
            if (Connectors.isNoResultMatchingQuery(ex)) {
                return NoOpCursor.noOp();
            }
            throw wrap(ex, "Failed to get data '%s' with %s from '%s'", flowRef, query, name);
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

    private static DataCursor getCursor(HasDataCursor connector, DataflowRef flowRef, DataStructure dsd, DataQuery query) throws SdmxException, IOException {
        return connector.getDataCursor(flowRef, dsd, query.getKey(), isSeriesKeyOnly(query));
    }

    private static DataCursor getAdaptedCursor(RestSdmxClient connector, DataflowRef flowRef, DataStructure dsd, DataQuery query) throws SdmxException {
        return new PortableTimeSeriesCursor(connector.getTimeSeries(Connectors.fromFlowQuery(flowRef, dsd.getRef()), Connectors.fromStructure(dsd), query.getKey().toString(), null, null, isSeriesKeyOnly(query), null, false), ObsParser.standard());
    }

    private static boolean isSeriesKeyOnly(DataQuery query) {
        return query.getDetail().equals(DataQueryDetail.SERIES_KEYS_ONLY);
    }

    private static IOException wrap(SdmxException ex, String format, Object... args) {
        return new IOException(String.format(format, args), ex);
    }

    private static void configure(RestSdmxClient client, Map<?, ?> info, LanguagePriorityList langs, SdmxWebContext context) {
        client.setLanguages(Connectors.fromLanguages(langs));
        client.setConnectTimeout(getConnectTimeout(info));
        client.setReadTimeout(getReadTimeout(info));
        // TODO: context + maxRedirections
    }
}
