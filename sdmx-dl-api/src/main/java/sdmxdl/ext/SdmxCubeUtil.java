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
package sdmxdl.ext;

import lombok.NonNull;
import nbbrd.design.NonNegative;
import sdmxdl.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sdmxdl.Detail.FULL;
import static sdmxdl.Detail.NO_DATA;

/**
 * Utility class used by JDemetra+ plugin.
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SdmxCubeUtil {

    public @NonNull Stream<Series> getAllSeries(@NonNull Connection conn, @NonNull DatabaseRef database, @NonNull FlowRef flow, @NonNull Key node) throws IOException, IllegalArgumentException {
        if (node.isSeries()) {
            throw new IllegalArgumentException("Expecting node");
        }
        return isDataQueryDetailSupported(conn)
                ? request(conn, database, flow, node, NO_DATA)
                : computeKeys(conn, database, flow, node);
    }

    public @NonNull Stream<Series> getAllSeriesWithData(@NonNull Connection conn, @NonNull DatabaseRef database, @NonNull FlowRef flow, @NonNull Key node) throws IOException, IllegalArgumentException {
        if (node.isSeries()) {
            throw new IllegalArgumentException("Expecting node");
        }
        return isDataQueryDetailSupported(conn)
                ? request(conn, database, flow, node, FULL)
                : computeKeysAndRequestData(conn, database, flow, node);
    }

    public @NonNull Optional<Series> getSeries(@NonNull Connection conn, @NonNull DatabaseRef database, @NonNull FlowRef flow, @NonNull Key leaf) throws IOException, IllegalArgumentException {
        if (!leaf.isSeries()) {
            throw new IllegalArgumentException("Expecting leaf");
        }
        try (Stream<Series> stream = request(conn, database, flow, leaf, NO_DATA)) {
            return stream.findFirst();
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    public @NonNull Optional<Series> getSeriesWithData(@NonNull Connection conn, @NonNull DatabaseRef database, @NonNull FlowRef flow, @NonNull Key leaf) throws IOException, IllegalArgumentException {
        if (!leaf.isSeries()) {
            throw new IllegalArgumentException("Expecting leaf");
        }
        try (Stream<Series> stream = request(conn, database, flow, leaf, FULL)) {
            return stream.findFirst();
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    public @NonNull Stream<String> getChildren(@NonNull Connection conn, @NonNull DatabaseRef database, @NonNull FlowRef flow, @NonNull Key node, @NonNegative int dimensionIndex) throws IOException {
        if (dimensionIndex < 0) {
            throw new IllegalArgumentException("Expecting dimensionIndex >= 0");
        }
        if (node.isSeries()) {
            throw new IllegalArgumentException("Expecting node");
        }
        if (!node.equals(Key.ALL) && !node.isWildcard(dimensionIndex)) {
            throw new IllegalArgumentException("Expecting wildcard on dimensionIndex");
        }
        return conn.getAvailableDimensionCodes(database, flow, node, dimensionIndex)
                .stream()
                .sorted();
    }

    public @NonNull Optional<Dimension> getDimensionById(@NonNull Structure dsd, @NonNull String id) {
        return dsd.getDimensions().stream().filter(dimension -> dimension.getId().equals(id)).findFirst();
    }

    public @NonNull OptionalInt getDimensionIndexById(@NonNull Structure dsd, @NonNull String id) {
        List<Dimension> dimensionList = dsd.getDimensionList();
        for (int i = 0; i < dimensionList.size(); i++) {
            if (dimensionList.get(i).getId().equals(id)) {
                return OptionalInt.of(i);
            }
        }
        return OptionalInt.empty();
    }

    private Stream<Series> request(Connection conn, @NonNull DatabaseRef database, FlowRef flow, Key key, Detail detail) throws IOException {
        return conn.getDataStream(database, flow, Query.builder().key(key).detail(detail).build());
    }

    private Stream<Series> computeKeys(Connection conn, @NonNull DatabaseRef database, FlowRef flow, Key key) throws IOException {
        return computeAllPossibleSeries(conn.getStructure(database, flow), key)
                .map(SdmxCubeUtil::emptySeriesOf);
    }

    private Stream<Series> computeKeysAndRequestData(Connection conn, @NonNull DatabaseRef database, FlowRef flow, Key key) throws IOException {
        Map<Key, Series> dataByKey = dataByKey(conn, database, flow, key);
        return computeAllPossibleSeries(conn.getStructure(database, flow), key)
                .map(seriesKey -> dataByKey.computeIfAbsent(seriesKey, SdmxCubeUtil::emptySeriesOf));
    }

    private Map<Key, Series> dataByKey(Connection conn, @NonNull DatabaseRef database, FlowRef flow, Key key) throws IOException {
        try (Stream<Series> cursor = request(conn, database, flow, key, FULL)) {
            return cursor.collect(Collectors.toMap(Series::getKey, Function.identity()));
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    private Stream<Key> computeAllPossibleSeries(Structure dsd, Key ref) {
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

    private Series emptySeriesOf(Key key) {
        return Series.builder().key(key).build();
    }

    private static boolean isDataQueryDetailSupported(Connection conn) throws IOException {
        return conn.getSupportedFeatures().contains(Feature.DATA_QUERY_DETAIL);
    }
}
