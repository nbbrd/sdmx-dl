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

import org.junit.Test;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.tck.SdmxCacheAssert;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class MapCacheTest {

    @Test
    public void testCompliance() {
        SdmxCacheAssert.assertCompliance(MapCache.of());
    }

    @Test
    public void testGet() {
        ConcurrentHashMap map = new ConcurrentHashMap();

        assertThat(MapCache.get(map, clock(1000), "KEY1"))
                .as("Empty map should return null")
                .isNull();

        MapCache.put(map, clock(1000), "KEY1", r1, Duration.ofMillis(10));
        assertThat(MapCache.get(map, clock(1009), "KEY1"))
                .as("Non-expired key should return value")
                .isEqualTo(r1);

        assertThat(MapCache.get(map, clock(1010), "KEY1"))
                .as("Expired key should return null")
                .isNull();
        assertThat(map)
                .as("Expired key should be evicted")
                .doesNotContainKey("KEY1");

        assertThat(MapCache.get(map, clock(1009), "KEY2"))
                .as("Non-existing key should return null")
                .isNull();

        MapCache.put(map, clock(1009), "KEY1", r2, Duration.ofMillis(10));
        assertThat(MapCache.get(map, clock(1010), "KEY1"))
                .as("Updated key should return updated value")
                .isEqualTo(r2);
    }

    private final SdmxRepository r1 = SdmxRepository.builder().name("r1").build();
    private final SdmxRepository r2 = SdmxRepository.builder().name("r2").build();

    private static Clock clock(long value) {
        return Clock.fixed(Instant.ofEpochMilli(value), ZoneId.systemDefault());
    }
}
