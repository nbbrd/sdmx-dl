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
package internal.sdmxld.connectors;

import _test.sdmxdl.connectors.samples.ConnectorsResource;
import internal.sdmxdl.connectors.Connectors;
import internal.sdmxdl.connectors.PortableTimeSeriesCursor;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.util.LanguagePriorityList;
import org.junit.BeforeClass;
import org.junit.Test;
import sdmxdl.*;
import sdmxdl.samples.SdmxSource;
import sdmxdl.tck.DataCursorAssert;
import sdmxdl.util.parser.ObsFactories;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class PortableTimeSeriesCursorTest {

    static DataFlowStructure DSD;
    static List<PortableTimeSeries<Double>> DATA;

    @BeforeClass
    public static void beforeClass() throws IOException {
        LanguagePriorityList l = LanguagePriorityList.parse("en");
        DSD = ConnectorsResource.struct21(SdmxSource.ECB_DATA_STRUCTURE, l).get(0);
        DATA = ConnectorsResource.data21(SdmxSource.ECB_DATA, DSD, l);
    }

    @Test
    public void test() throws IOException {
        try (DataCursor c = PortableTimeSeriesCursor.of(DATA, ObsFactories.SDMX21, Connectors.toStructure(DSD))) {
            assertThat(c.toStream())
                    .hasSize(120)
                    .allMatch(o -> o.getFreq().equals(Frequency.ANNUAL))
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
        DataCursorAssert.assertCompliance(
                () -> PortableTimeSeriesCursor.of(DATA, ObsFactories.SDMX21, Connectors.toStructure(DSD)),
                Key.ALL, DataFilter.FULL
        );
    }
}
