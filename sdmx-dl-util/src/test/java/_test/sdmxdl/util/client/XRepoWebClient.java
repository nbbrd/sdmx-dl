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
package _test.sdmxdl.util.client;

import sdmxdl.*;
import sdmxdl.ext.SdmxExceptions;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.util.web.DataRequest;
import sdmxdl.util.web.SdmxWebClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class XRepoWebClient implements SdmxWebClient {

    @lombok.NonNull
    private final SdmxRepository repo;

    @Override
    public String getName() throws IOException {
        return repo.getName();
    }

    @Override
    public List<Dataflow> getFlows() throws IOException {
        return repo.getFlows();
    }

    @Override
    public Dataflow getFlow(DataflowRef ref) throws IOException {
        return repo.getFlow(ref)
                .orElseThrow(() -> SdmxExceptions.missingFlow(repo.getName(), ref));
    }

    @Override
    public DataStructure getStructure(DataStructureRef ref) throws IOException {
        return repo.getStructure(ref)
                .orElseThrow(() -> SdmxExceptions.missingStructure(repo.getName(), ref));
    }

    @Override
    public DataCursor getData(DataRequest request, DataStructure dsd) throws IOException {
        return repo.getDataSet(request.getFlowRef())
                .map(dataSet -> dataSet.getDataCursor(request.getKey(), request.getFilter()))
                .orElseThrow(() -> SdmxExceptions.missingData(repo.getName(), request.getFlowRef()));
    }

    @Override
    public boolean isSeriesKeysOnlySupported() throws IOException {
        return true;
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
        return null;
    }

    @Override
    public Duration ping() throws IOException {
        return Duration.ZERO;
    }
}
