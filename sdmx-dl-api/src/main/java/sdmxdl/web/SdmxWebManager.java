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

import internal.util.WebAuthenticatorLoader;
import internal.util.WebDriverLoader;
import internal.util.WebMonitoringLoader;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import sdmxdl.Connection;
import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;
import sdmxdl.ext.Cache;
import sdmxdl.web.spi.WebAuthenticator;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;
import sdmxdl.web.spi.WebMonitoring;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
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
@lombok.EqualsAndHashCode(callSuper = false)
public class SdmxWebManager extends SdmxManager<SdmxWebSource> {

    @StaticFactoryMethod
    public static @NonNull SdmxWebManager ofServiceLoader() {
        return SdmxWebManager
                .builder()
                .drivers(WebDriverLoader.load())
                .monitorings(WebMonitoringLoader.load())
                .authenticators(WebAuthenticatorLoader.load())
                .build();
    }

    @StaticFactoryMethod
    public static @NonNull SdmxWebManager noOp() {
        return SdmxWebManager.builder().build();
    }

    @lombok.NonNull
    @lombok.Singular
    List<WebDriver> drivers;

    @lombok.NonNull
    @lombok.Singular
    List<WebMonitoring> monitorings;

    @lombok.NonNull
    @lombok.Builder.Default
    LanguagePriorityList languages = LanguagePriorityList.ANY;

    @lombok.NonNull
    @lombok.Builder.Default
    Network network = Network.getDefault();

    @lombok.NonNull
    @lombok.Builder.Default
    Cache cache = Cache.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    BiConsumer<? super SdmxWebSource, ? super String> eventListener = NO_OP_EVENT_LISTENER;

    @lombok.NonNull
    @lombok.Singular
    List<WebAuthenticator> authenticators;

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
    WebContext context = initContext();

    public @NonNull Connection getConnection(@NonNull String name) throws IOException {
        SdmxWebSource source = lookupSource(name)
                .orElseThrow(() -> newMissingSource(name));

        return getConnection(source);
    }

    @Override
    public @NonNull Connection getConnection(@NonNull SdmxWebSource source) throws IOException {
        WebDriver driver = lookupDriverById(source.getDriver())
                .orElseThrow(() -> new IOException("Failed to find a suitable driver for '" + source + "'"));

        checkSourceProperties(source, driver);

        return driver.connect(source, getContext());
    }

    @NonNull
    public MonitorReport getMonitorReport(@NonNull String name) throws IOException {
        SdmxWebSource source = lookupSource(name)
                .orElseThrow(() -> newMissingSource(name));

        return getMonitorReport(source);
    }

    @NonNull
    public MonitorReport getMonitorReport(@NonNull SdmxWebSource source) throws IOException {
        URI monitor = source.getMonitor();

        if (monitor == null) {
            throw new IOException("Missing monitor for '" + source + "'");
        }

        WebMonitoring monitoring = lookupMonitoring(monitor.getScheme())
                .orElseThrow(() -> new IOException("Failed to find a suitable monitoring for '" + source + "'"));

        return monitoring.getReport(source, getContext());
    }

    @Override
    public @NonNull Optional<String> getDialect(@NonNull SdmxWebSource source) {
        return source.getDialect() != null
                ? Optional.of(source.getDialect())
                : lookupDriverById(source.getDriver()).map(WebDriver::getDefaultDialect);
    }

    private void checkSourceProperties(SdmxWebSource source, WebDriver driver) {
        if (eventListener != NO_OP_EVENT_LISTENER) {
            Collection<String> expected = driver.getSupportedProperties();
            Collection<String> found = source.getProperties().keySet();
            String diff = found.stream().filter(item -> !expected.contains(item)).sorted().collect(Collectors.joining(","));
            if (!diff.isEmpty()) {
                eventListener.accept(source, "Unexpected properties [" + diff + "]");
            }
        }
    }

    private Optional<SdmxWebSource> lookupSource(String name) {
        return Optional.ofNullable(getSources().get(name));
    }

    private Optional<WebDriver> lookupDriverById(String id) {
        return drivers
                .stream()
                .filter(driver -> id.equals(driver.getId()))
                .findFirst();
    }

    private Optional<WebMonitoring> lookupMonitoring(String uriScheme) {
        return monitorings
                .stream()
                .filter(monitoring -> uriScheme.equals(monitoring.getUriScheme()))
                .findFirst();
    }

    private WebContext initContext() {
        return WebContext
                .builder()
                .cache(cache)
                .languages(languages)
                .network(network)
                .eventListener(eventListener)
                .authenticators(authenticators)
                .build();
    }

    private static List<SdmxWebSource> initDefaultSources(List<WebDriver> drivers) {
        return drivers
                .stream()
                .flatMap(driver -> driver.getDefaultSources().stream())
                .filter(distinctByKey(SdmxWebSource::getId))
                .collect(Collectors.toList());
    }

    private static SortedMap<String, SdmxWebSource> initSourceMap(List<SdmxWebSource> customSources, List<SdmxWebSource> defaultSources) {
        return Stream.concat(customSources.stream(), defaultSources.stream())
                .flatMap(SdmxWebManager::expandAliases)
                .collect(Collectors.groupingBy(SdmxWebSource::getId, TreeMap::new, reducingByFirst()));
    }

    private static Stream<SdmxWebSource> expandAliases(SdmxWebSource source) {
        Stream<SdmxWebSource> first = Stream.of(source);
        return !source.getAliases().isEmpty()
                ? Stream.concat(first, source.getAliases().stream().map(source::alias))
                : first;
    }

    private static IOException newMissingSource(String name) {
        return new IOException("Missing " + SdmxWebManager.class.getSimpleName() + " '" + name + "'");
    }

    private static <T> Collector<T, ?, T> reducingByFirst() {
        return Collectors.reducing(null, (first, last) -> first == null ? last : first);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
