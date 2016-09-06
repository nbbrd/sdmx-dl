/*
 * Copyright 2016 National Bank of Belgium
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
package be.nbb.sdmx.facade.util;

import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.SdmxConnectionSupplier;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public final class MemSdmxConnectionSupplier extends SdmxConnectionSupplier {

    private final Map<String, SdmxConnection> connections;

    private MemSdmxConnectionSupplier(Map<String, SdmxConnection> connections) {
        this.connections = connections;
    }

    @Override
    public SdmxConnection getConnection(String name) {
        return connections.getOrDefault(name, SdmxConnection.failing());
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    public interface Builder {

        @Nonnull
        MemSdmxConnectionSupplier build();

        @Nonnull
        Builder add(@Nonnull String name, @Nonnull MemSdmxConnection connection);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class BuilderImpl implements Builder {

        private final Map<String, SdmxConnection> connections = new HashMap<>();

        @Override
        public Builder add(String name, MemSdmxConnection connection) {
            connections.put(name, connection);
            return this;
        }

        @Override
        public MemSdmxConnectionSupplier build() {
            return new MemSdmxConnectionSupplier(new HashMap<>(connections));
        }
    }
    //</editor-fold>
}
