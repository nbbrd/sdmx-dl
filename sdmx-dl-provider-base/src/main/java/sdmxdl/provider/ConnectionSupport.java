package sdmxdl.provider;

import lombok.NonNull;
import nbbrd.design.NonNegative;
import sdmxdl.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@lombok.experimental.UtilityClass
public class ConnectionSupport {

    public static @NonNull Flow getFlowFromFlows(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Connection connection, @NonNull HasMarker source) throws IOException, IllegalArgumentException {
        return connection
                .getFlows(database)
                .stream()
                .filter(flowRef::containsRef)
                .findFirst()
                .orElseThrow(() -> CommonSdmxExceptions.missingFlow(source, flowRef));
    }

    public static @NonNull DataSet getDataSetFromStream(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query, @NonNull Connection connection) throws IOException, IllegalArgumentException {
        try (Stream<Series> stream = connection.getDataStream(database, flowRef, query)) {
            return stream.collect(DataSet.toDataSet(flowRef, query));
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    public static @NonNull Collection<String> getAvailableDimensionCodes(@NonNull Connection connection, @NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Key constraints, @NonNegative int dimensionIndex) throws IOException, IllegalArgumentException {
        Structure dsd = connection.getMeta(database, flowRef).getStructure();
        int size = dsd.getDimensions().size();

        if (dimensionIndex < 0 || dimensionIndex >= size) {
            throw new IllegalArgumentException("Expecting dimensionIndex in range [0," + size + "[");
        }
        if (!constraints.equals(Key.ALL) && !constraints.isWildcard(dimensionIndex)) {
            throw new IllegalArgumentException("Expecting wildcard on dimensionIndex");
        }

        if (connection.getSupportedFeatures().contains(Feature.DATA_QUERY_DETAIL)) {
            try (Stream<Series> stream = connection.getDataStream(database, flowRef, Query.builder().key(constraints).detail(Detail.SERIES_KEYS_ONLY).build())) {
                return stream
                        .map(series -> series.getKey().get(dimensionIndex))
                        .distinct()
                        .collect(toList());
            }
        } else {
            return dsd
                    .getDimensions()
                    .get(dimensionIndex)
                    .getCodes()
                    .keySet();
        }
    }
}
