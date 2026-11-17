/*
 * Copyright 2020 National Bank of Belgium
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
package sdmxdl;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;
import lombok.NonNull;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.EqualsAndHashCode(callSuper = false)
public class DataSet extends Resource<FlowRef> implements Iterable<Series> {

    @lombok.NonNull FlowRef ref;

    @lombok.NonNull @lombok.Builder.Default
    Query query = Query.ALL;

    @lombok.NonNull @lombok.Singular("series")
    Collection<Series> data;

    /**
     * Number of series before paging (see {@link DataRequest#getSeriesOffset()} and {@link
     * DataRequest#getSeriesLimit()}); defaults to the number of series in {@link #data}.
     */
    @lombok.Builder.Default
    int total = SIZE_OF_DATA;

    DataSet(@NonNull FlowRef ref, @NonNull Query query, @NonNull Collection<Series> data, int total) {
        if (total != SIZE_OF_DATA && total < data.size()) {
            throw new IllegalArgumentException(
                    "Expecting total to be at least " + data.size() + " instead of " + total);
        }
        this.ref = ref;
        this.query = query;
        this.data = data;
        this.total = total == SIZE_OF_DATA ? data.size() : total;
    }

    private static final int SIZE_OF_DATA = -1;

    @Override
    public @NonNull Iterator<Series> iterator() {
        return data.iterator();
    }

    public @NonNull Stream<Series> stream() {
        return data.stream();
    }

    public @NonNull DataSet getData(@NonNull Query query) {
        return query.equals(Query.ALL) ? this : query.execute(data.stream()).collect(toDataSet(ref, query));
    }

    public @NonNull Stream<Series> getDataStream(@NonNull Query query) {
        return query.equals(Query.ALL) ? data.stream() : query.execute(data.stream());
    }

    public static @NonNull Collector<Series, ?, DataSet> toDataSet(@NonNull FlowRef flowRef, @NonNull Query query) {
        return collectingAndThen(toList(), newDataSet(flowRef, query));
    }

    private static Function<List<Series>, DataSet> newDataSet(FlowRef flowRef, Query query) {
        return list -> new DataSet(flowRef, query, unmodifiableList(list), SIZE_OF_DATA);
    }
}
