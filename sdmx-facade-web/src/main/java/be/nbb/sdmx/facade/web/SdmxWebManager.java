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
import be.nbb.sdmx.facade.SdmxConnectionSupplier;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.util.UnexpectedIOException;
import be.nbb.sdmx.facade.web.spi.SdmxWebBridge;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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
@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.extern.java.Log
public final class SdmxWebManager implements SdmxConnectionSupplier, HasCache {

    @Nonnull
    public static SdmxWebManager ofServiceLoader() {
        return of(lookupBridge(), ServiceLoader.load(SdmxWebDriver.class));
    }

    @Nonnull
    public static SdmxWebManager of(@Nonnull SdmxWebBridge bridge, @Nonnull SdmxWebDriver... drivers) {
        return of(bridge, Arrays.asList(drivers));
    }

    @Nonnull
    public static SdmxWebManager of(@Nonnull SdmxWebBridge bridge, @Nonnull Iterable<? extends SdmxWebDriver> drivers) {
        Objects.requireNonNull(bridge);

        List<SdmxWebDriver> driverList = new ArrayList<>();
        drivers.forEach(driverList::add);

        ConcurrentMap<String, SdmxWebSource> sourceByName = new ConcurrentHashMap<>();
        updateSourceMap(sourceByName, driverList.stream().flatMap(o -> tryGetDefaultSources(o).stream()));

        HasCache cacheSupport = HasCache.of(ConcurrentHashMap::new, (o, n) -> applyCache(n, driverList));

        return new SdmxWebManager(bridge, driverList, sourceByName, cacheSupport);
    }

    private final SdmxWebBridge bridge;
    private final List<SdmxWebDriver> drivers;
    private final ConcurrentMap<String, SdmxWebSource> sourceByName;
    private final HasCache cacheSupport;

    @Override
    public SdmxWebConnection getConnection(String name, LanguagePriorityList languages) throws IOException {
        Objects.requireNonNull(name);
        Objects.requireNonNull(languages);

        SdmxWebSource source = sourceByName.get(name);
        if (source == null) {
            throw new IOException("Cannot find entry point for '" + name + "'");
        }
        return getConnection(source, languages);
    }

    @Nonnull
    public SdmxWebConnection getConnection(@Nonnull SdmxWebSource source) throws IOException {
        return getConnection(source, LanguagePriorityList.ANY);
    }

    @Nonnull
    public SdmxWebConnection getConnection(@Nonnull SdmxWebSource source, @Nonnull LanguagePriorityList languages) throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(languages);

        for (SdmxWebDriver o : drivers) {
            if (tryAccepts(o, source)) {
                return tryConnect(o, source, languages, bridge);
            }
        }
        throw new IOException("Failed to find a suitable driver for '" + source + "'");
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
    public List<SdmxWebSource> getSources() {
        return new ArrayList<>(sourceByName.values());
    }

    public void setSources(@Nonnull List<SdmxWebSource> list) {
        updateSourceMap(sourceByName, list.stream());
    }

    private static SdmxWebBridge lookupBridge() {
        Iterator<SdmxWebBridge> iter = ServiceLoader.load(SdmxWebBridge.class).iterator();
        return iter.hasNext() ? iter.next() : SdmxWebBridge.getDefault();
    }

    private static void applyCache(ConcurrentMap cache, List<SdmxWebDriver> drivers) {
        drivers.stream()
                .filter(HasCache.class::isInstance)
                .forEach(o -> ((HasCache) o).setCache(cache));
    }

    private static void updateSourceMap(ConcurrentMap<String, SdmxWebSource> sourceByName, Stream<SdmxWebSource> list) {
        sourceByName.clear();
        list.forEach(o -> sourceByName.put(o.getName(), o));
    }

    private static boolean tryAccepts(SdmxWebDriver driver, SdmxWebSource source) throws IOException {
        try {
            return driver.accepts(source);
        } catch (RuntimeException ex) {
            log.log(Level.WARNING, "Unexpected exception while parsing URI", ex);
            return false;
        }
    }

    @SuppressWarnings("null")
    private static SdmxWebConnection tryConnect(SdmxWebDriver driver, SdmxWebSource source, LanguagePriorityList langs, SdmxWebBridge bridge) throws IOException {
        SdmxWebConnection result;

        try {
            result = driver.connect(source, langs, bridge);
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
    private static Collection<SdmxWebSource> tryGetDefaultSources(SdmxWebDriver driver) {
        Collection<SdmxWebSource> result;

        try {
            result = driver.getDefaultSources();
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
