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
package sdmxdl;

import lombok.NonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class DataRepository implements HasExpiration {

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
    @lombok.Builder.Default
    Instant creationTime = Instant.EPOCH;

    @lombok.NonNull
    @lombok.Builder.Default
    Instant expirationTime = Instant.MAX;

    @NonNull
    public Optional<DataStructure> getStructure(@NonNull DataStructureRef ref) {
        return structures
                .stream()
                .filter(ref::equalsRef)
                .findFirst();
    }

    @NonNull
    public Optional<Dataflow> getFlow(@NonNull DataflowRef ref) {
        return flows
                .stream()
                .filter(ref::containsRef)
                .findFirst();
    }

    @NonNull
    public Optional<DataSet> getDataSet(@NonNull DataflowRef ref) {
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
}
