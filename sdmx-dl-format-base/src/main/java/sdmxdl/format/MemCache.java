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
package sdmxdl.format;

import lombok.AccessLevel;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.HasExpiration;
import sdmxdl.ext.Cache;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Philippe Charles
 */
@lombok.Builder(toBuilder = true)
public final class MemCache<V extends HasExpiration> implements Cache<V> {

    @lombok.Getter(AccessLevel.PACKAGE)
    @lombok.Builder.Default
    private final @NonNull Map<String, V> map = new HashMap<>();

    @lombok.Builder.Default
    private final @NonNull Clock clock = Clock.systemDefaultZone();

    @Override
    public @NonNull Clock getClock() {
        return clock;
    }

    @Override
    public @Nullable V get(@NonNull String key) {
        V result = map.get(key);
        if (result == null) {
            return null;
        }
        if (result.isExpired(clock)) {
            map.remove(key);
            return null;
        }
        return result;
    }

    @Override
    public void put(@NonNull String key, @NonNull V value) {
        map.put(key, value);
    }
}
