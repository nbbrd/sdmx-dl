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
package be.nbb.sdmx.facade.repo;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.SdmxConnection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class SdmxRepository {

    @lombok.NonNull
    String name;

    @lombok.NonNull
    @lombok.Singular
    List<DataStructure> dataStructures;

    @lombok.NonNull
    @lombok.Singular
    List<Dataflow> dataflows;

    @lombok.NonNull
    Map<DataflowRef, List<Series>> data;

    @lombok.Builder.Default
    boolean seriesKeysOnlySupported = true;

    @Nonnull
    public SdmxConnection asConnection() {
        return new RepoConnection(
                toMap(dataStructures, DataStructure::getRef),
                toMap(dataflows, Dataflow::getFlowRef),
                data,
                seriesKeysOnlySupported);
    }

    @Nonnull
    public DataCursor getData(@Nonnull DataflowRef flowRef, @Nonnull DataQuery query) throws IOException {
        Objects.requireNonNull(flowRef);
        Objects.requireNonNull(query);
        List<Series> col = data.get(flowRef);
        if (col != null) {
            return Series.asCursor(col, query.getKey());
        }
        throw new IOException("Data not found");
    }

    public static final class Builder {

        private final Map<DataflowRef, List<Series>> data = new HashMap<>();

        @Nonnull
        public Builder clearData() {
            this.data.clear();
            return this;
        }

        @Nonnull
        public Builder data(@Nonnull DataflowRef flowRef, @Nonnull Series series) {
            data.computeIfAbsent(flowRef, o -> new ArrayList<>()).add(series);
            return this;
        }

        @Nonnull
        public Builder data(@Nonnull DataflowRef flowRef, @Nonnull List<Series> list) {
            data.computeIfAbsent(flowRef, o -> new ArrayList<>()).addAll(list);
            return this;
        }

        @Nonnull
        public Builder copyOf(@Nonnull DataflowRef flowRef, @Nonnull DataCursor cursor) throws IOException {
            return data(flowRef, Series.copyOf(cursor));
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class RepoConnection implements SdmxConnection {

        private final Map<DataStructureRef, DataStructure> dataStructures;
        private final Map<DataflowRef, Dataflow> dataflows;
        private final Map<DataflowRef, List<Series>> data;
        private final boolean seriesKeysOnlySupported;
        private boolean closed;

        private RepoConnection(Map<DataStructureRef, DataStructure> dataStructures, Map<DataflowRef, Dataflow> dataflows, Map<DataflowRef, List<Series>> data, boolean seriesKeysOnlySupported) {
            this.dataStructures = dataStructures;
            this.dataflows = dataflows;
            this.data = data;
            this.seriesKeysOnlySupported = seriesKeysOnlySupported;
            this.closed = false;
        }

        @Override
        public Set<Dataflow> getDataflows() throws IOException {
            checkState();
            return new HashSet<>(dataflows.values());
        }

        @Override
        public Dataflow getDataflow(DataflowRef flowRef) throws IOException {
            checkState();
            Objects.requireNonNull(flowRef);
            Dataflow result = dataflows.get(flowRef);
            if (result != null) {
                return result;
            }
            throw new IOException("Dataflow not found");
        }

        @Override
        public DataStructure getDataStructure(DataflowRef flowRef) throws IOException {
            checkState();
            DataStructure result = dataStructures.get(getDataflow(flowRef).getDataStructureRef());
            if (result != null) {
                return result;
            }
            throw new IOException("DataStructure not found");
        }

        @Override
        public DataCursor getData(DataflowRef flowRef, DataQuery query) throws IOException {
            checkState();
            Objects.requireNonNull(flowRef);
            Objects.requireNonNull(query);
            List<Series> col = data.get(flowRef);
            if (col != null) {
                return Series.asCursor(col, query.getKey());
            }
            throw new IOException("Data not found");
        }

        @Override
        public boolean isSeriesKeysOnlySupported() {
            return seriesKeysOnlySupported;
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }

        private void checkState() throws IOException {
            if (closed) {
                throw new IOException("Connection closed");
            }
        }
    }

    private static <K, V> Map<K, V> toMap(List<V> list, Function<V, K> toKey) {
        return list.stream().collect(Collectors.toMap(toKey, Function.identity()));
    }
    //</editor-fold>
}
