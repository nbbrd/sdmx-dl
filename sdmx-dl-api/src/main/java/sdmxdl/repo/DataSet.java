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

import internal.sdmxdl.FilteredCursor;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    Key key;

    @lombok.NonNull
    @lombok.Singular("series")
    Collection<Series> data;

    public static Builder builder() {
        return new Builder().key(Key.ALL);
    }

    @NonNull
    public Collection<Series> getData(@NonNull Key key, @NonNull DataFilter filter) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);
        return FilteredCursor.isNoFilter(key, filter) ? data : DataCursor.of(data.iterator()).filter(key, filter).toStream().collect(Collectors.toList());
    }

    @NonNull
    public Stream<Series> getDataStream() {
        return data.stream();
    }

    @NonNull
    public Stream<Series> getDataStream(@NonNull Key key, @NonNull DataFilter filter) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);
        return FilteredCursor.isNoFilter(key, filter) ? data.stream() : DataCursor.of(data.iterator()).filter(key, filter).toStream();
    }

    @NonNull
    public DataCursor getDataCursor() {
        return DataCursor.of(data.iterator());
    }

    @NonNull
    public DataCursor getDataCursor(@NonNull Key key, @NonNull DataFilter filter) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);
        return DataCursor.of(data.iterator()).filter(key, filter);
    }

    public static class Builder {

        @NonNull
        public Builder copyOf(@NonNull DataCursor cursor) throws IOException {
            try {
                cursor.toStream().forEach(this::series);
            } catch (UncheckedIOException ex) {
                throw ex.getCause();
            }
            return this;
        }
    }
}
