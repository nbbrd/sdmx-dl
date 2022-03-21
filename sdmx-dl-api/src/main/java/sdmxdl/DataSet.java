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

import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.EqualsAndHashCode(callSuper = false)
public class DataSet extends Resource<DataflowRef> {

    @lombok.NonNull
    DataflowRef ref;

    @lombok.NonNull
    @lombok.Builder.Default
    DataQuery query = DataQuery.ALL;

    @lombok.NonNull
    @lombok.Singular("series")
    Collection<Series> data;

    public @NonNull DataSet getData(@NonNull DataQuery query) {
        return query.equals(DataQuery.ALL) ? this : query.execute(data.stream()).collect(toDataSet(ref, query));
    }

    public @NonNull Stream<Series> getDataStream(@NonNull DataQuery query) {
        return query.equals(DataQuery.ALL) ? data.stream() : query.execute(data.stream());
    }

    public static @NonNull Collector<Series, ?, DataSet> toDataSet(@NonNull DataflowRef flowRef, @NonNull DataQuery query) {
        return collectingAndThen(toList(), newDataSet(flowRef, query));
    }

    private static Function<List<Series>, DataSet> newDataSet(DataflowRef flowRef, DataQuery query) {
        return list -> new DataSet(flowRef, query, unmodifiableList(list));
    }
}
