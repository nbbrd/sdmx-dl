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
package be.nbb.sdmx.facade.connectors;

import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.Frequency;
import be.nbb.sdmx.facade.samples.SdmxSource;
import be.nbb.sdmx.facade.tck.DataCursorAssert;
import be.nbb.sdmx.facade.repo.Obs;
import be.nbb.sdmx.facade.repo.Series;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class DataCursorAdapterTest {

    static List<PortableTimeSeries> DATA;

    @BeforeClass
    public static void beforeClass() throws IOException {
        DataFlowStructure dsd = ConnectorsResource.struct21(SdmxSource.ECB_DATA_STRUCTURE).get(0);
        DATA = ConnectorsResource.data21(SdmxSource.ECB_DATA, dsd);
    }

    @Test
    public void test() throws IOException {
        try (DataCursorAdapter cursor = new DataCursorAdapter(DATA)) {
            assertThat(Series.copyOf(cursor))
                    .hasSize(120)
                    .allMatch(o -> o.getFrequency().equals(Frequency.ANNUAL))
                    .element(0)
                    .satisfies(o -> {
                        assertThat(o.getKey()).isEqualTo(Key.parse("A.DEU.1.0.319.0.UBLGE"));
                        assertThat(o.getMeta())
                                .hasSize(3)
                                .containsEntry("EXT_UNIT", "Percentage of GDP at market prices (excessive deficit procedure)")
                                .isNotEmpty();
                        assertThat(o.getObs())
                                .hasSize(25)
                                .startsWith(Obs.of(LocalDate.of(1991, 1, 1).atStartOfDay(), -2.8574221))
                                .endsWith(Obs.of(LocalDate.of(2015, 1, 1).atStartOfDay(), -0.1420473));
                    });
        }
    }

    @Test
    public void testCompliance() {
        DataCursorAssert.assertCompliance(() -> new DataCursorAdapter(DATA));
    }
}
