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

import be.nbb.sdmx.facade.LanguagePriorityList;
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
import java.util.stream.Collectors;
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
    public SdmxConnection getConnection(String name, LanguagePriorityList languages) throws IOException {
        WsEntryPoint wsEntryPoint = entryPointByName.get(name);
        if (wsEntryPoint == null) {
            throw new IOException("Cannot find entry point for '" + name + "'");
        }
        URI uri = wsEntryPoint.getUri();
        try {
            for (SdmxDriver o : drivers) {
                if (o.acceptsURI(uri)) {
                    return o.connect(uri, wsEntryPoint.getProperties(), languages);
                }
            }
        } catch (RuntimeException ex) {
            throw new IOException("Failed to connect to '" + name + "'", ex);
        }
        throw new IOException("Failed to find a suitable driver for '" + name + "'");
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
        list.forEach(o -> entryPointByName.put(o.getName(), o));
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
            result.setDrivers(getDrivers());
            result.setEntryPoints(getEntryPoints(result.getDrivers()));
            return result;
        }

        private static List<SdmxDriver> getDrivers() {
            List<SdmxDriver> result = new ArrayList<>();
            ServiceLoader.load(SdmxDriver.class).forEach(result::add);
            return result;
        }

        private static List<WsEntryPoint> getEntryPoints(List<SdmxDriver> drivers) {
            return drivers.stream()
                    .flatMap(o -> o.getDefaultEntryPoints().stream())
                    .collect(Collectors.toList());
        }
    }
    //</editor-fold>
}
