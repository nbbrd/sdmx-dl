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

import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.provider.CommonSdmxExceptions;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.web.SdmxRestClient;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class XRepoRestClient implements SdmxRestClient {

    @lombok.NonNull
    private final DataRepository repository;

    @Override
    public @NonNull String getName() {
        return repository.getName();
    }

    @Override
    public @NonNull List<Dataflow> getFlows() {
        return repository.getFlows();
    }

    @Override
    public @NonNull Dataflow getFlow(@NonNull DataflowRef ref) throws IOException {
        return repository.getFlow(ref)
                .orElseThrow(() -> CommonSdmxExceptions.missingFlow(repository.getName(), ref));
    }

    @Override
    public @NonNull DataStructure getStructure(@NonNull DataStructureRef ref) throws IOException {
        return repository.getStructure(ref)
                .orElseThrow(() -> CommonSdmxExceptions.missingStructure(repository.getName(), ref));
    }

    @Override
    public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull DataStructure dsd) throws IOException {
        return repository
                .getDataSet(ref.getFlowRef())
                .map(dataSet -> dataSet.getDataStream(ref.getQuery()))
                .orElseThrow(() -> CommonSdmxExceptions.missingData(repository.getName(), ref.getFlowRef()));
    }

    @Override
    public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException {
        return repository.getStructures().stream()
                .flatMap(dsd -> dsd.getDimensions().stream())
                .map(Component::getCodelist)
                .filter(ref::containsRef)
                .findFirst()
                .orElseThrow(() -> CommonSdmxExceptions.missingCodelist(repository.getName(), ref));
    }

    @Override
    public boolean isDetailSupported() {
        return true;
    }

    @Override
    public DataStructureRef peekStructureRef(@NonNull DataflowRef flowRef) {
        return null;
    }

    @Override
    public void testClient() {
    }
}
