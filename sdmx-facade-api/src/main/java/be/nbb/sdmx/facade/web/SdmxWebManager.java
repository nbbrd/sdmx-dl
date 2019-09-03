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
import java.io.IOException;
import java.net.ProxySelector;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import lombok.AccessLevel;
import be.nbb.sdmx.facade.SdmxManager;
import internal.util.SdmxWebDriverLoader;
import internal.util.SdmxWebDriverProc;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.StreamSupport;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.extern.java.Log
public final class SdmxWebManager implements SdmxManager {

    @NonNull
    public static SdmxWebManager ofServiceLoader() {
        return of(new SdmxWebDriverLoader().get());
    }

    @NonNull
    public static SdmxWebManager of(@NonNull SdmxWebDriver... drivers) {
        return of(Arrays.asList(drivers));
    }

    @NonNull
    public static SdmxWebManager of(@NonNull Iterable<SdmxWebDriver> drivers) {
        List<SdmxWebDriver> orderedList = SdmxWebDriverProc.INSTANCE
                .apply(StreamSupport.stream(drivers.spliterator(), false))
                .collect(Collectors.toList());
        return new SdmxWebManager(orderedList, sourcesOf(orderedList));
    }

    private static CopyOnWriteArrayList sourcesOf(List<SdmxWebDriver> drivers) {
        return drivers
                .stream()
                .flatMap(driver -> driver.getDefaultSources().stream())
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    private final AtomicReference<SdmxWebContext> context = new AtomicReference<>(SdmxWebContext.builder().build());
    private final List<SdmxWebDriver> drivers;
    private final CopyOnWriteArrayList<SdmxWebSource> sources;

    @Override
    public SdmxWebConnection getConnection(String name) throws IOException {
        Objects.requireNonNull(name);

        SdmxWebSource source = lookupSource(name)
                .orElseThrow(() -> new IOException("Cannot find entry point for '" + name + "'"));

        return getConnection(source);
    }

    @NonNull
    public SdmxWebConnection getConnection(@NonNull SdmxWebSource source) throws IOException {
        Objects.requireNonNull(source);

        SdmxWebDriver driver = lookupDriver(source.getDriver())
                .orElseThrow(() -> new IOException("Failed to find a suitable driver for '" + source + "'"));

        return driver.connect(source, context.get());
    }

    @Override
    public LanguagePriorityList getLanguages() {
        return context.get().getLanguages();
    }

    public void setLanguages(@NonNull LanguagePriorityList languages) {
        LanguagePriorityList newObj = languages != null ? languages : LanguagePriorityList.ANY;
        this.context.set(context.get().toBuilder().languages(newObj).build());
    }

    @NonNull
    public ProxySelector getProxySelector() {
        return context.get().getProxySelector();
    }

    public void setProxySelector(@Nullable ProxySelector proxySelector) {
        ProxySelector newObj = proxySelector != null ? proxySelector : getDefaultProxySelector();
        context.set(context.get().toBuilder().proxySelector(newObj).build());
    }

    @NonNull
    public SSLSocketFactory getSSLSocketFactory() {
        return context.get().getSslSocketFactory();
    }

    public void setSSLSocketFactory(@Nullable SSLSocketFactory sslSocketFactory) {
        SSLSocketFactory newObj = sslSocketFactory != null ? sslSocketFactory : getDefaultSSLSocketFactory();
        context.set(context.get().toBuilder().sslSocketFactory(newObj).build());
    }

    @NonNull
    public Logger getLogger() {
        return context.get().getLogger();
    }

    public void setLogger(@Nullable Logger logger) {
        Logger newObj = logger != null ? logger : getDefaultLogger();
        context.set(context.get().toBuilder().logger(newObj).build());
    }

    @NonNull
    public List<SdmxWebSource> getSources() {
        return Collections.unmodifiableList(sources);
    }

    public void setSources(@NonNull List<SdmxWebSource> list) {
        sources.clear();
        sources.addAll(list);
    }

    @NonNull
    public List<String> getDrivers() {
        return drivers
                .stream()
                .map(SdmxWebDriver::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @NonNull
    public Collection<String> getSupportedProperties(@NonNull String driver) {
        Objects.requireNonNull(driver);
        return lookupDriver(driver)
                .map(SdmxWebDriver::getSupportedProperties)
                .orElse(Collections.emptyList());
    }

    private Optional<SdmxWebSource> lookupSource(String name) {
        return sources
                .stream()
                .filter(o -> name.equals(o.getName()))
                .findFirst();
    }

    private Optional<SdmxWebDriver> lookupDriver(String name) {
        return drivers
                .stream()
                .filter(o -> name.equals(o.getName()))
                .findFirst();
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
