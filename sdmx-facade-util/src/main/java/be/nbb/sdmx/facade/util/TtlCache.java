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
package be.nbb.sdmx.facade.util;

import java.time.Clock;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
public final class TtlCache {

    @Nonnull
    public static TtlCache of(@Nonnull ConcurrentMap cache, @Nonnull Clock clock, @Nonnegative long ttlInMillis) {
        return new TtlCache(cache, clock, ttlInMillis);
    }

    private final ConcurrentMap cache;
    private final Clock clock;
    private final long ttlInMillis;

    private TtlCache(ConcurrentMap cache, Clock clock, long ttlInMillis) {
        this.cache = cache;
        this.clock = clock;
        this.ttlInMillis = ttlInMillis;
    }

    @Nullable
    public <T> T get(@Nonnull TypedId<T> key) {
        return get(cache, key, clock);
    }

    public <T> void put(@Nonnull TypedId<T> key, @Nonnull T value) {
        put(cache, key, value, ttlInMillis, clock);
    }

    @Nullable
    public static <X> X get(@Nonnull ConcurrentMap cache, @Nonnull Object key, @Nonnull Clock clock) {
        Entry entry = (Entry) cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.getExpirationTimeInMillis() <= clock.millis()) {
            cache.remove(key);
            return null;
        }
        return (X) entry.getValue();
    }

    public static void put(@Nonnull ConcurrentMap cache, @Nonnull Object key, @Nonnull Object value, @Nonnegative long ttlInMillis, @Nonnull Clock clock) {
        cache.put(key, new Entry(clock.millis() + ttlInMillis, value));
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    @lombok.Value
    private static class Entry {

        long expirationTimeInMillis;
        Object value;
    }
    //</editor-fold>
}
