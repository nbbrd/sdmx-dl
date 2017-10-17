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
package be.nbb.sdmx.facade.web;

import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.SdmxConnectionSupplier;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public final class SdmxWebManager implements SdmxConnectionSupplier {

    @Nonnull
    public static SdmxWebManager ofServiceLoader() {
        return of(ServiceLoader.load(SdmxWebDriver.class));
    }

    @Nonnull
    public static SdmxWebManager of(@Nonnull SdmxWebDriver... drivers) {
        return of(Arrays.asList(drivers));
    }

    @Nonnull
    public static SdmxWebManager of(@Nonnull Iterable<? extends SdmxWebDriver> drivers) {
        CopyOnWriteArrayList<SdmxWebDriver> driverList = new CopyOnWriteArrayList<>();
        ConcurrentMap<String, WebEntryPoint> entryPointByName = new ConcurrentHashMap<>();
        drivers.forEach(o -> {
            driverList.add(o);
            o.getDefaultEntryPoints().forEach(x -> entryPointByName.put(x.getName(), x));
        });
        return new SdmxWebManager(driverList, entryPointByName);
    }

    private final CopyOnWriteArrayList<SdmxWebDriver> drivers;
    private final ConcurrentMap<String, WebEntryPoint> entryPointByName;

    private SdmxWebManager(CopyOnWriteArrayList<SdmxWebDriver> drivers, ConcurrentMap<String, WebEntryPoint> entryPointByName) {
        this.drivers = drivers;
        this.entryPointByName = entryPointByName;
    }

    @Override
    public SdmxConnection getConnection(String name, LanguagePriorityList languages) throws IOException {
        WebEntryPoint wsEntryPoint = entryPointByName.get(name);
        if (wsEntryPoint == null) {
            throw new IOException("Cannot find entry point for '" + name + "'");
        }
        URI uri = wsEntryPoint.getUri();
        try {
            for (SdmxWebDriver o : drivers) {
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
    public List<SdmxWebDriver> getDrivers() {
        return Collections.unmodifiableList(drivers);
    }

    public void setDrivers(@Nonnull List<SdmxWebDriver> list) {
        drivers.clear();
        drivers.addAll(list);
    }

    @Nonnull
    public List<WebEntryPoint> getEntryPoints() {
        return new ArrayList<>(entryPointByName.values());
    }

    public void setEntryPoints(@Nonnull List<WebEntryPoint> list) {
        entryPointByName.clear();
        list.forEach(o -> entryPointByName.put(o.getName(), o));
    }
}
