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

import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class TtlCache {

    @Nullable
    public static <X> X get(@Nonnull ConcurrentMap cache, @Nonnull Object key, @Nonnull Clock clock) {
        Entry entry = (Entry) cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.getExpirationTimeInMillis() <= clock.currentTimeMillis()) {
            cache.remove(key);
            return null;
        }
        return (X) entry.getValue();
    }

    public void put(@Nonnull ConcurrentMap cache, @Nonnull Object key, @Nonnull Object value, @Nonnegative long ttlInMillis, @Nonnull Clock clock) {
        cache.put(key, new Entry(clock.currentTimeMillis() + ttlInMillis, value));
    }

    public interface Clock {

        long currentTimeMillis();
    }

    @Nonnull
    public static Clock systemClock() {
        return SystemClock.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private enum SystemClock implements Clock {

        INSTANCE;

        @Override
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    }

    @lombok.Value
    private static class Entry {

        long expirationTimeInMillis;
        Object value;
    }
    //</editor-fold>
}
