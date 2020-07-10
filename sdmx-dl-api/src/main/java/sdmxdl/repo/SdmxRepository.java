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
package sdmxdl.repo;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.SdmxExceptions;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
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

    boolean seriesKeysOnlySupported;

    // Fix lombok.Builder.Default bug in NetBeans
    public static Builder builder() {
        return new Builder()
                .name("")
                .seriesKeysOnlySupported(true);
    }

    @NonNull
    public SdmxConnection asConnection() {
        return new RepoConnection(this);
    }

    @NonNull
    public Optional<DataStructure> getStructure(@NonNull DataStructureRef ref) {
        Objects.requireNonNull(ref);
        return structures
                .stream()
                .filter(ref::equalsRef)
                .findFirst();
    }

    @NonNull
    public Optional<Dataflow> getFlow(@NonNull DataflowRef ref) {
        Objects.requireNonNull(ref);
        return flows
                .stream()
                .filter(ref::containsRef)
                .findFirst();
    }

    @NonNull
    public Optional<List<Series>> getData(@NonNull DataflowRef flowRef, @NonNull Key key, @NonNull DataFilter filter) {
        return getDataByFlowRef(flowRef).map(toStream(key, filter)).map(o -> o.collect(Collectors.toList()));
    }

    @NonNull
    public Optional<Stream<Series>> getDataStream(@NonNull DataflowRef flowRef, @NonNull Key key, @NonNull DataFilter filter) {
        return getDataByFlowRef(flowRef).map(toStream(key, filter));
    }

    @NonNull
    public Optional<DataCursor> getDataCursor(@NonNull DataflowRef flowRef, @NonNull Key key, @NonNull DataFilter filter) {
        return getDataByFlowRef(flowRef).map(toCursor(key, filter));
    }

    @NonNull
    private Optional<List<Series>> getDataByFlowRef(@NonNull DataflowRef flowRef) {
        Objects.requireNonNull(flowRef);
        return Optional.ofNullable(data.get(flowRef));
    }

    @NonNull
    private static Function<List<Series>, DataCursor> toCursor(@NonNull Key key, @NonNull DataFilter filter) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);
        return o -> DataCursor.of(o, key);
    }

    @NonNull
    private static Function<List<Series>, Stream<Series>> toStream(@NonNull Key key, @NonNull DataFilter filter) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);
        return o -> o.stream().filter(s -> key.contains(s.getKey()));
    }

    public static final class Builder {

        private final Map<DataflowRef, List<Series>> data = new HashMap<>();

        @NonNull
        public Builder clearData() {
            this.data.clear();
            return this;
        }

        @NonNull
        public Builder data(@NonNull DataflowRef flowRef, @NonNull Series series) {
            data.computeIfAbsent(flowRef, o -> new ArrayList<>()).add(series);
            return this;
        }

        @NonNull
        public Builder data(@NonNull DataflowRef flowRef, @NonNull List<Series> list) {
            data.computeIfAbsent(flowRef, o -> new ArrayList<>()).addAll(list);
            return this;
        }

        @NonNull
        public Builder copyOf(@NonNull DataflowRef flowRef, @NonNull DataCursor cursor) throws IOException {
            return data(flowRef, cursor.toStream(DataFilter.Detail.FULL).collect(Collectors.toList()));
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
