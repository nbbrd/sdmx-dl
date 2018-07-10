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
package _test.client;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataFilter;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import java.io.IOException;
import java.util.List;
import java.time.Duration;
import internal.web.SdmxWebClient;

/**
 *
 * @author Philippe Charles
 */
public enum FailingWebClient implements SdmxWebClient {

    INSTANCE;

    @Override
    public List<Dataflow> getFlows() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Dataflow getFlow(DataflowRef ref) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DataStructure getStructure(DataStructureRef ref) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DataCursor getData(DataflowRef flowRef, Key key, DataFilter filter, DataStructure dsd) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSeriesKeysOnlySupported() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Duration ping() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
