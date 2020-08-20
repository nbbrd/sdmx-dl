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
package sdmxdl.web;

import internal.util.SdmxWebDriverLoader;
import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;
import sdmxdl.ext.SdmxCache;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.ext.spi.SdmxDialectLoader;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
@lombok.With
public class SdmxWebManager implements SdmxManager {

    @NonNull
    public static SdmxWebManager ofServiceLoader() {
        return SdmxWebManager
                .builder()
                .drivers(new SdmxWebDriverLoader().get())
                .dialects(new SdmxDialectLoader().get())
                .build();
    }

    @lombok.NonNull
    @lombok.Singular
    List<SdmxWebDriver> drivers;

    @lombok.NonNull
    @lombok.Singular
    List<SdmxDialect> dialects;

    @lombok.NonNull
    LanguagePriorityList languages;

    @lombok.NonNull
    ProxySelector proxySelector;

    @lombok.NonNull
    SSLSocketFactory sslSocketFactory;

    @lombok.NonNull
    SdmxCache cache;

    @lombok.NonNull
    SdmxWebListener eventListener;

    @lombok.NonNull
    SdmxWebAuthenticator authenticator;

    @lombok.NonNull
    @lombok.Singular
    List<SdmxWebSource> customSources;

    @lombok.NonNull
    @lombok.Getter(lazy = true)
    List<SdmxWebSource> defaultSources = initDefaultSources(getDrivers());

    @lombok.NonNull
    @lombok.Getter(lazy = true)
    SortedMap<String, SdmxWebSource> sources = initSourceMap(getCustomSources(), getDefaultSources());

    @lombok.NonNull
    @lombok.Getter(lazy = true, value = AccessLevel.PRIVATE)
    SdmxWebContext context = initContext();

    // Fix lombok.Builder.Default bug in NetBeans
    public static Builder builder() {
        return new Builder()
                .languages(LanguagePriorityList.ANY)
                .proxySelector(ProxySelector.getDefault())
                .sslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory())
                .cache(SdmxCache.noOp())
                .eventListener(SdmxWebListener.getDefault())
                .authenticator(SdmxWebAuthenticator.noOp());
    }

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

        return driver.connect(source, getContext());
    }

    @NonNull
    public List<String> getDriverNames() {
        return drivers
                .stream()
                .map(SdmxWebDriver::getName)
                .collect(Collectors.toList());
    }

    @NonNull
    public Collection<String> getSupportedProperties(@NonNull String driver) {
        Objects.requireNonNull(driver);
        return lookupDriver(driver)
                .map(SdmxWebDriver::getSupportedProperties)
                .orElse(Collections.emptyList());
    }

    private Optional<SdmxWebSource> lookupSource(String sourceName) {
        return Optional.ofNullable(getSources().get(sourceName));
    }

    private Optional<SdmxWebDriver> lookupDriver(String driverName) {
        return drivers
                .stream()
                .filter(webDriver -> driverName.equals(webDriver.getName()))
                .findFirst();
    }

    private SdmxWebContext initContext() {
        return SdmxWebContext
                .builder()
                .cache(cache)
                .languages(languages)
                .proxySelector(proxySelector)
                .sslSocketFactory(sslSocketFactory)
                .dialects(dialects)
                .eventListener(eventListener)
                .authenticator(authenticator)
                .build();
    }

    private static List<SdmxWebSource> initDefaultSources(List<SdmxWebDriver> drivers) {
        return drivers
                .stream()
                .flatMap(driver -> driver.getDefaultSources().stream())
                .filter(distinctByKey(SdmxWebSource::getName))
                .collect(Collectors.toList());
    }

    private static SortedMap<String, SdmxWebSource> initSourceMap(List<SdmxWebSource> customSources, List<SdmxWebSource> defaultSources) {
        return Stream.concat(customSources.stream(), defaultSources.stream())
                .collect(Collectors.groupingBy(SdmxWebSource::getName, TreeMap::new, reducingByFirst()));
    }

    private static Collector<SdmxWebSource, ?, SdmxWebSource> reducingByFirst() {
        return Collectors.reducing(null, (first, last) -> first == null ? last : first);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
