/*
 * Copyright 2019 National Bank of Belgium
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

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.HasExpiration;
import sdmxdl.ext.Cache;

import java.time.Clock;

/**
 * @author Philippe Charles
 */
public enum NoOpCache implements Cache<HasExpiration> {

    INSTANCE;

    @Override
    public @NonNull Clock getClock() {
        return Clock.systemDefaultZone();
    }

    @Nullable
    @Override
    public HasExpiration get(@NonNull String key) {
        return null;
    }

    @Override
    public void put(@NonNull String key, @NonNull HasExpiration value) {
    }
}
