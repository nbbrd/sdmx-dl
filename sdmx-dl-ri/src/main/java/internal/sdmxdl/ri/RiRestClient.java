/*
 * Copyright 2018 National Bank of Belgium
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
package internal.sdmxdl.ri;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.util.web.DataRequest;
import sdmxdl.util.web.SdmxWebClient;

import java.io.IOException;
import java.net.URL;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * @author Philippe Charles
 */
public abstract class RiRestClient implements SdmxWebClient {

    @Override
    final public List<Dataflow> getFlows() throws IOException {
        URL url = getFlowsQuery();
        return getFlows(url);
    }

    @Override
    final public Dataflow getFlow(DataflowRef ref) throws IOException {
        URL url = getFlowsQuery();
        return getFlow(url, ref);
    }

    @Override
    final public DataStructure getStructure(DataStructureRef ref) throws IOException {
        URL url = getStructureQuery(ref);
        return getStructure(url, ref);
    }

    @Override
    final public DataCursor getData(DataRequest request, DataStructure dsd) throws IOException {
        URL url = getDataQuery(request);
        return getData(dsd, url);
    }

    @Override
    final public Duration ping() throws IOException {
        Clock clock = Clock.systemDefaultZone();
        Instant start = clock.instant();
        getFlows();
        return Duration.between(start, clock.instant());
    }

    @NonNull
    abstract protected URL getFlowsQuery() throws IOException;

    @NonNull
    abstract protected List<Dataflow> getFlows(@NonNull URL url) throws IOException;

    @NonNull
    abstract protected URL getFlowQuery(@NonNull DataflowRef ref) throws IOException;

    @NonNull
    abstract protected Dataflow getFlow(@NonNull URL url, @NonNull DataflowRef ref) throws IOException;

    @NonNull
    abstract protected URL getStructureQuery(@NonNull DataStructureRef ref) throws IOException;

    @NonNull
    abstract protected DataStructure getStructure(@NonNull URL url, @NonNull DataStructureRef ref) throws IOException;

    @NonNull
    abstract protected URL getDataQuery(@NonNull DataRequest request) throws IOException;

    @NonNull
    abstract protected DataCursor getData(@NonNull DataStructure dsd, @NonNull URL url) throws IOException;
}
