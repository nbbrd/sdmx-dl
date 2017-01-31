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
package be.nbb.sdmx.facade;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@NotThreadSafe
public abstract class SdmxConnection implements Closeable {

    @Nonnull
    abstract public Set<Dataflow> getDataflows() throws IOException;

    @Nonnull
    abstract public Dataflow getDataflow(@Nonnull DataflowRef flowRef) throws IOException;

    @Nonnull
    abstract public DataStructure getDataStructure(@Nonnull DataflowRef flowRef) throws IOException;

    @Nonnull
    abstract public DataCursor getData(@Nonnull DataflowRef flowRef, @Nonnull Key key, boolean serieskeysonly) throws IOException;

    abstract public boolean isSeriesKeysOnlySupported() throws IOException;

    @Nonnull
    public static SdmxConnection noOp() {
        return NoOpConnection.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class NoOpConnection extends SdmxConnection {

        private static final NoOpConnection INSTANCE = new NoOpConnection();

        @Override
        public Set<Dataflow> getDataflows() throws IOException {
            return Collections.emptySet();
        }

        @Override
        public Dataflow getDataflow(DataflowRef flowRef) throws IOException {
            throw new IOException(flowRef.toString());
        }

        @Override
        public DataStructure getDataStructure(DataflowRef flowRef) throws IOException {
            throw new IOException(flowRef.toString());
        }

        @Override
        public DataCursor getData(DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
            return DataCursor.noOp();
        }

        @Override
        public boolean isSeriesKeysOnlySupported() throws IOException {
            return false;
        }

        @Override
        public void close() throws IOException {
            // nothing to do
        }
    }
    //</editor-fold>
}
