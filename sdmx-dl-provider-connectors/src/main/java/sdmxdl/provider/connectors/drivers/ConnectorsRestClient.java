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
package sdmxdl.provider.connectors.drivers;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static sdmxdl.provider.web.DriverProperties.*;

import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.api.SDMXVersion;
import it.bancaditalia.oss.sdmx.client.AbstractRestSdmxClient;
import it.bancaditalia.oss.sdmx.client.Provider;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import nbbrd.io.text.BaseProperty;
import sdmxdl.*;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.Marker;
import sdmxdl.provider.web.RestClient;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
final class ConnectorsRestClient implements RestClient {

    @lombok.NonNull private final Marker marker;

    @lombok.NonNull private final AbstractRestSdmxClient<?> connector;

    @lombok.NonNull private final Supplier<ObsParser> dataFactory;

    @Override
    public @NonNull Marker getMarker() {
        return marker;
    }

    @Override
    public @NonNull List<Flow> getFlows() throws IOException {
        try {
            return connector.getDataflows().values().stream()
                    .map(Connectors::toFlow)
                    .collect(Collectors.toList());
        } catch (SdmxException ex) {
            throw wrap(ex, "Failed to get dataflows from '%s'", marker);
        }
    }

    @Override
    public @NonNull Structure getStructure(@NonNull StructureRef ref) throws IOException {
        try {
            return Connectors.toStructure(connector.getDataFlowStructure(Connectors.fromStructureRef(ref), true));
        } catch (SdmxException ex) {
            throw wrap(ex, "Failed to get datastructure '%s' from '%s'", ref, marker);
        }
    }

    @Override
    public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull Structure dsd) throws IOException {
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
            return Codelist.builder()
                    .ref(ref)
                    .codes(connector.getCodes(ref.getId(), ref.getAgency(), ref.getVersion()))
                    .build();
        } catch (SdmxException ex) {
            throw wrap(ex, "Failed to get codelist '%s' from '%s'", ref, marker);
        }
    }

    @Override
    public @NonNull Set<Feature> getSupportedFeatures() {
        return connector instanceof HasDetailSupported && ((HasDetailSupported) connector).isDetailSupported()
                ? EnumSet.of(Feature.DATA_QUERY_ALL_KEYWORD, Feature.DATA_QUERY_DETAIL)
                : EnumSet.of(Feature.DATA_QUERY_ALL_KEYWORD);
    }

    @NonNull @Override
    public Optional<URI> testClient() throws IOException {
        try {
            connector.getDataflows();
        } catch (SdmxException ex) {
            throw wrap(ex, "Failed to ping '%s' : '%s'", marker, ex.getMessage());
        }
        return Optional.empty();
    }

    public static final List<BaseProperty> CONNECTORS_CONNECTION_PROPERTIES =
            unmodifiableList(asList(CONNECT_TIMEOUT_PROPERTY, READ_TIMEOUT_PROPERTY, MAX_REDIRECTS_PROPERTY));

    private static List<PortableTimeSeries<Double>> getData(
            AbstractRestSdmxClient<?> connector, DataRef ref, Structure dsd) throws SdmxException {
        return connector.getTimeSeries(
                Connectors.fromFlowQuery(ref.getFlowRef(), dsd.getRef()),
                Connectors.fromStructure(dsd),
                ref.getQuery().getKey().toString(),
                null,
                null,
                ref.getQuery().getDetail().equals(Detail.SERIES_KEYS_ONLY),
                null,
                false);
    }

    private static IOException wrap(SdmxException ex, String format, Object... args) {
        return new IOException(String.format(Locale.ROOT, format, args), ex);
    }

    static Provider newProvider(
            URI endpoint, Provider.AuthenticationMethods auth, boolean needsUrlEncoding, boolean supportsCompression)
            throws SdmxException {
        return newProvider(endpoint, auth, needsUrlEncoding, supportsCompression, SDMXVersion.V2);
    }

    static Provider newProvider(
            URI endpoint,
            Provider.AuthenticationMethods auth,
            boolean needsUrlEncoding,
            boolean supportsCompression,
            SDMXVersion version)
            throws SdmxException {
        return new Provider("", endpoint, auth, needsUrlEncoding, supportsCompression, false, "", version);
    }

    static {
        ConnectorsConfigFix.fixConfiguration();
    }
}
