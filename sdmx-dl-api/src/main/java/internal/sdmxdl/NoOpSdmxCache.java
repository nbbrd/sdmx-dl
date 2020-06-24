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

import sdmxdl.SdmxCache;

import java.time.Duration;
import java.util.Objects;

/**
 * @author Philippe Charles
 */
public enum NoOpSdmxCache implements SdmxCache {

    INSTANCE;

    @Override
    public Object get(Object key) {
        Objects.requireNonNull(key);
        return null;
    }

    @Override
    public void put(Object key, Object value, Duration ttl) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Objects.requireNonNull(ttl);
    }
}
