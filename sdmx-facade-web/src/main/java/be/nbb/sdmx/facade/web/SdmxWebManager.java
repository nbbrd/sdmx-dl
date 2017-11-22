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

import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.SdmxConnectionSupplier;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.util.UnexpectedIOException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import lombok.AccessLevel;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.extern.java.Log
public final class SdmxWebManager implements SdmxConnectionSupplier, HasCache {

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
        List<SdmxWebDriver> driverList = new ArrayList<>();
        drivers.forEach(driverList::add);

        ConcurrentMap<String, SdmxWebEntryPoint> entryPointByName = new ConcurrentHashMap<>();
        updateEntryPointMap(entryPointByName, driverList.stream().flatMap(o -> tryGetDefaultEntryPoints(o).stream()));

        HasCache cacheSupport = HasCache.of(ConcurrentHashMap::new, (o, n) -> applyCache(n, driverList));

        return new SdmxWebManager(driverList, entryPointByName, cacheSupport);
    }

    private final List<SdmxWebDriver> drivers;
    private final ConcurrentMap<String, SdmxWebEntryPoint> entryPointByName;
    private final HasCache cacheSupport;

    @Override
    public SdmxConnection getConnection(String name, LanguagePriorityList languages) throws IOException {
        Objects.requireNonNull(name);
        Objects.requireNonNull(languages);

        SdmxWebEntryPoint entryPoint = entryPointByName.get(name);
        if (entryPoint == null) {
            throw new IOException("Cannot find entry point for '" + name + "'");
        }
        return getConnection(entryPoint, languages);
    }

    @Nonnull
    public SdmxConnection getConnection(@Nonnull SdmxWebEntryPoint entryPoint, @Nonnull LanguagePriorityList languages) throws IOException {
        Objects.requireNonNull(entryPoint);
        Objects.requireNonNull(languages);

        for (SdmxWebDriver o : drivers) {
            if (tryAccepts(o, entryPoint)) {
                return tryConnect(o, entryPoint, languages);
            }
        }
        throw new IOException("Failed to find a suitable driver for '" + entryPoint + "'");
    }

    @Override
    public ConcurrentMap getCache() {
        return cacheSupport.getCache();
    }

    @Override
    public void setCache(ConcurrentMap cache) {
        this.cacheSupport.setCache(cache);
    }

    @Nonnull
    public List<SdmxWebEntryPoint> getEntryPoints() {
        return new ArrayList<>(entryPointByName.values());
    }

    public void setEntryPoints(@Nonnull List<SdmxWebEntryPoint> list) {
        updateEntryPointMap(entryPointByName, list.stream());
    }

    private static void applyCache(ConcurrentMap cache, List<SdmxWebDriver> drivers) {
        drivers.stream()
                .filter(HasCache.class::isInstance)
                .forEach(o -> ((HasCache) o).setCache(cache));
    }

    private static void updateEntryPointMap(ConcurrentMap<String, SdmxWebEntryPoint> entryPointByName, Stream<SdmxWebEntryPoint> list) {
        entryPointByName.clear();
        list.forEach(o -> entryPointByName.put(o.getName(), o));
    }

    private static boolean tryAccepts(SdmxWebDriver driver, SdmxWebEntryPoint entryPoint) throws IOException {
        try {
            return driver.accepts(entryPoint);
        } catch (RuntimeException ex) {
            log.log(Level.WARNING, "Unexpected exception while parsing URI", ex);
            return false;
        }
    }

    @SuppressWarnings("null")
    private static SdmxConnection tryConnect(SdmxWebDriver driver, SdmxWebEntryPoint entryPoint, LanguagePriorityList languages) throws IOException {
        SdmxConnection result;

        try {
            result = driver.connect(entryPoint, languages);
        } catch (RuntimeException ex) {
            log.log(Level.WARNING, "Unexpected exception while connecting", ex);
            throw new UnexpectedIOException(ex);
        }

        if (result == null) {
            log.log(Level.WARNING, "Unexpected null connection");
            throw new IOException("Unexpected null connection");
        }

        return result;
    }

    @SuppressWarnings("null")
    private static Collection<SdmxWebEntryPoint> tryGetDefaultEntryPoints(SdmxWebDriver driver) {
        Collection<SdmxWebEntryPoint> result;

        try {
            result = driver.getDefaultEntryPoints();
        } catch (RuntimeException ex) {
            log.log(Level.WARNING, "Unexpected exception while getting default entry points", ex);
            return Collections.emptyList();
        }

        if (result == null) {
            log.log(Level.WARNING, "Unexpected null list");
            return Collections.emptyList();
        }

        return result;
    }
}
