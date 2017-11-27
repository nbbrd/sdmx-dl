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
package test.client;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.repo.SdmxRepository;
import internal.web.RestClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class RepoRestClient implements RestClient {

    @lombok.NonNull
    private final SdmxRepository repo;

    @Override
    public List<Dataflow> getFlows() throws IOException {
        return new ArrayList(repo.getDataflows());
    }

    @Override
    public Dataflow getFlow(DataflowRef ref) throws IOException {
        return repo.getFlow(ref)
                .orElseThrow(() -> new IOException("Dataflow not found"));
    }

    @Override
    public DataStructure getStructure(DataStructureRef ref) throws IOException {
        return repo.getStructure(ref)
                .orElseThrow(() -> new IOException("DataStructure not found"));
    }

    @Override
    public DataCursor getData(DataflowRef flowRef, DataStructure dsd, DataQuery query) throws IOException {
        return repo.getCursor(flowRef, query)
                .orElseThrow(() -> new IOException("Data not found"));
    }

    @Override
    public boolean isSeriesKeysOnlySupported() throws IOException {
        return true;
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
        return null;
    }
}
