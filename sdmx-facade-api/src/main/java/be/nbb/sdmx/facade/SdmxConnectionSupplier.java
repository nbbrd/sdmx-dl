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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@ThreadSafe
public abstract class SdmxConnectionSupplier {

    @Nonnull
    abstract public SdmxConnection getConnection(@Nonnull String name);

    @Nonnull
    public static SdmxConnectionSupplier noOp() {
        return NoOpSupplier.INSTANCE;
    }

    @Nonnull
    public static SdmxConnectionSupplier failing() {
        return FailingSupplier.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation">
    private static final class NoOpSupplier extends SdmxConnectionSupplier {

        private static final NoOpSupplier INSTANCE = new NoOpSupplier();

        @Override
        public SdmxConnection getConnection(String name) {
            return SdmxConnection.noOp();
        }
    }

    private static final class FailingSupplier extends SdmxConnectionSupplier {

        private static final FailingSupplier INSTANCE = new FailingSupplier();

        @Override
        public SdmxConnection getConnection(String name) {
            return SdmxConnection.failing();
        }
    }
    //</editor-fold>
}
