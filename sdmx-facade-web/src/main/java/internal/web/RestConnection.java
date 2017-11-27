/*
 * Copyright 2015 National Bank of Belgium
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
package internal.web;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.DataQueryDetail;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.util.SeriesSupport;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
final class RestConnection implements SdmxConnection {

    @lombok.NonNull
    private final RestClient client;
    private boolean closed = false;

    @Override
    public Set<Dataflow> getFlows() throws IOException {
        checkState();
        return new HashSet<>(client.getFlows());
    }

    @Override
    public Dataflow getFlow(DataflowRef flowRef) throws IOException {
        checkState();
        return client.getFlow(flowRef);
    }

    @Override
    public DataStructure getStructure(DataflowRef flowRef) throws IOException {
        checkState();

        DataStructureRef structRef = client.peekStructureRef(flowRef);
        if (structRef == null) {
            Dataflow flow = client.getFlow(flowRef);
            structRef = flow.getStructureRef();
        }

        return client.getStructure(structRef);
    }

    @Override
    public DataCursor getCursor(DataflowRef flowRef, DataQuery query) throws IOException {
        checkState();
        checkQuery(query);

        DataStructureRef structRef = client.peekStructureRef(flowRef);
        if (structRef == null) {
            Dataflow flow = client.getFlow(flowRef);
            structRef = flow.getStructureRef();
            flowRef = flow.getRef(); // FIXME: all,...,latest fails sometimes
        }

        DataStructure structure = client.getStructure(structRef);
        return client.getData(flowRef, structure, query);
    }

    @Override
    public Stream<Series> getStream(DataflowRef flowRef, DataQuery query) throws IOException {
        return SeriesSupport.asStream(() -> getCursor(flowRef, query));
    }

    @Override
    public boolean isSeriesKeysOnlySupported() throws IOException {
        return client.isSeriesKeysOnlySupported();
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

    private void checkState() throws IOException {
        if (closed) {
            throw new IOException("Connection closed");
        }
    }

    private void checkQuery(DataQuery query) throws IOException {
        boolean serieskeysonly = query.getDetail().equals(DataQueryDetail.SERIES_KEYS_ONLY);
        if (serieskeysonly && !isSeriesKeysOnlySupported()) {
            throw new IllegalStateException("serieskeysonly not supported");
        }
    }
}
