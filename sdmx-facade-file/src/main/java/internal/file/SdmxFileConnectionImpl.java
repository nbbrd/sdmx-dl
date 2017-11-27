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
package internal.file;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.DataQueryDetail;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.file.SdmxFileConnection;
import be.nbb.sdmx.facade.util.SeriesSupport;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class SdmxFileConnectionImpl implements SdmxFileConnection {

    public interface Resource {

        SdmxDecoder.Info decode() throws IOException;

        DataCursor loadData(SdmxDecoder.Info entry, DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException;
    }

    private final Resource resource;
    private final Dataflow dataflow;
    private boolean closed = false;

    @Override
    public DataflowRef getDataflowRef() throws IOException {
        checkState();
        return dataflow.getRef();
    }

    @Override
    public Set<Dataflow> getFlows() throws IOException {
        checkState();
        return Collections.singleton(dataflow);
    }

    @Override
    public Dataflow getFlow() throws IOException {
        checkState();
        return dataflow;
    }

    @Override
    public Dataflow getFlow(DataflowRef flowRef) throws IOException {
        checkState();
        checkFlowRef(flowRef);
        return dataflow;
    }

    @Override
    public DataStructure getStructure() throws IOException {
        checkState();
        return resource.decode().getStructure();
    }

    @Override
    public DataStructure getStructure(DataflowRef flowRef) throws IOException {
        checkState();
        checkFlowRef(flowRef);
        return resource.decode().getStructure();
    }

    @Override
    public DataCursor getCursor(DataQuery query) throws IOException {
        checkState();
        Objects.requireNonNull(query);
        return resource.loadData(resource.decode(), dataflow.getRef(), query.getKey(), query.getDetail().equals(DataQueryDetail.SERIES_KEYS_ONLY));
    }

    @Override
    public DataCursor getCursor(DataflowRef flowRef, DataQuery query) throws IOException {
        checkState();
        checkFlowRef(flowRef);
        Objects.requireNonNull(query);
        return resource.loadData(resource.decode(), dataflow.getRef(), query.getKey(), query.getDetail().equals(DataQueryDetail.SERIES_KEYS_ONLY));
    }

    @Override
    public Stream<Series> getStream(DataQuery query) throws IOException {
        return SeriesSupport.asStream(() -> getCursor(query));
    }

    @Override
    public Stream<Series> getStream(DataflowRef flowRef, DataQuery query) throws IOException {
        return SeriesSupport.asStream(() -> getCursor(flowRef, query));
    }

    @Override
    public boolean isSeriesKeysOnlySupported() {
        return true;
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

    private void checkFlowRef(DataflowRef flowRef) throws IOException {
        Objects.requireNonNull(flowRef);
        if (!this.dataflow.getRef().contains(flowRef)) {
            throw new IOException("Invalid flowref '" + flowRef + "'");
        }
    }
}
