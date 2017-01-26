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
import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@NotThreadSafe
public abstract class DataCursor implements Closeable {

    abstract public boolean nextSeries() throws IOException;

    abstract public boolean nextObs() throws IOException;

    @Nonnull
    abstract public Key getKey() throws IOException;

    @Nonnull
    abstract public TimeFormat getTimeFormat() throws IOException;

    @Nullable
    abstract public Date getPeriod() throws IOException;

    @Nullable
    abstract public Double getValue() throws IOException;

    @Nonnull
    public static DataCursor noOp() {
        return NoOpCursor.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class NoOpCursor extends DataCursor {

        private static final NoOpCursor INSTANCE = new NoOpCursor();

        @Override
        public boolean nextSeries() throws IOException {
            return false;
        }

        @Override
        public boolean nextObs() throws IOException {
            return false;
        }

        @Override
        public Key getKey() throws IOException {
            throw new IllegalStateException();
        }

        @Override
        public TimeFormat getTimeFormat() throws IOException {
            throw new IllegalStateException();
        }

        @Override
        public Date getPeriod() throws IOException {
            throw new IllegalStateException();
        }

        @Override
        public Double getValue() throws IOException {
            throw new IllegalStateException();
        }

        @Override
        public void close() throws IOException {
        }
    }
    //</editor-fold>
}
