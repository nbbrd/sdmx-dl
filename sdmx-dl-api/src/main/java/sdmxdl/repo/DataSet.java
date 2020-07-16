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
package sdmxdl.repo;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class DataSet implements Resource<DataflowRef> {

    @lombok.NonNull
    DataflowRef ref;

    @lombok.NonNull
    Key key;

    @lombok.NonNull
    @lombok.Singular("series")
    List<Series> data;

    public static Builder builder() {
        return new Builder().key(Key.ALL);
    }

    @NonNull
    public List<Series> getData(@NonNull Key key, @NonNull DataFilter filter) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);
        return isNoFilters(key, filter) ? data : getDataStream(key, filter).collect(Collectors.toList());
    }


    @NonNull
    public Stream<Series> getDataStream(@NonNull Key key, @NonNull DataFilter filter) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);
        return data.stream().filter(key::containsKey);
    }

    @NonNull
    public DataCursor getDataCursor(@NonNull Key key, @NonNull DataFilter filter) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);
        return DataCursor.of(data, key);
    }

    private boolean isNoFilters(Key key, DataFilter filter) {
        return Key.ALL.equals(key) && DataFilter.ALL.equals(filter);
    }

    public static class Builder {

        @NonNull
        public Builder copyOf(@NonNull DataCursor cursor, @NonNull DataFilter filter) throws IOException {
            cursor.toStream(filter.getDetail()).forEach(this::series);
            return this;
        }
    }
}
