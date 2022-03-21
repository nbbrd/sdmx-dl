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

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ext.Cache;
import sdmxdl.web.MonitorReports;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.AllArgsConstructor(staticName = "of")
public final class MapCache implements Cache {

    @NonNull
    public static MapCache of() {
        return of(new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), Clock.systemDefaultZone());
    }

    @lombok.NonNull
    private final ConcurrentMap<String, DataRepository> repositories;

    @lombok.NonNull
    private final ConcurrentMap<String, MonitorReports> webMonitors;

    @lombok.NonNull
    private final Clock clock;

    @Override
    public @Nullable DataRepository getRepository(@NonNull String key) {
        return getRepository(repositories, clock, key);
    }

    @Override
    public void putRepository(@NonNull String key, @NonNull DataRepository value) {
        put(repositories, key, value);
    }

    @Override
    public @Nullable MonitorReports getMonitorReports(@NonNull String key) {
        return getWebMonitorReports(webMonitors, clock, key);
    }

    @Override
    public void putMonitorReports(@NonNull String key, @NonNull MonitorReports value) {
        put(webMonitors, key, value);
    }

    @VisibleForTesting
    static DataRepository getRepository(@NonNull ConcurrentMap<String, DataRepository> map, @NonNull Clock clock, @NonNull String key) {
        return get(reports -> !reports.isExpired(clock), map, key);
    }

    @VisibleForTesting
    static MonitorReports getWebMonitorReports(@NonNull ConcurrentMap<String, MonitorReports> map, @NonNull Clock clock, @NonNull String key) {
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
        map.put(key, value);
    }
}
