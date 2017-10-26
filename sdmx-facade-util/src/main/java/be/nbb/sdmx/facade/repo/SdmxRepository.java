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

import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.util.SeriesSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    Set<Dataflow> dataflows;

    @lombok.NonNull
    Map<DataflowRef, List<Series>> data;

    @lombok.Builder.Default
    boolean seriesKeysOnlySupported = true;

    @Nonnull
    public SdmxConnection asConnection() {
        return new RepoConnection(this);
    }

    @Nullable
    public DataCursor getCursor(@Nonnull DataflowRef flowRef, @Nonnull DataQuery query) {
        Objects.requireNonNull(flowRef);
        Objects.requireNonNull(query);
        List<Series> result = data.get(flowRef);
        return result != null ? SeriesSupport.asCursor(result, query.getKey()) : null;
    }

    @Nullable
    public Stream<Series> getStream(@Nonnull DataflowRef flowRef, @Nonnull DataQuery query) {
        Objects.requireNonNull(flowRef);
        Objects.requireNonNull(query);
        List<Series> result = data.get(flowRef);
        return result != null ? result.stream().filter(o -> query.getKey().contains(o.getKey())) : null;
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
            return data(flowRef, SeriesSupport.copyOf(cursor));
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class RepoConnection implements SdmxConnection {

        private final SdmxRepository repo;
        private boolean closed;

        private RepoConnection(SdmxRepository repo) {
            this.repo = repo;
            this.closed = false;
        }

        @Override
        public Set<Dataflow> getDataflows() throws IOException {
            checkState();
            return repo.getDataflows();
        }

        @Override
        public Dataflow getDataflow(DataflowRef flowRef) throws IOException {
            checkState();
            Objects.requireNonNull(flowRef);
            return repo.getDataflows()
                    .stream()
                    .filter(o -> flowRef.equals(o.getFlowRef()))
                    .findFirst()
                    .orElseThrow(() -> new IOException("Dataflow not found"));
        }

        @Override
        public DataStructure getDataStructure(DataflowRef flowRef) throws IOException {
            Dataflow flow = getDataflow(flowRef);
            return repo.getDataStructures()
                    .stream()
                    .filter(o -> flow.getDataStructureRef().equals(o.getRef()))
                    .findFirst()
                    .orElseThrow(() -> new IOException("DataStructure not found"));
        }

        @Override
        public DataCursor getDataCursor(DataflowRef flowRef, DataQuery query) throws IOException {
            checkState();
            DataCursor result = repo.getCursor(flowRef, query);
            if (result != null) {
                return result;
            }
            throw new IOException("Data not found");
        }

        @Override
        public Stream<Series> getDataStream(DataflowRef flowRef, DataQuery query) throws IOException {
            checkState();
            Stream<Series> result = repo.getStream(flowRef, query);
            if (result != null) {
                return result;
            }
            throw new IOException("Data not found");
        }

        @Override
        public boolean isSeriesKeysOnlySupported() {
            return repo.isSeriesKeysOnlySupported();
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
    //</editor-fold>}
}
