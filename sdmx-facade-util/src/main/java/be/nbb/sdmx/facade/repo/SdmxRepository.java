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
import be.nbb.sdmx.facade.DataFilter;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.util.SdmxExceptions;
import be.nbb.sdmx.facade.util.SeriesSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    List<DataStructure> structures;

    @lombok.NonNull
    @lombok.Singular
    List<Dataflow> flows;

    @lombok.NonNull
    Map<DataflowRef, List<Series>> data;

    @lombok.Builder.Default
    boolean seriesKeysOnlySupported = true;

    @Nonnull
    public SdmxConnection asConnection() {
        return new RepoConnection(this);
    }

    @Nonnull
    public Optional<DataStructure> getStructure(@Nonnull DataStructureRef ref) {
        Objects.requireNonNull(ref);
        return structures
                .stream()
                .filter(ref::equalsRef)
                .findFirst();
    }

    @Nonnull
    public Optional<Dataflow> getFlow(@Nonnull DataflowRef ref) {
        Objects.requireNonNull(ref);
        return flows
                .stream()
                .filter(ref::containsRef)
                .findFirst();
    }

    @Nonnull
    public Optional<List<Series>> getData(@Nonnull DataflowRef flowRef, @Nonnull Key key, @Nonnull DataFilter filter) {
        return getDataByFlowRef(flowRef).map(toStream(key, filter)).map(o -> o.collect(Collectors.toList()));
    }

    @Nonnull
    public Optional<Stream<Series>> getDataStream(@Nonnull DataflowRef flowRef, @Nonnull Key key, @Nonnull DataFilter filter) {
        return getDataByFlowRef(flowRef).map(toStream(key, filter));
    }

    @Nonnull
    public Optional<DataCursor> getDataCursor(@Nonnull DataflowRef flowRef, @Nonnull Key key, @Nonnull DataFilter filter) {
        return getDataByFlowRef(flowRef).map(toCursor(key, filter));
    }

    @Nonnull
    private Optional<List<Series>> getDataByFlowRef(@Nonnull DataflowRef flowRef) {
        Objects.requireNonNull(flowRef);
        return Optional.ofNullable(data.get(flowRef));
    }

    @Nonnull
    private static Function<List<Series>, DataCursor> toCursor(@Nonnull Key key, @Nonnull DataFilter filter) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);
        return o -> SeriesSupport.asCursor(o, key);
    }

    @Nonnull
    private static Function<List<Series>, Stream<Series>> toStream(@Nonnull Key key, @Nonnull DataFilter filter) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);
        return o -> o.stream().filter(s -> key.contains(s.getKey()));
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
        public Collection<Dataflow> getFlows() throws IOException {
            checkState();
            return repo.getFlows();
        }

        @Override
        public Dataflow getFlow(DataflowRef flowRef) throws IOException {
            checkState();
            return repo
                    .getFlow(flowRef)
                    .orElseThrow(() -> SdmxExceptions.missingFlow(repo.getName(), flowRef));
        }

        @Override
        public DataStructure getStructure(DataflowRef flowRef) throws IOException {
            checkState();
            DataStructureRef structRef = getFlow(flowRef).getStructureRef();
            return repo
                    .getStructure(structRef)
                    .orElseThrow(() -> SdmxExceptions.missingStructure(repo.getName(), structRef));
        }

        @Override
        public List<Series> getData(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
            checkState();
            return repo
                    .getData(flowRef, key, filter)
                    .orElseThrow(() -> SdmxExceptions.missingData(repo.getName(), flowRef));
        }

        @Override
        public Stream<Series> getDataStream(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
            checkState();
            return repo
                    .getDataStream(flowRef, key, filter)
                    .orElseThrow(() -> SdmxExceptions.missingData(repo.getName(), flowRef));
        }

        @Override
        public DataCursor getDataCursor(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
            checkState();
            return repo
                    .getDataCursor(flowRef, key, filter)
                    .orElseThrow(() -> SdmxExceptions.missingData(repo.getName(), flowRef));
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
                throw SdmxExceptions.connectionClosed(repo.getName());
            }
        }
    }
    //</editor-fold>}
}
