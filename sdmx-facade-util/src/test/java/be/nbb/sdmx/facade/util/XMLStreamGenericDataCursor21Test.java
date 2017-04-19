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
package be.nbb.sdmx.facade.util;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.TimeFormat;
import be.nbb.sdmx.facade.samples.ByteSource;
import be.nbb.sdmx.facade.samples.SdmxSource;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author charphi
 */
public class XMLStreamGenericDataCursor21Test {

    @Test
    public void testGenericData21() throws Exception {
        ByteSource xml = SdmxSource.OTHER_GENERIC21;
        Key.Builder builder = Key.builder("FREQ", "AME_REF_AREA", "AME_TRANSFORMATION", "AME_AGG_METHOD", "AME_UNIT", "AME_REFERENCE", "AME_ITEM");

        try (DataCursor o = new XMLStreamGenericDataCursor21(xml.openXmlStream(), builder, 0)) {
            assertThat(o.nextSeries()).isTrue();
            assertThat(o.getSeriesKey()).isEqualTo(Key.of("A", "BEL", "1", "0", "0", "0", "OVGD"));
            assertThat(o.getSeriesTimeFormat()).isEqualTo(TimeFormat.YEARLY);
            assertThat(o.getSeriesAttributes())
                    .hasSize(3)
                    .containsEntry("EXT_TITLE", "Belgium - Gross domestic product at 2010 market prices")
                    .containsEntry("TITLE_COMPL", "Belgium - Gross domestic product at 2010 market prices - Mrd EURO-BEF - AMECO data class: Data at constant prices")
                    .containsEntry("EXT_UNIT", "Mrd EURO-BEF");
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

    @Test
    public void testCursor() throws Exception {
        ByteSource xml = SdmxSource.ECB_DATA;
        Key.Builder builder = Key.builder("FREQ", "AME_REF_AREA", "AME_TRANSFORMATION", "AME_AGG_METHOD", "AME_UNIT", "AME_REFERENCE", "AME_ITEM");

        try (DataCursor o = new XMLStreamGenericDataCursor21(xml.openXmlStream(), builder, 0)) {
            int indexSeries = -1;
            while (o.nextSeries()) {
                switch (++indexSeries) {
                    case 0:
                        assertThat(o.getSeriesKey()).isEqualTo(Key.of("A", "DEU", "1", "0", "319", "0", "UBLGE"));
                        assertThat(o.getSeriesTimeFormat()).isEqualTo(TimeFormat.YEARLY);
                        int indexObs = -1;
                        while (o.nextObs()) {
                            switch (++indexObs) {
                                case 0:
                                    assertThat(o.getObsPeriod()).isEqualTo("1991-01-01");
                                    assertThat(o.getObsValue()).isEqualTo(-2.8574221);
                                    break;
                                case 24:
                                    assertThat(o.getObsPeriod()).isEqualTo("2015-01-01");
                                    assertThat(o.getObsValue()).isEqualTo(-0.1420473);
                                    break;
                            }
                        }
                        assertThat(indexObs).isEqualTo(24);
                        break;
                    case 119:
                        assertThat(o.getSeriesKey()).isEqualTo(Key.of("A", "HRV", "1", "0", "0", "0", "ZUTN"));
                        assertThat(o.getSeriesTimeFormat()).isEqualTo(TimeFormat.YEARLY);
                        assertThat(o.nextObs()).isFalse();
                        break;
                }
            }
            assertThat(indexSeries).isEqualTo(119);
        }
    }
}
