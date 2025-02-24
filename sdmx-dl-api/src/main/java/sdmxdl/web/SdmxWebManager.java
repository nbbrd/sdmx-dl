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

import internal.sdmxdl.ext.PersistenceLoader;
import internal.sdmxdl.web.spi.*;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.EventListener;
import sdmxdl.*;
import sdmxdl.ext.Persistence;
import sdmxdl.web.spi.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
public class SdmxWebManager extends SdmxManager<WebSource> {

    @StaticFactoryMethod
    public static @NonNull SdmxWebManager ofServiceLoader() {
        return SdmxWebManager
                .builder()
                .drivers(DriverLoader.load())
                .monitors(MonitorLoader.load())
                .networking(NetworkingLoader.load())
                .caching(WebCachingLoader.load())
                .persistences(PersistenceLoader.load())
                .authenticators(AuthenticatorLoader.load())
                .registry(RegistryLoader.load())
                .build();
    }

    @StaticFactoryMethod
    public static @NonNull SdmxWebManager noOp() {
        return SdmxWebManager.builder().build();
    }

    @lombok.Singular
    @NonNull
    List<Driver> drivers;

    @lombok.Singular
    @NonNull
    List<Monitor> monitors;

    @lombok.Builder.Default
    @NonNull
    Networking networking = Networking.getDefault();

    @lombok.Builder.Default
    @NonNull
    WebCaching caching = WebCaching.noOp();

    @Nullable
    EventListener<? super WebSource> onEvent;

    @Nullable
    ErrorListener<? super WebSource> onError;

    @lombok.Singular
    @NonNull
    List<Persistence> persistences;

    @lombok.Singular
    @NonNull
    List<Authenticator> authenticators;

    @lombok.Builder.Default
    @NonNull
    Registry registry = Registry.noOp();

    @Nullable
    Consumer<CharSequence> onRegistryEvent;

    @Nullable
    BiConsumer<CharSequence, IOException> onRegistryError;

    @lombok.Getter(lazy = true)
    @NonNull
    List<WebSource> customSources = initLazyCustomSources(getRegistry(), getPersistences(), getOnRegistryEvent(), getOnRegistryError());

    @lombok.Getter(lazy = true)
    @NonNull
    List<WebSource> defaultSources = initLazyDefaultSources(getDrivers());

    @lombok.Getter(lazy = true)
    @NonNull
    SortedMap<String, WebSource> sources = initLazySourceMap(getCustomSources(), getDefaultSources());

    @lombok.Getter(lazy = true, value = AccessLevel.PRIVATE)
    @NonNull
    WebContext context = initLazyContext();

    public @NonNull Connection getConnection(@NonNull String name, @NonNull Languages languages) throws IOException {
        WebSource source = lookupSource(name)
                .orElseThrow(() -> newMissingSource(name));

        return getConnection(source, languages);
    }

    @Override
    public @NonNull Connection getConnection(@NonNull WebSource source, @NonNull Languages languages) throws IOException {
        Driver driver = lookupDriverById(source.getDriver())
                .orElseThrow(() -> new IOException("Failed to find a suitable driver for '" + source + "'"));

        return driver.connect(source, languages, getContext());
    }

    public @NonNull MonitorReport getMonitorReport(@NonNull String name) throws IOException {
        WebSource source = lookupSource(name)
                .orElseThrow(() -> newMissingSource(name));

        return getMonitorReport(source);
    }

    public @NonNull MonitorReport getMonitorReport(@NonNull WebSource source) throws IOException {
        URI monitorURI = source.getMonitor();

        if (monitorURI == null) {
            throw new IOException("Missing monitor URI for '" + source + "'");
        }

        Monitor monitor = lookupMonitor(monitorURI.getScheme())
                .orElseThrow(() -> new IOException("Failed to find a suitable monitoring for '" + source + "'"));

        return monitor.getReport(source, getContext());
    }

    private Optional<WebSource> lookupSource(String name) {
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
                .persistences(persistences)
                .authenticators(authenticators)
                .build();
    }

    private static List<WebSource> initLazyCustomSources(Registry registry, List<Persistence> persistences, Consumer<CharSequence> onEvent, BiConsumer<CharSequence, IOException> onError) {
        return registry.getSources(persistences, onEvent, onError).getSources();
    }

    private static List<WebSource> initLazyDefaultSources(List<Driver> drivers) {
        return drivers
                .stream()
                .flatMap(driver -> driver.getDefaultSources().stream())
                .filter(distinctByKey(WebSource::getId))
                .collect(toList());
    }

    private static SortedMap<String, WebSource> initLazySourceMap(List<WebSource> customSources, List<WebSource> defaultSources) {
        return Stream.concat(customSources.stream(), defaultSources.stream())
                .flatMap(SdmxWebManager::expandAliases)
                .collect(groupingBy(WebSource::getId, TreeMap::new, reducingByFirst()));
    }

    private static Stream<WebSource> expandAliases(WebSource source) {
        Stream<WebSource> first = Stream.of(source);
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
