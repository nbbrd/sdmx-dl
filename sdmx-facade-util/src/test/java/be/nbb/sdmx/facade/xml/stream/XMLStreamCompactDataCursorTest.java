/*
 * Copyright 2015 National Bank of Belgium
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
package be.nbb.sdmx.facade.xml.stream;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.TimeFormat;
import be.nbb.sdmx.facade.samples.ByteSource;
import be.nbb.sdmx.facade.samples.SdmxSource;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author charphi
 */
public class XMLStreamCompactDataCursorTest {

    @Test
    public void testCompactData20() throws Exception {
        ByteSource xml = SdmxSource.OTHER_COMPACT20;
        Key.Builder builder = Key.builder("FREQ", "COLLECTION", "VIS_CTY", "JD_TYPE", "JD_CATEGORY");

        try (DataCursor o = new XMLStreamCompactDataCursor(xml.openXmlStream(), builder, TimeFormatParser.sdmx20(), "TIME_PERIOD", "OBS_VALUE")) {
            int indexSeries = -1;
            while (o.nextSeries()) {
                switch (++indexSeries) {
                    case 0:
                        assertThat(o.getSeriesKey()).isEqualTo(Key.of("M", "B", "MX", "P", "A"));
                        assertThat(o.getSeriesTimeFormat()).isEqualTo(TimeFormat.MONTHLY);
                        assertThat(o.getSeriesAttributes())
                                .hasSize(1)
                                .containsEntry("TIME_FORMAT", "P1M");
                        int indexObs = -1;
                        while (o.nextObs()) {
                            switch (++indexObs) {
                                case 0:
                                    assertThat(o.getObsPeriod()).isEqualTo("2000-01-01");
                                    assertThat(o.getObsValue()).isEqualTo(3.14);
                                    break;
                                case 11:
                                    assertThat(o.getObsPeriod()).isEqualTo("2000-12-01");
                                    assertThat(o.getObsValue()).isEqualTo(1.21);
                                    break;
                            }
                        }
                        assertThat(indexObs).isEqualTo(11);
                        break;
                }
            }
            assertThat(indexSeries).isEqualTo(3);
        }
    }

    @Test
    public void testCompactData21() throws Exception {
        ByteSource xml = SdmxSource.OTHER_COMPACT21;
        Key.Builder builder = Key.builder("FREQ", "AME_REF_AREA", "AME_TRANSFORMATION", "AME_AGG_METHOD", "AME_UNIT", "AME_REFERENCE", "AME_ITEM");

        try (DataCursor o = new XMLStreamCompactDataCursor(xml.openXmlStream(), builder, TimeFormatParser.sdmx21(0), "TIME_PERIOD", "OBS_VALUE")) {
            assertThat(o.nextSeries()).isTrue();
            assertThat(o.getSeriesKey()).isEqualTo(Key.of("A", "BEL", "1", "0", "0", "0", "OVGD"));
            assertThat(o.getSeriesTimeFormat()).isEqualTo(TimeFormat.YEARLY);
            assertThat(o.getSeriesAttributes())
                    .hasSize(3)
                    .containsEntry("EXT_TITLE", "Belgium - Gross domestic product at 2010 market prices")
                    .containsEntry("EXT_UNIT", "Mrd EURO-BEF")
                    .containsEntry("TITLE_COMPL", "Belgium - Gross domestic product at 2010 market prices - Mrd EURO-BEF - AMECO data class: Data at constant prices");
            int indexObs = -1;
            while (o.nextObs()) {
                switch (++indexObs) {
                    case 0:
                        assertThat(o.getObsPeriod()).isEqualTo("1960-01-01");
                        assertThat(o.getObsValue()).isEqualTo(92.0142);
                        break;
                    case 56:
                        assertThat(o.getObsPeriod()).isEqualTo("2016-01-01");
                        assertThat(o.getObsValue()).isEqualTo(386.5655);
                        break;
                }
            }
            assertThat(indexObs).isEqualTo(56);
            assertThat(o.nextSeries()).isFalse();
        }
    }
}
