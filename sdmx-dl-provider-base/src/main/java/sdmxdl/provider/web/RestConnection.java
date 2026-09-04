/*
 * Copyright 2015 National Bank of Belgium
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
package sdmxdl.provider.web;

import static sdmxdl.Feature.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;
import lombok.NonNull;
import nbbrd.design.NonNegative;
import nbbrd.design.VisibleForTesting;
import sdmxdl.*;
import sdmxdl.provider.CommonSdmxExceptions;
import sdmxdl.provider.ConnectionSupport;
import sdmxdl.provider.DataRef;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
final class RestConnection implements Connection {

    @lombok.NonNull private final RestClient client;

    private boolean closed = false;

    @Override
    public @NonNull Collection<Database> getDatabases() throws IOException {
        checkState();
        return Collections.emptyList();
    }

    @Override
    public @NonNull Collection<Flow> getFlows(@NonNull DatabaseRef database) throws IOException {
        checkState();
        checkDatabase(database);
        return client.getFlows();
    }

    @Override
    public @NonNull MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef)
            throws IOException, IllegalArgumentException {
        checkState();
        checkDatabase(database);
        Flow flow = lookupFlow(database, flowRef);
        return MetaSet.builder()
                .flow(flow)
                .structure(client.getStructure(flow.getStructureRef()))
                .build();
    }

    @Override
    public @NonNull DataSet getData(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query)
            throws IOException {
        checkDatabase(database);
        return ConnectionSupport.getDataSetFromStream(database, flowRef, query, this);
    }

    @Override
    public @NonNull Stream<Series> getDataStream(
            @NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException {
        MetaSet meta = getMeta(database, flowRef);

        checkKey(query.getKey(), meta.getStructure());

        Query normalizedQuery = query.toBuilder()
                .key(query.getKey().normalize(meta.getStructure()))
                .build();

        Query realQuery = deriveQuery(normalizedQuery, getSupportedFeatures(), meta.getStructure());

        Stream<Series> result = client.getData(DataRef.of(meta.getFlow().getRef(), realQuery), meta.getStructure());

        // Observation-level filters (period range, first/last N) are always re-applied client-side
        // because a data source may not support them (or may support them only partially).
        return realQuery.equals(normalizedQuery) && !normalizedQuery.hasObsLevelFilter()
                ? result
                : normalizedQuery.execute(result);
    }

    @Override
    public @NonNull Collection<String> getAvailableDimensionCodes(
            @NonNull DatabaseRef database,
            @NonNull FlowRef flowRef,
            @NonNull Key constraints,
            @NonNegative int dimensionIndex)
            throws IOException, IllegalArgumentException {
        return ConnectionSupport.getAvailableDimensionCodes(this, database, flowRef, constraints, dimensionIndex);
    }

    @VisibleForTesting
    static Query deriveQuery(Query query, Set<Feature> features, Structure dsd) {
        return Query.builder()
                .key(fixTrailingWildcards(
                        features.contains(DATA_QUERY_ALL_KEYWORD) || !Key.ALL.equals(query.getKey())
                                ? query.getKey()
                                : alternateAllOf(dsd),
                        dsd))
                .detail(features.contains(DATA_QUERY_DETAIL) ? query.getDetail() : Detail.FULL)
                .startPeriod(features.contains(DATA_QUERY_TIME_RANGE) ? query.getStartPeriod() : null)
                .endPeriod(features.contains(DATA_QUERY_TIME_RANGE) ? query.getEndPeriod() : null)
                .firstNObservations(features.contains(DATA_QUERY_OBS_COUNT) ? query.getFirstNObservations() : null)
                .lastNObservations(features.contains(DATA_QUERY_OBS_COUNT) ? query.getLastNObservations() : null)
                .build();
    }

    private static Key alternateAllOf(Structure dsd) {
        return Key.of(new String[dsd.getDimensions().size()]);
    }

    /**
     * Rewrites keys ending with two trailing wildcards to avoid generating positional keys
     * with a trailing ".." segment in REST URLs.
     * <p>
     * When the last two dimensions are wildcards and the key has at least 3 dimensions,
     * one of these wildcards is expanded to an explicit '+'-joined list of all codes.
     * The dimension with fewer codes is chosen to keep the expanded key as short as possible.
     * In case of a tie, the last dimension is expanded to preserve previous behavior.
     * <p>
     * If one trailing dimension has no codes (non-enumerated), the other one is expanded.
     * If both trailing dimensions have no codes, no rewrite is possible and the key is returned unchanged.
     */
    private static Key fixTrailingWildcards(Key key, Structure dsd) {
        int size = key.size();
        if (size > 2 && key.isWildcard(size - 1) && key.isWildcard(size - 2)) {
            int last = size - 1;
            int previous = size - 2;
            List<Dimension> dimensions = dsd.getDimensions();
            Set<String> previousCodes = dimensions.get(previous).getCodes().keySet();
            Set<String> lastCodes = dimensions.get(last).getCodes().keySet();

            if (previousCodes.isEmpty() && lastCodes.isEmpty()) {
                return key;
            } else if (previousCodes.isEmpty()) {
                return key.with(lastCodes, last);
            } else if (lastCodes.isEmpty()) {
                return key.with(previousCodes, previous);
            } else if (previousCodes.size() < lastCodes.size()) {
                return key.with(previousCodes, previous);
            } else {
                return key.with(lastCodes, last);
            }
        }
        return key;
    }

    @Override
    public @NonNull Set<Feature> getSupportedFeatures() throws IOException {
        return client.getSupportedFeatures();
    }

    @Override
    public @NonNull Optional<URI> testConnection() throws IOException {
        checkState();
        return client.testClient();
    }

    @Override
    public void close() {
        closed = true;
    }

    private void checkState() throws IOException {
        if (closed) {
            throw CommonSdmxExceptions.connectionClosed(client);
        }
    }

    private Flow lookupFlow(DatabaseRef database, FlowRef flowRef) throws IOException, IllegalArgumentException {
        return ConnectionSupport.getFlowFromFlows(database, flowRef, this, client);
    }

    private void checkKey(Key key, Structure dsd) throws IllegalArgumentException {
        WebValidators.onDataStructure(dsd).checkValidity(key);
    }

    private void checkDatabase(DatabaseRef database) throws IOException {
        if (!database.equals(DatabaseRef.NO_DATABASE)) {
            throw new IOException("Database reference is not supported");
        }
    }
}
