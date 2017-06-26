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
package be.nbb.sdmx.facade.file;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.xml.stream.SdmxXmlStreams;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import javax.xml.stream.XMLInputFactory;

/**
 *
 * @author Philippe Charles
 */
class FileSdmxConnection implements SdmxConnection {

    private static final DataStructureRef EMPTY = DataStructureRef.of("", "", "");

    private final SdmxFile file;
    private final LanguagePriorityList languages;
    private final XMLInputFactory factory;
    private final SdmxDecoder decoder;
    private final Dataflow dataflow;
    private boolean closed;

    FileSdmxConnection(SdmxFile file, LanguagePriorityList languages, XMLInputFactory factory, SdmxDecoder decoder) {
        this.file = file;
        this.languages = languages;
        this.factory = factory;
        this.decoder = decoder;
        this.dataflow = Dataflow.of(file.getDataflowRef(), EMPTY, file.getData().getName());
        this.closed = false;
    }

    @Override
    final public Set<Dataflow> getDataflows() throws IOException {
        checkState();
        return Collections.singleton(dataflow);
    }

    @Override
    final public Dataflow getDataflow(DataflowRef flowRef) throws IOException {
        checkState();
        Objects.requireNonNull(flowRef);
        checkFlowRef(flowRef);
        return dataflow;
    }

    @Override
    final public DataStructure getDataStructure(DataflowRef flowRef) throws IOException {
        checkState();
        Objects.requireNonNull(flowRef);
        checkFlowRef(flowRef);
        return decode().getDataStructure();
    }

    @Override
    final public DataCursor getData(DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
        checkState();
        Objects.requireNonNull(flowRef);
        Objects.requireNonNull(key);
        checkFlowRef(flowRef);
        return loadData(decode(), flowRef, key, serieskeysonly);
    }

    @Override
    final public boolean isSeriesKeysOnlySupported() {
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

    protected SdmxDecoder.Info decode() throws IOException {
        return decoder.decode(file, languages);
    }

    protected DataCursor loadData(SdmxDecoder.Info entry, DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
        switch (entry.getDataType()) {
            case GENERIC20:
                return SdmxXmlStreams.genericData20(factory, open(file.getData()), entry.getDataStructure());
            case COMPACT20:
                return SdmxXmlStreams.compactData20(factory, open(file.getData()), entry.getDataStructure());
            case GENERIC21:
                return SdmxXmlStreams.genericData21(factory, open(file.getData()), entry.getDataStructure());
            case COMPACT21:
                return SdmxXmlStreams.compactData21(factory, open(file.getData()), entry.getDataStructure());
            default:
                throw new IOException("Don't known how to handle type '" + entry.getDataType() + "'");
        }
    }

    private void checkFlowRef(DataflowRef flowRef) throws IOException {
        if (!this.dataflow.getFlowRef().contains(flowRef)) {
            throw new IOException("Invalid flowref '" + flowRef + "'");
        }
    }

    private static InputStreamReader open(File file) throws FileNotFoundException {
        return new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
    }
}
