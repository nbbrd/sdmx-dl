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

import org.junit.jupiter.api.Test;
import sdmxdl.DataRepository;
import tests.sdmxdl.ext.CacheAssert;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class MemCacheTest {

    @Test
    public void testCompliance() {
        CacheAssert.assertCompliance(MemCache.builder().build());
    }

    @Test
    public void testGetRepository() {
        Map<String, DataRepository> map = new HashMap<>();

        assertThat(MemCache.getRepository(map, clock(1000), "KEY1"))
                .as("Empty map should return null")
                .isNull();

        DataRepository r1000 = DataRepository
                .builder()
                .name("r1")
                .ttl(clock(1000).instant(), Duration.ofMillis(10))
                .build();
        map.put("KEY1", r1000);
        assertThat(MemCache.getRepository(map, clock(1009), "KEY1"))
                .as("Non-expired key should return value")
                .isEqualTo(r1000);

        assertThat(MemCache.getRepository(map, clock(1010), "KEY1"))
                .as("Expired key should return null")
                .isNull();
        assertThat(map)
                .as("Expired key should be evicted")
                .doesNotContainKey("KEY1");

        assertThat(MemCache.getRepository(map, clock(1009), "KEY2"))
                .as("Non-existing key should return null")
                .isNull();

        DataRepository r1009 = DataRepository
                .builder()
                .name("r2")
                .ttl(clock(1009).instant(), Duration.ofMillis(10))
                .build();
        map.put("KEY1", r1009);
        assertThat(MemCache.getRepository(map, clock(1010), "KEY1"))
                .as("Updated key should return updated value")
                .isEqualTo(r1009);
    }

    @Test
    public void testMapFactories() {
        assertThat(MemCache.builder().build())
                .satisfies(x -> {
                    assertThat(x.getRepositories())
                            .isInstanceOf(HashMap.class);

                    assertThat(x.getWebMonitors())
                            .isInstanceOf(HashMap.class);
                });

        assertThat(MemCache.builder().repositories(new TreeMap<>()).webMonitors(new ConcurrentHashMap<>()).build())
                .satisfies(x -> {
                    assertThat(x.getRepositories())
                            .isInstanceOf(TreeMap.class);

                    assertThat(x.getWebMonitors())
                            .isInstanceOf(ConcurrentHashMap.class);
                });
    }

    private static Clock clock(long value) {
        return Clock.fixed(Instant.ofEpochMilli(value), ZoneId.systemDefault());
    }
}
