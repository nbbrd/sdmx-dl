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
import static be.nbb.sdmx.facade.util.CommonSdmxProperty.CONNECT_TIMEOUT;
import static be.nbb.sdmx.facade.util.CommonSdmxProperty.READ_TIMEOUT;
import be.nbb.sdmx.facade.util.NoOpCursor;
import be.nbb.sdmx.facade.web.SdmxWebEntryPoint;
import java.io.IOException;
import java.util.List;
import internal.web.RestClient;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.client.custom.DotStat;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.AccessLevel;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConnectorRestClient implements RestClient {

    @FunctionalInterface
    public interface ConnectorConstructor {

        @Nonnull
        RestSdmxClient get() throws URISyntaxException;
    }

    public static RestClient.Supplier of(ConnectorConstructor supplier) {
        return (x, prefix, z) -> {
            try {
                RestSdmxClient client = supplier.get();
                client.setEndpoint(getEndpoint(x, prefix));
                configure(client, x.getProperties(), z);
                return new ConnectorRestClient(client);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    public static RestClient.Supplier of(BiFunction<URI, Map<?, ?>, RestSdmxClient> supplier) {
        return (x, prefix, z) -> {
            try {
                RestSdmxClient client = supplier.apply(getEndpoint(x, prefix), x.getProperties());
                configure(client, x.getProperties(), z);
                return new ConnectorRestClient(client);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    @lombok.NonNull
    private final RestSdmxClient connector;

    @Override
    public List<Dataflow> getFlows() throws IOException {
        try {
            return connector
                    .getDataflows()
                    .values()
                    .stream()
                    .map(Util::toFlow)
                    .collect(Collectors.toList());
        } catch (SdmxException ex) {
            throw expected(ex, "Failed to get datasets");
        }
    }

    @Override
    public Dataflow getFlow(DataflowRef ref) throws IOException {
        try {
            return Util.toFlow(connector.getDataflow(ref.getId(), ref.getAgency(), ref.getVersion()));
        } catch (SdmxException ex) {
            throw expected(ex, "Failed to get details from dataset '%s'", ref);
        }
    }

    @Override
    public DataStructure getStructure(DataStructureRef ref) throws IOException {
        try {
            return Util.toStructure(connector.getDataFlowStructure(Util.fromStructureRef(ref), true));
        } catch (SdmxException ex) {
            throw expected(ex, "Failed to get data structure from dataset '%s'", ref);
        }
    }

    @Override
    public DataCursor getData(DataflowRef flowRef, DataQuery query, DataStructure dsd) throws IOException {
        try {
            return connector instanceof HasDataCursor
                    ? getCursor((HasDataCursor) connector, flowRef, dsd, query)
                    : getAdaptedCursor(connector, flowRef, dsd, query);
        } catch (SdmxException ex) {
            if (Util.isNoResultMatchingQuery(ex)) {
                return NoOpCursor.noOp();
            }
            throw expected(ex, "Failed to get data from dataset '%s' with key '%s'", flowRef, query.getKey());
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

    private static DataCursor getCursor(HasDataCursor connector, DataflowRef flowRef, DataStructure dsd, DataQuery query) throws SdmxException, IOException {
        return connector.getDataCursor(flowRef, dsd, query.getKey(), isSeriesKeyOnly(query));
    }

    private static DataCursor getAdaptedCursor(RestSdmxClient connector, DataflowRef flowRef, DataStructure dsd, DataQuery query) throws SdmxException {
        return new PortableTimeSeriesCursor(connector.getTimeSeries(Util.fromFlowQuery(flowRef, dsd.getRef()), Util.fromStructure(dsd), query.getKey().toString(), null, null, isSeriesKeyOnly(query), null, false), ObsParser.standard());
    }

    private static boolean isSeriesKeyOnly(DataQuery query) {
        return query.getDetail().equals(DataQueryDetail.SERIES_KEYS_ONLY);
    }

    private static IOException expected(SdmxException ex, String format, Object... args) {
        return new IOException(String.format(format, args), ex);
    }

    private static void configure(RestSdmxClient client, Map<?, ?> info, LanguagePriorityList languages) {
        client.setLanguages(Util.fromLanguages(languages));
        client.setConnectTimeout(CONNECT_TIMEOUT.get(info, DEFAULT_CONNECT_TIMEOUT));
        client.setReadTimeout(READ_TIMEOUT.get(info, DEFAULT_READ_TIMEOUT));
    }

    @Nonnull
    private static URI getEndpoint(@Nonnull SdmxWebEntryPoint o, @Nonnull String prefix) throws URISyntaxException {
        return new URI(o.getUri().toString().substring(prefix.length()));
    }

    private final static int DEFAULT_CONNECT_TIMEOUT = 1000 * 60 * 2; // 2 minutes
    private final static int DEFAULT_READ_TIMEOUT = 1000 * 60 * 2; // 2 minutes
}
