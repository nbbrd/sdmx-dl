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
package sdmxdl.util;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class used by JDemetra+ plugin.
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SdmxCubeUtil {

    public @NonNull Stream<Series> getAllSeries(@NonNull Connection conn, @NonNull DataflowRef flow, @NonNull Key node) throws IOException, IllegalArgumentException {
        if (node.isSeries()) {
            throw new IllegalArgumentException("Expecting node");
        }
        return conn.getSupportedFeatures().contains(Feature.DATA_QUERY_DETAIL)
                ? request(conn, flow, node).map(series -> series.toBuilder().freq(Frequency.UNDEFINED).build())
                : computeKeys(conn, flow, node);
    }

    public @NonNull Stream<Series> getAllSeriesWithData(@NonNull Connection conn, @NonNull DataflowRef flow, @NonNull Key node) throws IOException, IllegalArgumentException {
        if (node.isSeries()) {
            throw new IllegalArgumentException("Expecting node");
        }
        return conn.getSupportedFeatures().contains(Feature.DATA_QUERY_DETAIL)
                ? requestWithData(conn, flow, node)
                : computeKeysAndRequestData(conn, flow, node);
    }

    public @NonNull Optional<Series> getSeries(@NonNull Connection conn, @NonNull DataflowRef flow, @NonNull Key leaf) throws IOException, IllegalArgumentException {
        if (!leaf.isSeries()) {
            throw new IllegalArgumentException("Expecting leaf");
        }
        try (Stream<Series> stream = request(conn, flow, leaf)) {
            return stream.findFirst().map(series -> series.toBuilder().freq(Frequency.UNDEFINED).build());
        }
    }

    public @NonNull Optional<Series> getSeriesWithData(@NonNull Connection conn, @NonNull DataflowRef flow, @NonNull Key leaf) throws IOException, IllegalArgumentException {
        if (!leaf.isSeries()) {
            throw new IllegalArgumentException("Expecting leaf");
        }
        try (Stream<Series> stream = requestWithData(conn, flow, leaf)) {
            return stream.findFirst();
        }
    }

    public @NonNull Stream<String> getChildren(@NonNull Connection conn, @NonNull DataflowRef flow, @NonNull Key node, @NonNegative int dimensionIndex) throws IOException {
        if (dimensionIndex < 0) {
            throw new IllegalArgumentException("Expecting dimensionIndex >= 0");
        }
        if (node.isSeries()) {
            throw new IllegalArgumentException("Expecting node");
        }
        if (!node.equals(Key.ALL) && !node.isWildcard(dimensionIndex)) {
            throw new IllegalArgumentException("Expecting wildcard on dimensionIndex");
        }
        return conn.getSupportedFeatures().contains(Feature.DATA_QUERY_DETAIL)
                ? request(conn, flow, node).map(series -> series.getKey().get(dimensionIndex)).distinct()
                : computeAllPossibleChildren(conn.getStructure(flow).getDimensionList(), dimensionIndex);
    }


    public @NonNull Optional<Dimension> getDimensionById(@NonNull DataStructure dsd, @NonNull String id) {
        Objects.requireNonNull(dsd);
        Objects.requireNonNull(id);
        return dsd.getDimensions().stream().filter(dimension -> dimension.getId().equals(id)).findFirst();
    }

    public @NonNull OptionalInt getDimensionIndexById(@NonNull DataStructure dsd, @NonNull String id) {
        Objects.requireNonNull(dsd);
        Objects.requireNonNull(id);
        List<Dimension> dimensionList = dsd.getDimensionList();
        for (int i = 0; i < dimensionList.size(); i++) {
            if (dimensionList.get(i).getId().equals(id)) {
                return OptionalInt.of(i);
            }
        }
        return OptionalInt.empty();
    }

    private Stream<Series> request(Connection conn, DataflowRef flow, Key key) throws IOException {
        return conn.getDataStream(flow, DataQuery.of(key, DataDetail.NO_DATA));
    }

    private Stream<Series> requestWithData(Connection conn, DataflowRef flow, Key key) throws IOException {
        return conn.getDataStream(flow, DataQuery.of(key, DataDetail.FULL));
    }

    private Stream<Series> computeKeys(Connection conn, DataflowRef flow, Key key) throws IOException {
        return computeAllPossibleSeries(conn.getStructure(flow), key)
                .map(SdmxCubeUtil::emptySeriesOf);
    }

    private Stream<Series> computeKeysAndRequestData(Connection conn, DataflowRef flow, Key key) throws IOException {
        Map<Key, Series> dataByKey = dataByKey(conn, flow, key);
        return computeAllPossibleSeries(conn.getStructure(flow), key)
                .map(seriesKey -> dataByKey.computeIfAbsent(seriesKey, SdmxCubeUtil::emptySeriesOf));
    }

    private Map<Key, Series> dataByKey(Connection conn, DataflowRef flow, Key key) throws IOException {
        try (Stream<Series> cursor = requestWithData(conn, flow, key)) {
            return cursor.collect(Collectors.toMap(Series::getKey, Function.identity()));
        }
    }

    private Stream<Key> computeAllPossibleSeries(DataStructure dsd, Key ref) {
        return computeAllPossibleSeries(dsd.getDimensionList(), ref);
    }

    private Stream<Key> computeAllPossibleSeries(List<Dimension> dimensions, Key ref) {
        List<Key> result = new ArrayList<>();
        String[] stack = new String[dimensions.size()];
        computeAllPossibleSeries(index -> getCodeList(dimensions, ref, index), 0, stack, result);
        return result.stream();
    }

    private Set<String> getCodeList(List<Dimension> dimensions, Key ref, int dimensionIndex) {
        return Key.ALL.equals(ref) || ref.isWildcard(dimensionIndex)
                ? dimensions.get(dimensionIndex).getCodes().keySet()
                : Collections.singleton(ref.get(dimensionIndex));
    }

    private void computeAllPossibleSeries(IntFunction<Set<String>> codeLists, int idx, String[] stack, List<Key> result) {
        codeLists.apply(idx).forEach(code -> {
            stack[idx] = code;
            if (idx == stack.length - 1) {
                result.add(Key.of(stack));
            } else {
                computeAllPossibleSeries(codeLists, idx + 1, stack, result);
            }
        });
    }

    private Stream<String> computeAllPossibleChildren(List<Dimension> dimensions, int dimensionIndex) {
        return dimensions.get(dimensionIndex).getCodes().keySet().stream().sorted();
    }

    private Series emptySeriesOf(Key key) {
        return Series.builder().key(key).freq(Frequency.UNDEFINED).build();
    }
}
