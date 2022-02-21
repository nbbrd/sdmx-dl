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
import sdmxdl.ext.SdmxException;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class SdmxRepository {

    @lombok.NonNull
    @lombok.Builder.Default
    String name = "";

    @lombok.NonNull
    @lombok.Singular
    List<DataStructure> structures;

    @lombok.NonNull
    @lombok.Singular
    List<Dataflow> flows;

    @lombok.NonNull
    @lombok.Singular
    List<DataSet> dataSets;

    @lombok.NonNull
    @lombok.Singular
    Set<Feature> supportedFeatures;

    @lombok.NonNull
    @lombok.Builder.Default
    Instant creationTime = Instant.EPOCH;

    @lombok.NonNull
    @lombok.Builder.Default
    Instant expirationTime = Instant.MAX;

    public boolean isExpired(@NonNull Clock clock) {
        return !clock.instant().isBefore(expirationTime);
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
    public Optional<DataSet> getDataSet(@NonNull DataflowRef ref) {
        Objects.requireNonNull(ref);
        return dataSets
                .stream()
                .filter(ref::containsRef)
                .findFirst();
    }

    public static final class Builder {

        public Builder ttl(Instant creationTime, Duration ttl) {
            return creationTime(creationTime).expirationTime(creationTime.plus(ttl));
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
        public void testConnection() throws IOException {
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
                    .orElseThrow(() -> SdmxException.missingFlow(repo.getName(), flowRef));
        }

        @Override
        public DataStructure getStructure(DataflowRef flowRef) throws IOException {
            checkState();
            DataStructureRef structRef = getFlow(flowRef).getStructureRef();
            return repo
                    .getStructure(structRef)
                    .orElseThrow(() -> SdmxException.missingStructure(repo.getName(), structRef));
        }

        @Override
        public DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
            checkState();
            return repo
                    .getDataSet(flowRef)
                    .map(dataSet -> dataSet.getData(query))
                    .orElseThrow(() -> SdmxException.missingData(repo.getName(), flowRef));
        }

        @Override
        public Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
            checkState();
            return repo
                    .getDataSet(flowRef)
                    .map(dataSet -> dataSet.getDataStream(query))
                    .orElseThrow(() -> SdmxException.missingData(repo.getName(), flowRef));
        }

        @Override
        public Set<Feature> getSupportedFeatures() {
            return repo.getSupportedFeatures();
        }

        @Override
        public void close() {
            closed = true;
        }

        private void checkState() throws IOException {
            if (closed) {
                throw SdmxException.connectionClosed(repo.getName());
            }
        }
    }
    //</editor-fold>}
}
