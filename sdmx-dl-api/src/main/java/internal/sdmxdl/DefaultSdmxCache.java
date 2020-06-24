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

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.SdmxCache;
import java.time.Duration;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class DefaultSdmxCache implements SdmxCache {

    @lombok.NonNull
    private final ConcurrentMap cache;

    @lombok.NonNull
    private final Clock clock;

    @Override
    public Object get(Object key) {
        return get(cache, key, clock);
    }

    @Override
    public void put(Object key, Object value, Duration ttl) {
        put(cache, key, value, ttl, clock);
    }

    @Nullable
    static Object get(@NonNull ConcurrentMap cache, @NonNull Object key, @NonNull Clock clock) {
        Entry entry = (Entry) cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.getExpirationTimeInMillis() <= clock.millis()) {
            cache.remove(key);
            return null;
        }
        return entry.getValue();
    }

    static void put(@NonNull ConcurrentMap cache, @NonNull Object key, @NonNull Object value, @NonNull Duration ttl, @NonNull Clock clock) {
        Objects.requireNonNull(value);
        cache.put(key, new Entry(clock.millis() + ttl.toMillis(), value));
    }

    @lombok.Value
    private static class Entry {

        long expirationTimeInMillis;
        Object value;
    }
}
