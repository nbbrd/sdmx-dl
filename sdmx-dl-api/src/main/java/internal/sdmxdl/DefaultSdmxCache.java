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
package internal.sdmxdl;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ext.SdmxCache;
import sdmxdl.repo.SdmxRepository;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class DefaultSdmxCache implements SdmxCache {

    @lombok.NonNull
    private final ConcurrentMap cache;

    @lombok.NonNull
    private final Clock clock;

    @Override
    public SdmxRepository get(String key) {
        return get(cache, clock, key);
    }

    @Override
    public void put(String key, SdmxRepository value, Duration ttl) {
        put(cache, clock, key, value, ttl);
    }

    @Nullable
    static SdmxRepository get(@NonNull ConcurrentMap cache, @NonNull Clock clock, @NonNull String key) {
        Object value = cache.get(key);
        if (!(value instanceof Entry)) {
            return null;
        }
        Entry entry = (Entry) value;
        if (entry.getExpirationTimeInMillis() <= clock.millis()) {
            cache.remove(key);
            return null;
        }
        return entry.getValue();
    }

    static void put(@NonNull ConcurrentMap cache, @NonNull Clock clock, @NonNull String key, @NonNull SdmxRepository value, @NonNull Duration ttl) {
        Objects.requireNonNull(value);
        cache.put(key, new Entry(clock.millis() + ttl.toMillis(), value));
    }

    @lombok.Value
    private static class Entry {

        long expirationTimeInMillis;
        SdmxRepository value;
    }
}
