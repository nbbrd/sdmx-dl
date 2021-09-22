/*
 * Copyright 2016 National Bank of Belgium
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
package sdmxdl.util.ext;

import nbbrd.design.VisibleForTesting;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ext.SdmxCache;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.web.SdmxWebMonitorReports;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.AllArgsConstructor(staticName = "of")
public final class MapCache implements SdmxCache {

    @NonNull
    public static MapCache of() {
        return of(new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), Clock.systemDefaultZone());
    }

    @lombok.NonNull
    private final ConcurrentMap<String, SdmxRepository> repositories;

    @lombok.NonNull
    private final ConcurrentMap<String, SdmxWebMonitorReports> webMonitors;

    @lombok.NonNull
    private final Clock clock;

    @Override
    public @Nullable SdmxRepository getRepository(@NonNull String key) {
        Objects.requireNonNull(key);
        return getRepository(repositories, clock, key);
    }

    @Override
    public void putRepository(@NonNull String key, @NonNull SdmxRepository value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        put(repositories, key, value);
    }

    @Override
    public @Nullable SdmxWebMonitorReports getWebMonitorReports(@NonNull String key) {
        Objects.requireNonNull(key);
        return getWebMonitorReports(webMonitors, clock, key);
    }

    @Override
    public void putWebMonitorReports(@NonNull String key, @NonNull SdmxWebMonitorReports value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        put(webMonitors, key, value);
    }

    @VisibleForTesting
    static SdmxRepository getRepository(@NonNull ConcurrentMap<String, SdmxRepository> map, @NonNull Clock clock, @NonNull String key) {
        return get(reports -> !reports.isExpired(clock), map, key);
    }

    @VisibleForTesting
    static SdmxWebMonitorReports getWebMonitorReports(@NonNull ConcurrentMap<String, SdmxWebMonitorReports> map, @NonNull Clock clock, @NonNull String key) {
        return get(reports -> !reports.isExpired(clock), map, key);
    }

    @Nullable
    private static <T> T get(@NonNull Predicate<T> validator, @NonNull ConcurrentMap<String, T> map, @NonNull String key) {
        T result = map.get(key);
        if (result == null) {
            return null;
        }
        if (!validator.test(result)) {
            map.remove(key);
            return null;
        }
        return result;
    }

    @VisibleForTesting
    static <T> void put(@NonNull ConcurrentMap<String, T> map, @NonNull String key, @NonNull T value) {
        Objects.requireNonNull(value);
        map.put(key, value);
    }
}
