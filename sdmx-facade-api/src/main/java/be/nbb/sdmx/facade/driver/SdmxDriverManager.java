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
package be.nbb.sdmx.facade.driver;

import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.SdmxConnectionSupplier;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public final class SdmxDriverManager implements SdmxConnectionSupplier {

    private final List<SdmxDriver> drivers;
    private final Map<String, WsEntryPoint> entryPointByName;

    public SdmxDriverManager() {
        this.drivers = new CopyOnWriteArrayList<>();
        this.entryPointByName = new ConcurrentHashMap<>();
    }

    @Override
    public SdmxConnection getConnection(String name) throws IOException {
        WsEntryPoint wsEntryPoint = entryPointByName.get(name);
        if (wsEntryPoint != null) {
            URI uri = wsEntryPoint.getUri();
            for (SdmxDriver o : drivers) {
                try {
                    if (o.acceptsURI(uri)) {
                        return o.connect(uri, wsEntryPoint.getProperties());
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SdmxDriverManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        throw new IOException(name);
    }

    @Nonnull
    public List<SdmxDriver> getDrivers() {
        return Collections.unmodifiableList(drivers);
    }

    public void setDrivers(@Nonnull List<SdmxDriver> list) {
        drivers.clear();
        drivers.addAll(list);
    }

    @Nonnull
    public List<WsEntryPoint> getEntryPoints() {
        return new ArrayList<>(entryPointByName.values());
    }

    public void setEntryPoints(@Nonnull List<WsEntryPoint> list) {
        entryPointByName.clear();
        for (WsEntryPoint o : list) {
            entryPointByName.put(o.getName(), o);
        }
    }

    @Nonnull
    public static SdmxDriverManager getDefault() {
        return Holder.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class Holder {

        private static final SdmxDriverManager INSTANCE = instanciate();

        private static SdmxDriverManager instanciate() {
            SdmxDriverManager result = new SdmxDriverManager();
            List<SdmxDriver> drivers = new ArrayList<>();
            for (SdmxDriver o : ServiceLoader.load(SdmxDriver.class)) {
                drivers.add(o);
            }
            result.setDrivers(drivers);
            List<WsEntryPoint> entryPoints = new ArrayList<>();
            for (SdmxDriver o : result.getDrivers()) {
                entryPoints.addAll(o.getDefaultEntryPoints());
            }
            result.setEntryPoints(entryPoints);
            return result;
        }
    }
    //</editor-fold>
}
