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
package _test.sdmxdl.util;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.SdmxException;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.util.web.SdmxRestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class XRepoRestClient implements SdmxRestClient {

    @lombok.NonNull
    private final SdmxRepository repo;

    @Override
    public String getName() {
        return repo.getName();
    }

    @Override
    public List<Dataflow> getFlows() {
        return repo.getFlows();
    }

    @Override
    public Dataflow getFlow(DataflowRef ref) throws IOException {
        return repo.getFlow(ref)
                .orElseThrow(() -> SdmxException.missingFlow(repo.getName(), ref));
    }

    @Override
    public DataStructure getStructure(DataStructureRef ref) throws IOException {
        return repo.getStructure(ref)
                .orElseThrow(() -> SdmxException.missingStructure(repo.getName(), ref));
    }

    @Override
    public Stream<Series> getData(DataRef ref, DataStructure dsd) throws IOException {
        Objects.requireNonNull(ref);
        Objects.requireNonNull(dsd);
        return repo.getDataSet(ref.getFlowRef())
                .map(dataSet -> dataSet.getDataStream(ref.getKey(), ref.getFilter()))
                .orElseThrow(() -> SdmxException.missingData(repo.getName(), ref));
    }

    @Override
    public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException {
        Objects.requireNonNull(ref);
        return repo.getStructures().stream()
                .flatMap(dsd -> dsd.getDimensions().stream())
                .map(Component::getCodelist)
                .filter(ref::containsRef)
                .findFirst()
                .orElseThrow(() -> SdmxException.missingCodelist(repo.getName(), ref));
    }

    @Override
    public boolean isDetailSupported() {
        return true;
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) {
        Objects.requireNonNull(flowRef);
        return null;
    }

    @Override
    public Duration ping() {
        return Duration.ZERO;
    }
}
