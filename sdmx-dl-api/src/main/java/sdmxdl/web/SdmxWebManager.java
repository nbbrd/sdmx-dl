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

import internal.util.SdmxWebAuthenticatorLoader;
import internal.util.SdmxWebDriverLoader;
import internal.util.SdmxWebMonitoringLoader;
import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;
import sdmxdl.ext.NetworkFactory;
import sdmxdl.ext.SdmxCache;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.ext.spi.SdmxDialectLoader;
import sdmxdl.web.spi.SdmxWebAuthenticator;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;
import sdmxdl.web.spi.SdmxWebMonitoring;

import java.io.IOException;
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
@lombok.Builder(toBuilder = true)
public class SdmxWebManager implements SdmxManager {

    @NonNull
    public static SdmxWebManager ofServiceLoader() {
        return SdmxWebManager
                .builder()
                .drivers(SdmxWebDriverLoader.load())
                .dialects(SdmxDialectLoader.load())
                .monitorings(SdmxWebMonitoringLoader.load())
                .authenticators(SdmxWebAuthenticatorLoader.load())
                .build();
    }

    @lombok.NonNull
    @lombok.Singular
    List<SdmxWebDriver> drivers;

    @lombok.NonNull
    @lombok.Singular
    List<SdmxDialect> dialects;

    @lombok.NonNull
    @lombok.Singular
    List<SdmxWebMonitoring> monitorings;

    @lombok.NonNull
    @lombok.Builder.Default
    LanguagePriorityList languages = LanguagePriorityList.ANY;

    @lombok.NonNull
    @lombok.Builder.Default
    NetworkFactory network = NetworkFactory.getDefault();

    @lombok.NonNull
    @lombok.Builder.Default
    SdmxCache cache = SdmxCache.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    SdmxWebListener eventListener = SdmxWebListener.getDefault();

    @lombok.NonNull
    @lombok.Singular
    List<SdmxWebAuthenticator> authenticators;

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

        checkSourceProperties(source, driver);

        return driver.connect(source, getContext());
    }

    @NonNull
    public SdmxWebMonitorReport getMonitorReport(@NonNull String name) throws IOException {
        Objects.requireNonNull(name);

        SdmxWebSource source = lookupSource(name)
                .orElseThrow(() -> new IOException("Cannot find entry point for '" + name + "'"));

        return getMonitorReport(source);
    }

    @NonNull
    public SdmxWebMonitorReport getMonitorReport(@NonNull SdmxWebSource source) throws IOException {
        Objects.requireNonNull(source);

        SdmxWebMonitor monitor = source.getMonitor();

        if (monitor == null) {
            throw new IOException("Missing monitor for '" + source + "'");
        }

        SdmxWebMonitoring monitoring = lookupMonitoring(monitor.getProvider())
                .orElseThrow(() -> new IOException("Failed to find a suitable monitoring for '" + source + "'"));

        return monitoring.getReport(source, getContext());
    }

    private void checkSourceProperties(SdmxWebSource source, SdmxWebDriver driver) {
        if (eventListener.isEnabled()) {
            Collection<String> expected = driver.getSupportedProperties();
            Collection<String> found = source.getProperties().keySet();
            String diff = found.stream().filter(item -> !expected.contains(item)).sorted().collect(Collectors.joining(","));
            if (!diff.isEmpty()) {
                eventListener.onWebSourceEvent(source, "Unexpected properties [" + diff + "]");
            }
        }
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

    private Optional<SdmxWebMonitoring> lookupMonitoring(String monitoringName) {
        return monitorings
                .stream()
                .filter(monitoring -> monitoringName.equals(monitoring.getProviderName()))
                .findFirst();
    }

    private SdmxWebContext initContext() {
        return SdmxWebContext
                .builder()
                .cache(cache)
                .languages(languages)
                .network(network)
                .dialects(dialects)
                .eventListener(eventListener)
                .authenticators(authenticators)
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
                .flatMap(SdmxWebManager::expandAliases)
                .collect(Collectors.groupingBy(SdmxWebSource::getName, TreeMap::new, reducingByFirst()));
    }

    private static Stream<SdmxWebSource> expandAliases(SdmxWebSource source) {
        Stream<SdmxWebSource> first = Stream.of(source);
        return !source.getAliases().isEmpty()
                ? Stream.concat(first, source.getAliases().stream().map(source::alias))
                : first;
    }

    private static <T> Collector<T, ?, T> reducingByFirst() {
        return Collectors.reducing(null, (first, last) -> first == null ? last : first);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
