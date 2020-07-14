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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ext.SdmxCache;
import sdmxdl.repo.SdmxRepository;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.AllArgsConstructor(staticName = "of")
public final class MapCache implements SdmxCache {

    @NonNull
    public static MapCache of() {
        return of(new ConcurrentHashMap(), Clock.systemDefaultZone());
    }

    @lombok.NonNull
    private final ConcurrentMap map;

    @lombok.NonNull
    private final Clock clock;

    @Override
    public SdmxRepository get(String key) {
        return get(map, clock, key);
    }

    @Override
    public void put(String key, SdmxRepository value, Duration ttl) {
        put(map, clock, key, value, ttl);
    }

    @Nullable
    static SdmxRepository get(@NonNull ConcurrentMap map, @NonNull Clock clock, @NonNull String key) {
        Object value = map.get(key);
        if (!(value instanceof ExpiringRepository)) {
            return null;
        }
        ExpiringRepository entry = (ExpiringRepository) value;
        if (entry.isExpired(clock)) {
            map.remove(key);
            return null;
        }
        return entry.getValue();
    }

    static void put(@NonNull ConcurrentMap map, @NonNull Clock clock, @NonNull String key, @NonNull SdmxRepository value, @NonNull Duration ttl) {
        Objects.requireNonNull(value);
        map.put(key, ExpiringRepository.of(clock, ttl, value));
    }
}
