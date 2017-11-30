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
import internal.web.RestClient;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Philippe Charles
 */
public enum NullRestClient implements RestClient {

    INSTANCE;

    @Override
    public List<Dataflow> getFlows() throws IOException {
        return null;
    }

    @Override
    public Dataflow getFlow(DataflowRef ref) throws IOException {
        Objects.requireNonNull(ref);
        return null;
    }

    @Override
    public DataStructure getStructure(DataStructureRef ref) throws IOException {
        Objects.requireNonNull(ref);
        return null;
    }

    @Override
    public DataCursor getData(DataflowRef flowRef, DataQuery query, DataStructure dsd) throws IOException {
        Objects.requireNonNull(flowRef);
        Objects.requireNonNull(dsd);
        Objects.requireNonNull(query);
        return null;
    }

    @Override
    public boolean isSeriesKeysOnlySupported() throws IOException {
        return false;
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
        Objects.requireNonNull(flowRef);
        return null;
    }
}
