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

import org.junit.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class DefaultSdmxCacheTest {

    @Test
    public void test() {
        ConcurrentMap cache = new ConcurrentHashMap();
        assertThat((String) DefaultSdmxCache.get(cache, "KEY1", of(1000))).isNull();
        DefaultSdmxCache.put(cache, "KEY1", "VALUE1", Duration.ofMillis(10), of(1000));
        assertThat((String) DefaultSdmxCache.get(cache, "KEY1", of(1009))).isEqualTo("VALUE1");
        assertThat((String) DefaultSdmxCache.get(cache, "KEY1", of(1010))).isNull();
        assertThat((String) DefaultSdmxCache.get(cache, "KEY2", of(1009))).isNull();
        DefaultSdmxCache.put(cache, "KEY1", "VALUE2", Duration.ofMillis(10), of(1009));
        assertThat((String) DefaultSdmxCache.get(cache, "KEY1", of(1010))).isEqualTo("VALUE2");
    }

    private static Clock of(long value) {
        return Clock.fixed(Instant.ofEpochMilli(value), ZoneId.systemDefault());
    }
}
