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

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public abstract class SdmxConnection implements AutoCloseable {

    @Nonnull
    abstract public Set<Dataflow> getDataflows() throws IOException;

    @Nonnull
    abstract public Dataflow getDataflow(@Nonnull FlowRef flowRef) throws IOException;

    @Nonnull
    abstract public DataStructure getDataStructure(@Nonnull FlowRef flowRef) throws IOException;

    @Nonnull
    abstract public DataCursor getData(@Nonnull FlowRef flowRef, @Nonnull Key key, boolean serieskeysonly) throws IOException;

    public boolean isSeriesKeysOnlySupported() {
        return false;
    }

    @Override
    public void close() throws IOException {
    }

    @Nonnull
    public static SdmxConnection noOp() {
        return NoOpConnection.INSTANCE;
    }

    @Nonnull
    public static SdmxConnection failing() {
        return FailingConnection.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class NoOpConnection extends SdmxConnection {

        private static final NoOpConnection INSTANCE = new NoOpConnection();

        @Override
        public Set<Dataflow> getDataflows() throws IOException {
            return Collections.emptySet();
        }

        @Override
        public Dataflow getDataflow(FlowRef flowRef) throws IOException {
            throw new IOException(flowRef.toString());
        }

        @Override
        public DataStructure getDataStructure(FlowRef flowRef) throws IOException {
            throw new IOException(flowRef.toString());
        }

        @Override
        public DataCursor getData(FlowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
            return DataCursor.noOp();
        }
    }

    private static final class FailingConnection extends SdmxConnection {

        private static final FailingConnection INSTANCE = new FailingConnection();

        @Override
        public Set<Dataflow> getDataflows() throws IOException {
            throw new IOException("");
        }

        @Override
        public Dataflow getDataflow(FlowRef flowRef) throws IOException {
            throw new IOException("");
        }

        @Override
        public DataStructure getDataStructure(FlowRef flowRef) throws IOException {
            throw new IOException("");
        }

        @Override
        public DataCursor getData(FlowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
            throw new IOException("");
        }
    }
    //</editor-fold>
}
