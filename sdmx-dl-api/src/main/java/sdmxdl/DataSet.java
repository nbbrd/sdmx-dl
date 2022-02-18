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

import nbbrd.design.MightBePromoted;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.EqualsAndHashCode(callSuper = false)
public class DataSet {

    @lombok.NonNull
    DataRef ref;

    @lombok.NonNull
    @lombok.Singular("series")
    Collection<Series> data;

    public @NonNull DataSet getData(@NonNull DataRef other) {
        Objects.requireNonNull(other);
        checkFlowRef(other.getFlowRef());
        return isNoFilter(other) ? this : filter(data.stream(), other).collect(toDataSet(other));
    }

    public @NonNull Stream<Series> getDataStream(@NonNull DataRef other) {
        Objects.requireNonNull(other);
        checkFlowRef(other.getFlowRef());
        return isNoFilter(other) ? data.stream() : filter(data.stream(), other);
    }

    private void checkFlowRef(DataflowRef flowRef) throws IllegalArgumentException {
        Objects.requireNonNull(flowRef);
        if (!ref.getFlowRef().contains(flowRef)) {
            throw new IllegalArgumentException(flowRef.toString());
        }
    }

    public static @NonNull Collector<Series, ?, DataSet> toDataSet(@NonNull DataRef ref) {
        Objects.requireNonNull(ref);
        return Collectors.collectingAndThen(Collectors.toList(), list -> DataSet.builder().ref(ref).data(list).build());
    }

    @MightBePromoted
    private static Stream<Series> filter(Stream<Series> data, DataRef filter) {
        return data
                .filter(filter.getKey()::containsKey)
                .map(filter.getFilter()::apply);
    }

    @MightBePromoted
    private static boolean isNoFilter(@NonNull DataRef ref) {
        return Key.ALL.equals(ref.getKey()) && DataFilter.FULL.equals(ref.getFilter());
    }
}
