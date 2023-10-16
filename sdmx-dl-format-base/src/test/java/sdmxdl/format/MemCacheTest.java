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
import sdmxdl.web.MonitorReports;
import tests.sdmxdl.ext.FakeClock;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.sdmxdl.ext.CacheAssert.assertMonitorCompliance;
import static tests.sdmxdl.ext.CacheAssert.assertRepositoryCompliance;

/**
 * @author Philippe Charles
 */
public class MemCacheTest {

    @Test
    public void testCompliance() {
        assertMonitorCompliance(MemCache.<MonitorReports>builder().build());
        assertRepositoryCompliance(MemCache.<DataRepository>builder().build());
    }

    @Test
    public void testGet() {
        Map<String, DataRepository> map = new HashMap<>();

        FakeClock clock = new FakeClock();

        MemCache<DataRepository> x = MemCache
                .<DataRepository>builder()
                .map(map)
                .clock(clock)
                .build();

        clock.set(1000);
        assertThat(x.get("KEY1"))
                .as("Empty map should return null")
                .isNull();

        DataRepository r1000 = DataRepository
                .builder()
                .name("r1")
                .ttl(clock(1000).instant(), Duration.ofMillis(10))
                .build();
        map.put("KEY1", r1000);
        clock.set(1009);
        assertThat(x.get("KEY1"))
                .as("Non-expired key should return value")
                .isEqualTo(r1000);

        clock.set(1010);
        assertThat(x.get("KEY1"))
                .as("Expired key should return null")
                .isNull();
        assertThat(map)
                .as("Expired key should be evicted")
                .doesNotContainKey("KEY1");

        clock.set(1009);
        assertThat(x.get("KEY2"))
                .as("Non-existing key should return null")
                .isNull();

        DataRepository r1009 = DataRepository
                .builder()
                .name("r2")
                .ttl(clock(1009).instant(), Duration.ofMillis(10))
                .build();
        map.put("KEY1", r1009);
        clock.set(1010);
        assertThat(x.get("KEY1"))
                .as("Updated key should return updated value")
                .isEqualTo(r1009);
    }

    @Test
    public void testMapFactories() {
        assertThat(MemCache.builder().build().getMap())
                .isInstanceOf(HashMap.class);

        assertThat(MemCache.builder().map(new TreeMap<>()).build().getMap())
                .isInstanceOf(TreeMap.class);
    }

    private static Clock clock(long value) {
        return Clock.fixed(Instant.ofEpochMilli(value), ZoneId.systemDefault());
    }
}
