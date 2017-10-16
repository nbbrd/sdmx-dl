/*
 * Copyright 2017 National Bank of Belgium
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
package be.nbb.sdmx.facade.repo;

import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.tck.ConnectionAssert;
import be.nbb.sdmx.facade.tck.DataCursorAssert;
import java.time.LocalDateTime;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SdmxRepositoryTest {

    @Test
    public void testCompliance() {
        ConnectionAssert.assertCompliance(repo::asConnection, xyz);
    }

    @Test
    public void testDataCursorCompliance() {
        DataCursorAssert.assertCompliance(() -> Series.asCursor(Collections.singletonList(series), Key.ALL));
    }

    @Test
    public void testBuilder() {
        assertThat(SdmxRepository.builder().name("test").data(xyz, series).build().isSeriesKeysOnlySupported()).isTrue();
    }

    private final DataflowRef xyz = DataflowRef.parse("XYZ");
    private final Series series = Series.builder().key(Key.of("BE")).frequency(Frequency.MONTHLY).obs(Obs.of(LocalDateTime.now(), Math.PI)).meta("hello", "world").build();
    private final SdmxRepository repo = SdmxRepository.builder().name("test").data(xyz, series).build();
}
