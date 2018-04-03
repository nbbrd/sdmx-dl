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

import be.nbb.sdmx.facade.web.spi.SdmxWebContext;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.SdmxConnectionSupplier;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.util.UnexpectedIOException;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
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

        ConcurrentMap<String, SdmxWebSource> sourceByName = new ConcurrentHashMap<>();
        updateSourceMap(sourceByName, driverList.stream().flatMap(o -> tryGetDefaultSources(o)));

        HasCache cacheSupport = HasCache.of(ConcurrentHashMap::new, (o, n) -> applyCache(n, driverList));

        return new SdmxWebManager(new AtomicReference<>(SdmxWebContext.builder().build()), driverList, sourceByName, cacheSupport);
    }

    private final AtomicReference<SdmxWebContext> context;
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

        SdmxWebDriver driver = drivers
                .stream()
                .filter(o -> source.getDriver().equals(o.getName()))
                .findFirst()
                .orElseThrow(() -> new IOException("Failed to find a suitable driver for '" + source + "'"));

        return tryConnect(driver, source, languages, context.get());
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
    public ProxySelector getProxySelector() {
        return context.get().getProxySelector();
    }

    public void setProxySelector(@Nullable ProxySelector proxySelector) {
        ProxySelector newObj = proxySelector != null ? proxySelector : getDefaultProxySelector();
        context.set(context.get().toBuilder().proxySelector(newObj).build());
    }

    @Nonnull
    public SSLSocketFactory getSSLSocketFactory() {
        return context.get().getSslSocketFactory();
    }

    public void setSSLSocketFactory(@Nullable SSLSocketFactory sslSocketFactory) {
        SSLSocketFactory newObj = sslSocketFactory != null ? sslSocketFactory : getDefaultSSLSocketFactory();
        context.set(context.get().toBuilder().sslSocketFactory(newObj).build());
    }

    @Nonnull
    public Logger getLogger() {
        return context.get().getLogger();
    }

    public void setLogger(@Nullable Logger logger) {
        Logger newObj = logger != null ? logger : getDefaultLogger();
        context.set(context.get().toBuilder().logger(newObj).build());
    }

    @Nonnull
    public List<SdmxWebSource> getSources() {
        return new ArrayList<>(sourceByName.values());
    }

    public void setSources(@Nonnull List<SdmxWebSource> list) {
        updateSourceMap(sourceByName, list.stream());
    }

    @Nonnull
    public List<String> getDrivers() {
        return drivers
                .stream()
                .map(SdmxWebDriver::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nonnull
    public Collection<String> getSupportedProperties(@Nonnull String driver) {
        return drivers
                .stream()
                .filter(o -> driver.equals(o.getName()))
                .map(SdmxWebDriver::getSupportedProperties)
                .findFirst()
                .orElse(Collections.emptyList());
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

    @SuppressWarnings("null")
    private static SdmxWebConnection tryConnect(SdmxWebDriver driver, SdmxWebSource s, LanguagePriorityList l, SdmxWebContext c) throws IOException {
        SdmxWebConnection result;

        try {
            result = driver.connect(s, l, c);
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
    private static Stream<SdmxWebSource> tryGetDefaultSources(SdmxWebDriver driver) {
        Collection<SdmxWebSource> result;

        try {
            result = driver.getDefaultSources();
        } catch (RuntimeException ex) {
            log.log(Level.WARNING, "Unexpected exception while getting default entry points", ex);
            return Stream.empty();
        }

        if (result == null) {
            log.log(Level.WARNING, "Unexpected null list");
            return Stream.empty();
        }

        return result
                .stream()
                .filter(o -> o.getDriver().equals(driver.getName()));
    }

    private static ProxySelector getDefaultProxySelector() {
        return ProxySelector.getDefault();
    }

    private static SSLSocketFactory getDefaultSSLSocketFactory() {
        return HttpsURLConnection.getDefaultSSLSocketFactory();
    }

    private static Logger getDefaultLogger() {
        return Logger.getLogger(SdmxWebManager.class.getName());
    }
}
