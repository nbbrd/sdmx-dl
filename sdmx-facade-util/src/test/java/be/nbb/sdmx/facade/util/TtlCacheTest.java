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

import be.nbb.sdmx.facade.util.TtlCache.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TtlCacheTest {

    @Test
    public void test() {
        ConcurrentMap cache = new ConcurrentHashMap();
        assertThat(TtlCache.get(cache, "KEY1", of(1000))).isNull();
        TtlCache.put(cache, "KEY1", "VALUE1", 10, of(1000));
        assertThat(TtlCache.get(cache, "KEY1", of(1009))).isEqualTo("VALUE1");
        assertThat(TtlCache.get(cache, "KEY1", of(1010))).isNull();
        assertThat(TtlCache.get(cache, "KEY2", of(1009))).isNull();
        TtlCache.put(cache, "KEY1", "VALUE2", 10, of(1009));
        assertThat(TtlCache.get(cache, "KEY1", of(1010))).isEqualTo("VALUE2");
    }

    private static Clock of(final long value) {
        return new Clock() {
            @Override
            public long currentTimeMillis() {
                return value;
            }
        };
    }
}
