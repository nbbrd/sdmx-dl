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

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import internal.sdmxdl.ext.PersistenceLoader;
import internal.sdmxdl.web.spi.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import org.jspecify.annotations.Nullable;
import sdmxdl.*;
import sdmxdl.EventListener;
import sdmxdl.ext.Persistence;
import sdmxdl.web.spi.*;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.EqualsAndHashCode(callSuper = false)
public class SdmxWebManager extends SdmxManager<WebSource> {

    @StaticFactoryMethod
    public static @NonNull SdmxWebManager ofServiceLoader() {
        return SdmxWebManager.builder()
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
    @NonNull List<Driver> drivers;

    @lombok.Singular
    @NonNull List<Monitor> monitors;

    @lombok.Builder.Default
    @NonNull Networking networking = Networking.getDefault();

    @lombok.Builder.Default
    @NonNull WebCaching caching = WebCaching.noOp();

    @Nullable Function<? super WebSource, EventListener> onEvent;

    @Nullable Function<? super WebSource, ErrorListener> onError;

    @lombok.Singular
    @NonNull List<Persistence> persistences;

    @lombok.Singular
    @NonNull List<Authenticator> authenticators;

    @lombok.Builder.Default
    @NonNull Registry registry = Registry.noOp();

    @Nullable EventListener onRegistryEvent;

    @Nullable ErrorListener onRegistryError;

    @lombok.Getter(lazy = true)
    @NonNull List<WebSource> customSources =
            initLazyCustomSources(getRegistry(), getPersistences(), getOnRegistryEvent(), getOnRegistryError());

    @lombok.Getter(lazy = true)
    @NonNull List<WebSource> defaultSources = initLazyDefaultSources(getDrivers());

    @lombok.Getter(lazy = true)
    @NonNull SortedMap<String, WebSource> sources = initLazySourceMap(getCustomSources(), getDefaultSources());

    @lombok.Getter(lazy = true, value = AccessLevel.PRIVATE)
    @NonNull WebContext context = initLazyContext();

    public @NonNull SdmxWebManager warmupAsync() {
        Executors.newSingleThreadExecutor(SdmxWebManager::newLowPriorityDaemonThread)
                .execute(networking::warmupNetwork);
        return this;
    }

    public @NonNull Connection getConnection(@NonNull String name, @NonNull Languages languages) throws IOException {
        WebSource source = lookupSource(name).orElseThrow(() -> newMissingSource(name));

        return getConnection(source, languages);
    }

    @Override
    public @NonNull Connection getConnection(@NonNull WebSource source, @NonNull Languages languages)
            throws IOException {
        Driver driver = lookupDriverById(source.getDriver())
                .orElseThrow(() -> new IOException("Failed to find a suitable driver for '" + source + "'"));

        if (onEvent != null) {
            EventListener listener = onEvent.apply(source);
            if (listener != null) {
                listener.accept("DRIVER", "Using driver '" + driver.getDriverId() + "'", 0);
            }
        }

        return driver.connect(source, languages, getContext());
    }

    /**
     * @deprecated use {@link #checkHealth(WebHealthRequest)} with {@link HealthCheck#MONITOR} instead
     */
    @Deprecated
    public @NonNull MonitorReport getMonitorReport(@NonNull String name) throws IOException {
        WebSource source = lookupSource(name).orElseThrow(() -> newMissingSource(name));

        return getMonitorReport(source);
    }

    /**
     * @deprecated use {@link #checkHealth(WebHealthRequest)} with {@link HealthCheck#MONITOR} instead
     */
    @Deprecated
    public @NonNull MonitorReport getMonitorReport(@NonNull WebSource source) throws IOException {
        return loadMonitorReport(source);
    }

    /**
     * Checks the health of the requested web sources.
     * <p>
     * Each requested {@linkplain WebHealthRequest#getChecks() check} is performed on every
     * {@linkplain WebHealthRequest#getSources() source}, in parallel unless
     * {@linkplain WebHealthRequest#isParallel() disabled}. Failures of individual checks are
     * reported in the returned reports instead of being thrown.
     *
     * @param request the non-null request describing the sources and the checks to perform
     * @return a non-null list of reports, sorted by source id
     * @throws IOException if a requested source cannot be found
     */
    public @NonNull List<HealthReport> checkHealth(@NonNull WebHealthRequest request) throws IOException {
        List<WebSource> sources = new ArrayList<>();
        if (request.getSources().isEmpty()) {
            getSources().values().stream().filter(source -> !source.isAlias()).forEach(sources::add);
        } else {
            for (String name : request.getSources()) {
                sources.add(lookupSource(name).orElseThrow(() -> newMissingSource(name)));
            }
        }
        if (request.getChecks().contains(HealthCheck.ACCESS)) {
            networking.warmupNetwork();
        }
        return (request.isParallel() ? sources.parallelStream() : sources.stream())
                .map(source -> checkHealth(source, request.getChecks()))
                .sorted(comparing(HealthReport::getSource))
                .collect(toList());
    }

    /**
     * Checks the health of a single web source.
     *
     * @param name   the non-null source id
     * @param checks the non-null checks to perform
     * @return a non-null report
     * @throws IOException if the source cannot be found
     * @see #checkHealth(WebHealthRequest)
     */
    public @NonNull HealthReport checkHealth(@NonNull String name, @NonNull Set<HealthCheck> checks)
            throws IOException {
        WebSource source = lookupSource(name).orElseThrow(() -> newMissingSource(name));
        if (checks.contains(HealthCheck.ACCESS)) {
            networking.warmupNetwork();
        }
        return checkHealth(source, checks);
    }

    private HealthReport checkHealth(WebSource source, Set<HealthCheck> checks) {
        HealthReport.Builder result = HealthReport.builder().source(source.getId());
        MonitorReport monitor = null;
        if (checks.contains(HealthCheck.MONITOR)) {
            if (source.getMonitor() == null) {
                result.monitorError("No monitor defined");
            } else {
                try {
                    monitor = loadMonitorReport(source);
                } catch (IOException | IllegalArgumentException ex) {
                    result.monitorError(ex.getMessage());
                }
            }
        }
        AccessReport access =
                checks.contains(HealthCheck.ACCESS) ? using(source).checkAccess() : null;
        return result.monitor(monitor)
                .access(access)
                .verdict(HealthVerdict.of(monitor != null ? monitor.getStatus() : null, access))
                .build();
    }

    private MonitorReport loadMonitorReport(WebSource source) throws IOException {
        URI monitorURI = source.getMonitor();

        if (monitorURI == null) {
            throw new IOException("Missing monitor URI for '" + source + "'");
        }

        Monitor monitor = lookupMonitor(monitorURI.getScheme())
                .orElseThrow(() -> new IOException("Failed to find a suitable monitoring for '" + source + "'"));

        return monitor.getReport(source, getContext());
    }

    /**
     * Lists the web sources known to this manager, applying the filtering, ranking and
     * limit options carried by the given request.
     * <p>
     * Sources are first restricted to non-alias sources whose confidentiality is allowed
     * by {@link WebSourcesRequest#getConfidentialityThreshold()}. Then:
     * <ul>
     *     <li>if the request has no query ({@link HasSearch#NO_QUERY}), the remaining
     *     sources are sorted by {@linkplain WebSource#getId() id} and truncated to
     *     {@link WebSourcesRequest#getEffectiveMaxResults()}</li>
     *     <li>otherwise, the remaining sources are ranked by relevance to
     *     {@link WebSourcesRequest#getQuery()} using {@link Search#ofSources(Collection, Languages)}
     *     and limited to {@link WebSourcesRequest#getEffectiveMaxResults()}</li>
     * </ul>
     *
     * @param request the non-null request describing the query, threshold and limit to apply
     * @return a non-null, possibly empty, list of matching web sources
     */
    public @NonNull List<WebSource> listSources(@NonNull WebSourcesRequest request) {
        Collection<WebSource> result = getSources().values().stream()
                .filter(source -> !source.isAlias())
                .filter(request.getConfidentialityThreshold()::isAllowedIn)
                .collect(toList());
        return request.getQuery().isEmpty()
                ? result.stream()
                        .sorted(comparing(WebSource::getId))
                        .limit(request.getEffectiveMaxResults())
                        .collect(toList())
                : Search.ofSources(result, request.getLanguages())
                        .search(request.getQuery(), request.getEffectiveMaxResults())
                        .stream()
                        .map(Search.Result::getItem)
                        .collect(toList());
    }

    public @NonNull Provider<WebSource> usingName(@NonNull String name) throws IOException {
        return using(lookupSource(name).orElseThrow(() -> newMissingSource(name)));
    }

    private Optional<WebSource> lookupSource(String name) {
        return Optional.ofNullable(getSources().get(name));
    }

    private Optional<Driver> lookupDriverById(String id) {
        return drivers.stream()
                .map(FailsafeDriver::wrap)
                .filter(driver -> id.equals(driver.getDriverId()))
                .findFirst();
    }

    private Optional<Monitor> lookupMonitor(String uriScheme) {
        return monitors.stream()
                .filter(monitor -> uriScheme.equals(monitor.getMonitorUriScheme()))
                .findFirst();
    }

    private WebContext initLazyContext() {
        return WebContext.builder()
                .caching(caching)
                .networking(networking)
                .onEvent(onEvent)
                .onError(onError)
                .authenticators(authenticators)
                .build();
    }

    private static List<WebSource> initLazyCustomSources(
            Registry registry, List<Persistence> persistences, EventListener onEvent, ErrorListener onError) {
        return registry.getSources(persistences, onEvent, onError).getSources();
    }

    private static List<WebSource> initLazyDefaultSources(List<Driver> drivers) {
        return drivers.stream()
                .flatMap(driver -> driver.getDefaultSources().stream())
                .filter(distinctByKey(WebSource::getId))
                .collect(toList());
    }

    private static SortedMap<String, WebSource> initLazySourceMap(
            List<WebSource> customSources, List<WebSource> defaultSources) {
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

    @StaticFactoryMethod(Thread.class)
    public static @NonNull Thread newLowPriorityDaemonThread(@NonNull Runnable runnable) {
        Thread result = new Thread(runnable);
        result.setDaemon(true);
        result.setPriority(Thread.MIN_PRIORITY);
        return result;
    }

    @StaticFactoryMethod(EventListener.class)
    public static EventListener printEvent(WebSource source) {
        return (marker, message) -> System.out.println("[" + source.getId() + "] (" + marker + ") " + message);
    }

    @StaticFactoryMethod(ErrorListener.class)
    public static ErrorListener printError(WebSource source) {
        return (marker, message, error) ->
                System.err.println("[" + source.getId() + "] (" + marker + ") " + message + ": " + error.getMessage());
    }
}
