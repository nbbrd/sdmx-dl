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
import sdmxdl.provider.HasMarker;
import sdmxdl.provider.Marker;
import sdmxdl.provider.web.RestClient;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class XRepoRestClient implements RestClient {

    @lombok.NonNull
    private final DataRepository repository;

    @Override
    public @NonNull Marker getMarker() {
        return HasMarker.of(repository);
    }

    @Override
    public @NonNull List<Dataflow> getFlows() {
        return repository.getFlows();
    }

    @Override
    public @NonNull Dataflow getFlow(@NonNull DataflowRef ref) throws IOException {
        return repository.getFlow(ref)
                .orElseThrow(() -> CommonSdmxExceptions.missingFlow(this, ref));
    }

    @Override
    public @NonNull DataStructure getStructure(@NonNull DataStructureRef ref) throws IOException {
        return repository.getStructure(ref)
                .orElseThrow(() -> CommonSdmxExceptions.missingStructure(this, ref));
    }

    @Override
    public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull DataStructure dsd) throws IOException {
        return repository
                .getDataSet(ref.getFlowRef())
                .map(dataSet -> dataSet.getDataStream(ref.getQuery()))
                .orElseGet(Stream::empty);
    }

    @Override
    public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException {
        return repository.getStructures().stream()
                .flatMap(dsd -> dsd.getDimensions().stream())
                .map(Component::getCodelist)
                .filter(ref::containsRef)
                .findFirst()
                .orElseThrow(() -> CommonSdmxExceptions.missingCodelist(this, ref));
    }

    @Override
    public Set<Feature> getSupportedFeatures() {
        return EnumSet.allOf(Feature.class);
    }

    @Override
    public void testClient() {
    }
}
