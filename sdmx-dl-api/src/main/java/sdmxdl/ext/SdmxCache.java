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
package sdmxdl.ext;

import internal.sdmxdl.DefaultSdmxCache;
import internal.sdmxdl.NoOpSdmxCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.repo.SdmxRepository;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Philippe Charles
 */
//@ThreadSafe
public interface SdmxCache {

    @Nullable
    SdmxRepository get(@NonNull String key);

    void put(@NonNull String key, @NonNull SdmxRepository value, @NonNull Duration ttl);

    @NonNull
    static SdmxCache of(@NonNull ConcurrentMap cache, @NonNull Clock clock) {
        return new DefaultSdmxCache(cache, clock);
    }

    @NonNull
    static SdmxCache of() {
        return new DefaultSdmxCache(new ConcurrentHashMap(), Clock.systemDefaultZone());
    }

    @NonNull
    static SdmxCache noOp() {
        return NoOpSdmxCache.INSTANCE;
    }
}
