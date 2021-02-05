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
import sdmxdl.util.web.DataRequest;
import sdmxdl.util.web.SdmxWebClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
public final class XCallStackWebClient implements SdmxWebClient {

    @lombok.NonNull
    private final SdmxWebClient delegate;

    @lombok.NonNull
    private final AtomicInteger count;

    @Override
    public String getName() throws IOException {
        return delegate.getName();
    }

    @Override
    public List<Dataflow> getFlows() throws IOException {
        count.incrementAndGet();
        return delegate.getFlows();
    }

    @Override
    public Dataflow getFlow(DataflowRef ref) throws IOException {
        count.incrementAndGet();
        return delegate.getFlow(ref);
    }

    @Override
    public DataStructure getStructure(DataStructureRef ref) throws IOException {
        count.incrementAndGet();
        return delegate.getStructure(ref);
    }

    @Override
    public DataCursor getData(DataRequest request, DataStructure dsd) throws IOException {
        count.incrementAndGet();
        return delegate.getData(request, dsd);
    }

    @Override
    public boolean isDetailSupported() throws IOException {
        return delegate.isDetailSupported();
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
        return delegate.peekStructureRef(flowRef);
    }

    @Override
    public Duration ping() throws IOException {
        return delegate.ping();
    }
}
