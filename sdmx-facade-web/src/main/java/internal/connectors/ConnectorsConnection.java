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
package internal.connectors;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.DataQueryDetail;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.util.SeriesSupport;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
final class ConnectorsConnection implements SdmxConnection {

    interface Resource {

        @Nonnull
        Map<String, it.bancaditalia.oss.sdmx.api.Dataflow> loadDataFlowsById() throws IOException;

        @Nonnull
        it.bancaditalia.oss.sdmx.api.Dataflow loadDataflow(@Nonnull DataflowRef flowRef) throws IOException;

        @Nonnull
        it.bancaditalia.oss.sdmx.api.DataFlowStructure loadDataStructure(@Nonnull DataflowRef flowRef) throws IOException;

        @Nonnull
        DataCursor loadData(@Nonnull DataflowRef flowRef, @Nonnull Key key, boolean serieskeysonly) throws IOException;

        boolean isSeriesKeysOnlySupported();
    }

    private final Resource resource;
    private boolean closed;

    ConnectorsConnection(Resource resource) {
        this.resource = resource;
        this.closed = false;
    }

    @Override
    public Set<Dataflow> getFlows() throws IOException {
        checkState();
        return resource.loadDataFlowsById().values().stream()
                .map(Util::toFlow)
                .collect(Collectors.toSet());
    }

    @Override
    public Dataflow getFlow(DataflowRef flowRef) throws IOException {
        checkState();
        return Util.toFlow(resource.loadDataflow(flowRef));
    }

    @Override
    public DataStructure getStructure(DataflowRef flowRef) throws IOException {
        checkState();
        return Util.toStructure(resource.loadDataStructure(flowRef));
    }

    @Override
    public DataCursor getCursor(DataflowRef flowRef, DataQuery query) throws IOException {
        checkState();
        boolean serieskeysonly = query.getDetail().equals(DataQueryDetail.SERIES_KEYS_ONLY);
        if (serieskeysonly && !isSeriesKeysOnlySupported()) {
            throw new IllegalStateException("serieskeysonly not supported");
        }
        return resource.loadData(flowRef, query.getKey(), serieskeysonly);
    }

    @Override
    public Stream<Series> getStream(DataflowRef flowRef, DataQuery query) throws IOException {
        return SeriesSupport.asStream(() -> getCursor(flowRef, query));
    }

    @Override
    public boolean isSeriesKeysOnlySupported() {
        return resource.isSeriesKeysOnlySupported();
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
}
