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
package sdmxdl.provider.connectors.drivers;

import _test.sdmxdl.connectors.samples.ConnectorsResource;
import sdmxdl.provider.connectors.drivers.Connectors;
import sdmxdl.provider.connectors.drivers.PortableTimeSeriesCursor;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sdmxdl.Duration;
import sdmxdl.Key;
import sdmxdl.Obs;
import sdmxdl.TimeInterval;
import sdmxdl.format.DataCursor;
import sdmxdl.format.ObsParser;
import tests.sdmxdl.format.xml.SdmxXmlSources;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class PortableTimeSeriesCursorTest {

    static DataFlowStructure DSD;
    static List<PortableTimeSeries<Double>> DATA;

    @BeforeAll
    public static void beforeClass() throws IOException {
        List<Locale.LanguageRange> l = Locale.LanguageRange.parse("en");
        DSD = ConnectorsResource.struct21(SdmxXmlSources.ECB_DATA_STRUCTURE, l).get(0);
        DATA = ConnectorsResource.data21(SdmxXmlSources.ECB_DATA, DSD, l);
    }

    @Test
    public void test() throws IOException {
        Duration P1Y = Duration.parse("P1Y");
        try (DataCursor c = PortableTimeSeriesCursor.of(DATA, ObsParser::newDefault, Connectors.toStructure(DSD))) {
            assertThat(c.asStream())
                    .hasSize(120)
//                    .allMatch(o -> o.getFreq().equals(Frequency.ANNUAL))
                    .element(0)
                    .satisfies(o -> {
                        assertThat(o.getKey()).isEqualTo(Key.parse("A.DEU.1.0.319.0.UBLGE"));
                        assertThat(o.getMeta())
                                .hasSize(3)
                                .containsEntry("EXT_UNIT", "Percentage of GDP at market prices (excessive deficit procedure)")
                                .isNotEmpty();
                        assertThat(o.getObs())
                                .hasSize(25)
                                .startsWith(Obs.builder().period(TimeInterval.of(LocalDate.of(1991, 1, 1).atStartOfDay(), P1Y)).value(-2.8574221).meta("OBS_STATUS", "A").build())
                                .endsWith(Obs.builder().period(TimeInterval.of(LocalDate.of(2015, 1, 1).atStartOfDay(), P1Y)).value(-0.1420473).meta("OBS_STATUS", "A").build());
                    });
        }
    }

//    @Test
//    public void testCompliance() {
//        DataCursorAssert.assertCompliance(
//                () -> PortableTimeSeriesCursor.of(DATA, ObsFactories.SDMX21, Connectors.toStructure(DSD)),
//                Key.ALL, DataFilter.FULL
//        );
//    }
}
