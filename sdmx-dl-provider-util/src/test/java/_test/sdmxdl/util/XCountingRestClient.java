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
import sdmxdl.util.DataRef;
import sdmxdl.util.web.SdmxRestClient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
public final class XCountingRestClient implements SdmxRestClient {

    @lombok.NonNull
    private final SdmxRestClient delegate;

    @lombok.NonNull
    private final AtomicInteger count;

    @Override
    public @NonNull String getName() throws IOException {
        return delegate.getName();
    }

    @Override
    public @NonNull List<Dataflow> getFlows() throws IOException {
        count.incrementAndGet();
        return delegate.getFlows();
    }

    @Override
    public @NonNull Dataflow getFlow(@NonNull DataflowRef ref) throws IOException {
        count.incrementAndGet();
        return delegate.getFlow(ref);
    }

    @Override
    public @NonNull DataStructure getStructure(@NonNull DataStructureRef ref) throws IOException {
        count.incrementAndGet();
        return delegate.getStructure(ref);
    }

    @Override
    public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull DataStructure dsd) throws IOException {
        count.incrementAndGet();
        return delegate.getData(ref, dsd);
    }

    @Override
    public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException {
        count.incrementAndGet();
        return delegate.getCodelist(ref);
    }

    @Override
    public boolean isDetailSupported() throws IOException {
        return delegate.isDetailSupported();
    }

    @Override
    public DataStructureRef peekStructureRef(@NonNull DataflowRef flowRef) throws IOException {
        return delegate.peekStructureRef(flowRef);
    }

    @Override
    public void testClient() throws IOException {
        delegate.testClient();
    }
}
