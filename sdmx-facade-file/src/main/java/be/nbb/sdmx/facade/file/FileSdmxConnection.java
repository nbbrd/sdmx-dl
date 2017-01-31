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
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.util.XMLStreamCompactDataCursor21;
import be.nbb.sdmx.facade.util.XMLStreamGenericDataCursor20;
import be.nbb.sdmx.facade.util.XMLStreamGenericDataCursor21;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import javax.xml.stream.XMLInputFactory;

/**
 *
 * @author Philippe Charles
 */
class FileSdmxConnection extends SdmxConnection {

    private static final DataStructureRef EMPTY = DataStructureRef.of("", "", "");

    private final File dataFile;
    private final XMLInputFactory factory;
    private final SdmxDecoder decoder;
    private final Dataflow dataflow;

    FileSdmxConnection(File data, XMLInputFactory factory, SdmxDecoder decoder) {
        this.dataFile = data;
        this.factory = factory;
        this.decoder = decoder;
        this.dataflow = Dataflow.of(DataflowRef.parse(data.getName()), EMPTY, data.getName());
    }

    @Override
    final public Set<Dataflow> getDataflows() throws IOException {
        return Collections.singleton(dataflow);
    }

    @Override
    final public Dataflow getDataflow(DataflowRef flowRef) throws IOException {
        checkFlowRef(flowRef);
        return dataflow;
    }

    @Override
    final public DataStructure getDataStructure(DataflowRef flowRef) throws IOException {
        checkFlowRef(flowRef);
        return decode().getDataStructure();
    }

    @Override
    final public DataCursor getData(DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
        checkFlowRef(flowRef);
        return loadData(decode());
    }

    @Override
    final public boolean isSeriesKeysOnlySupported() {
        return true;
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    protected SdmxDecoder.Info decode() throws IOException {
        return decoder.decode(dataFile);
    }

    protected DataCursor loadData(SdmxDecoder.Info entry) throws IOException {
        switch (entry.getFileType()) {
            case GENERIC20:
                return XMLStreamGenericDataCursor20.genericData20(factory, open(dataFile), entry.getDataStructure());
            case GENERIC21:
                return XMLStreamGenericDataCursor21.genericData21(factory, open(dataFile), entry.getDataStructure());
            case COMPACT21:
                return XMLStreamCompactDataCursor21.compactData21(factory, open(dataFile), entry.getDataStructure());
            default:
                throw new IOException("Don't known how to handle type '" + entry.getFileType() + "'");
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
