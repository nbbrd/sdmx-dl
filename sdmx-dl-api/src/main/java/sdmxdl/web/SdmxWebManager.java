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

import internal.util.*;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.EventListener;
import sdmxdl.*;
import sdmxdl.web.spi.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

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
                .drivers(DriverLoader.load())
                .monitors(MonitorLoader.load())
                .networking(NetworkingLoader.load())
                .caching(WebCachingLoader.load())
                .authenticators(AuthenticatorLoader.load())
                .build();
    }

    @StaticFactoryMethod
    public static @NonNull SdmxWebManager noOp() {
        return SdmxWebManager.builder().build();
    }

    @lombok.Singular
    @NonNull List<Driver> drivers;

    @lombok.Singular
    @NonNull List<Monitor> monitors;

    @lombok.Builder.Default
    @NonNull Networking networking = Networking.getDefault();

    @lombok.Builder.Default
    @NonNull WebCaching caching = WebCaching.noOp();

    @Nullable EventListener<? super SdmxWebSource> onEvent;

    @Nullable ErrorListener<? super SdmxWebSource> onError;

    @lombok.Singular
    @NonNull List<Authenticator> authenticators;

    @lombok.Singular
    @NonNull List<SdmxWebSource> customSources;

    @lombok.Getter(lazy = true)
    @NonNull List<SdmxWebSource> defaultSources = initLazyDefaultSources(getDrivers());

    @lombok.Getter(lazy = true)
    @NonNull SortedMap<String, SdmxWebSource> sources = initLazySourceMap(getCustomSources(), getDefaultSources());

    @lombok.Getter(lazy = true, value = AccessLevel.PRIVATE)
    @NonNull WebContext context = initLazyContext();

    public @NonNull Connection getConnection(@NonNull String name, @NonNull Languages languages) throws IOException {
        SdmxWebSource source = lookupSource(name)
                .orElseThrow(() -> newMissingSource(name));

        return getConnection(source, languages);
    }

    @Override
    public @NonNull Connection getConnection(@NonNull SdmxWebSource source, @NonNull Languages languages) throws IOException {
        Driver driver = lookupDriverById(source.getDriver())
                .orElseThrow(() -> new IOException("Failed to find a suitable driver for '" + source + "'"));

        checkSourceProperties(source, driver);

        return driver.connect(source, languages, getContext());
    }

    public @NonNull MonitorReport getMonitorReport(@NonNull String name) throws IOException {
        SdmxWebSource source = lookupSource(name)
                .orElseThrow(() -> newMissingSource(name));

        return getMonitorReport(source);
    }

    public @NonNull MonitorReport getMonitorReport(@NonNull SdmxWebSource source) throws IOException {
        URI monitorURI = source.getMonitor();

        if (monitorURI == null) {
            throw new IOException("Missing monitor URI for '" + source + "'");
        }

        Monitor monitor = lookupMonitor(monitorURI.getScheme())
                .orElseThrow(() -> new IOException("Failed to find a suitable monitoring for '" + source + "'"));

        return monitor.getReport(source, getContext());
    }

    private void checkSourceProperties(SdmxWebSource source, Driver driver) {
        if (onEvent != null) {
            Collection<String> expected = new ArrayList<>();
            expected.addAll(driver.getDriverProperties());
            expected.addAll(networking.getNetworkingProperties());
            expected.addAll(caching.getWebCachingProperties());
            Collection<String> found = source.getProperties().keySet();
            String diff = found.stream().filter(item -> !expected.contains(item)).sorted().collect(Collectors.joining(","));
            if (!diff.isEmpty()) {
                onEvent.accept(source, "WEB_MANAGER", "Unexpected properties [" + diff + "]");
            }
        }
    }

    private Optional<SdmxWebSource> lookupSource(String name) {
        return Optional.ofNullable(getSources().get(name));
    }

    private Optional<Driver> lookupDriverById(String id) {
        return drivers
                .stream()
                .filter(driver -> id.equals(driver.getDriverId()))
                .findFirst();
    }

    private Optional<Monitor> lookupMonitor(String uriScheme) {
        return monitors
                .stream()
                .filter(monitor -> uriScheme.equals(monitor.getMonitorUriScheme()))
                .findFirst();
    }

    private WebContext initLazyContext() {
        return WebContext
                .builder()
                .caching(caching)
                .networking(networking)
                .onEvent(onEvent)
                .onError(onError)
                .authenticators(authenticators)
                .build();
    }

    private static List<SdmxWebSource> initLazyDefaultSources(List<Driver> drivers) {
        return drivers
                .stream()
                .flatMap(driver -> driver.getDefaultSources().stream())
                .filter(distinctByKey(SdmxWebSource::getId))
                .collect(toList());
    }

    private static SortedMap<String, SdmxWebSource> initLazySourceMap(List<SdmxWebSource> customSources, List<SdmxWebSource> defaultSources) {
        return Stream.concat(customSources.stream(), defaultSources.stream())
                .flatMap(SdmxWebManager::expandAliases)
                .collect(groupingBy(SdmxWebSource::getId, TreeMap::new, reducingByFirst()));
    }

    private static Stream<SdmxWebSource> expandAliases(SdmxWebSource source) {
        Stream<SdmxWebSource> first = Stream.of(source);
        return !source.getAliases().isEmpty()
                ? Stream.concat(first, source.getAliases().stream().map(source::alias))
                : first;
    }

    private static IOException newMissingSource(String name) {
        return new IOException("Missing source '" + name + "'");
    }

    private static <T> Collector<T, ?, T> reducingByFirst() {
        return Collectors.reducing(null, (first, last) -> first == null ? last : first);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
