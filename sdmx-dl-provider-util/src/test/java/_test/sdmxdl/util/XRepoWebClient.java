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

import sdmxdl.*;
import sdmxdl.ext.SdmxException;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.util.web.DataRequest;
import sdmxdl.util.web.SdmxWebClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class XRepoWebClient implements SdmxWebClient {

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
    public DataCursor getData(DataRequest request, DataStructure dsd) throws IOException {
        Objects.requireNonNull(request);
        Objects.requireNonNull(dsd);
        return repo.getDataSet(request.getFlowRef())
                .map(dataSet -> dataSet.getDataCursor(request.getKey(), request.getFilter()))
                .orElseThrow(() -> SdmxException.missingData(repo.getName(), request.getFlowRef(), request.getKey(), request.getFilter()));
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
