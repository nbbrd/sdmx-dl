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
package sdmxdl.xml.stream;

import org.junit.jupiter.api.Test;
import sdmxdl.DataDetail;
import sdmxdl.Frequency;
import sdmxdl.Key;
import sdmxdl.ext.ObsParser;
import sdmxdl.samples.ByteSource;
import sdmxdl.samples.SdmxSource;
import sdmxdl.util.parser.DefaultObsParser;
import sdmxdl.util.parser.FreqFactory;
import sdmxdl.xml.DataCursor;
import sdmxdl.xml.DataCursorAssert;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class XMLStreamGenericDataCursorTest {

    @Test
    public void testGenericData20() throws Exception {
        ByteSource xml = SdmxSource.NBB_DATA;
        Key.Builder builder = Key.builder(asList("SUBJECT", "LOCATION", "FREQUENCY"));

        DataCursorAssert.assertCompliance(() -> {
            InputStream stream = xml.openStream();
            ObsParser obsParser = DefaultObsParser.builder().freqFactory(FreqFactory.sdmx20(FreqFactory.TIME_FORMAT_CONCEPT)).build();
            try {
                return XMLStreamGenericDataCursor.sdmx20(xif.createXMLStreamReader(stream), stream, builder, obsParser);
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
        }, Key.ALL, DataDetail.FULL);

        ObsParser obsParser = DefaultObsParser.builder().freqFactory(FreqFactory.sdmx20(FreqFactory.TIME_FORMAT_CONCEPT)).build();
        try (InputStream stream = xml.openStream();
             DataCursor o = XMLStreamGenericDataCursor.sdmx20(xif.createXMLStreamReader(stream), stream, builder, obsParser)) {
            int indexSeries = -1;
            while (o.nextSeries()) {
                switch (++indexSeries) {
                    case 0:
                        assertThat(o.getSeriesKey()).isEqualTo(Key.of("LOCSTL04", "AUS", "M"));
                        assertThat(o.getSeriesFrequency()).isEqualTo(Frequency.MONTHLY);
                        assertThat(o.getSeriesAttributes())
                                .hasSize(1)
                                .containsEntry(FreqFactory.TIME_FORMAT_CONCEPT, "P1M");
                        assertThat(o.getSeriesAttribute(FreqFactory.TIME_FORMAT_CONCEPT)).isEqualTo("P1M");
                        assertThat(o.getSeriesAttribute("hello")).isNull();
                        int indexObs = -1;
                        while (o.nextObs()) {
                            switch (++indexObs) {
                                case 0:
                                    assertThat(o.getObsPeriod()).isEqualTo("1966-02-01T00:00:00");
                                    assertThat(o.getObsValue()).isEqualTo(98.68823);
                                    break;
                                case 188:
                                    assertThat(o.getObsPeriod()).isEqualTo("1970-08-01T00:00:00");
                                    assertThat(o.getObsValue()).isEqualTo(101.1945);
                                    break;
                                case 199:
                                    assertThat(o.getObsPeriod()).isEqualTo("1970-08-17T00:00:00");
                                    assertThat(o.getObsValue()).isEqualTo(93.7211);
                                    break;
                            }
                        }
                        assertThat(indexObs).isEqualTo(199);
                        break;
                }
            }
            assertThat(indexSeries).isEqualTo(0);
        }
    }

    @Test
    public void testGenericData21() throws Exception {
        ByteSource xml = SdmxSource.OTHER_GENERIC21;
        Key.Builder builder = Key.builder(asList("FREQ", "AME_REF_AREA", "AME_TRANSFORMATION", "AME_AGG_METHOD", "AME_UNIT", "AME_REFERENCE", "AME_ITEM"));

        DataCursorAssert.assertCompliance(() -> {
            InputStream stream = xml.openStream();
            ObsParser obsParser = DefaultObsParser.builder().freqFactory(FreqFactory.sdmx21(0)).build();
            try {
                return XMLStreamGenericDataCursor.sdmx21(xif.createXMLStreamReader(stream), stream, builder, obsParser);
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
        }, Key.ALL, DataDetail.FULL);

        ObsParser obsParser = DefaultObsParser.builder().freqFactory(FreqFactory.sdmx21(0)).build();
        try (InputStream stream = xml.openStream();
             DataCursor o = XMLStreamGenericDataCursor.sdmx21(xif.createXMLStreamReader(stream), stream, builder, obsParser)) {
            assertThat(o.nextSeries()).isTrue();
            assertThat(o.getSeriesKey()).isEqualTo(Key.of("A", "BEL", "1", "0", "0", "0", "OVGD"));
            assertThat(o.getSeriesFrequency()).isEqualTo(Frequency.ANNUAL);
            assertThat(o.getSeriesAttributes())
                    .hasSize(3)
                    .containsEntry("EXT_TITLE", "Belgium - Gross domestic product at 2010 market prices")
                    .containsEntry("TITLE_COMPL", "Belgium - Gross domestic product at 2010 market prices - Mrd EURO-BEF - AMECO data class: Data at constant prices")
                    .containsEntry("EXT_UNIT", "Mrd EURO-BEF");
            assertThat(o.getSeriesAttribute("EXT_UNIT")).isEqualTo("Mrd EURO-BEF");
            assertThat(o.getSeriesAttribute("hello")).isNull();
            int indexObs = -1;
            while (o.nextObs()) {
                switch (++indexObs) {
                    case 0:
                        assertThat(o.getObsPeriod()).isEqualTo("1960-01-01T00:00:00");
                        assertThat(o.getObsValue()).isEqualTo(92.0142);
                        break;
                    case 56:
                        assertThat(o.getObsPeriod()).isEqualTo("2016-01-01T00:00:00");
                        assertThat(o.getObsValue()).isEqualTo(386.5655);
                        break;
                }
            }
            assertThat(indexObs).isEqualTo(56);
            assertThat(o.nextSeries()).isFalse();
        }
    }

    @Test
    public void testGenericData21Bis() throws Exception {
        ByteSource xml = SdmxSource.ECB_DATA;
        Key.Builder builder = Key.builder(asList("FREQ", "AME_REF_AREA", "AME_TRANSFORMATION", "AME_AGG_METHOD", "AME_UNIT", "AME_REFERENCE", "AME_ITEM"));

        DataCursorAssert.assertCompliance(() -> {
            InputStream stream = xml.openStream();
            ObsParser obsParser = DefaultObsParser.builder().freqFactory(FreqFactory.sdmx21(0)).build();
            try {
                return XMLStreamGenericDataCursor.sdmx21(xif.createXMLStreamReader(stream), stream, builder, obsParser);
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
        }, Key.ALL, DataDetail.FULL);

        ObsParser obsParser = DefaultObsParser.builder().freqFactory(FreqFactory.sdmx21(0)).build();
        try (InputStream stream = xml.openStream();
             DataCursor o = XMLStreamGenericDataCursor.sdmx21(xif.createXMLStreamReader(stream), stream, builder, obsParser)) {
            int indexSeries = -1;
            while (o.nextSeries()) {
                switch (++indexSeries) {
                    case 0:
                        assertThat(o.getSeriesKey()).isEqualTo(Key.of("A", "DEU", "1", "0", "319", "0", "UBLGE"));
                        assertThat(o.getSeriesFrequency()).isEqualTo(Frequency.ANNUAL);
                        int indexObs = -1;
                        while (o.nextObs()) {
                            switch (++indexObs) {
                                case 0:
                                    assertThat(o.getObsPeriod()).isEqualTo("1991-01-01T00:00:00");
                                    assertThat(o.getObsValue()).isEqualTo(-2.8574221);
                                    break;
                                case 24:
                                    assertThat(o.getObsPeriod()).isEqualTo("2015-01-01T00:00:00");
                                    assertThat(o.getObsValue()).isEqualTo(-0.1420473);
                                    break;
                            }
                        }
                        assertThat(indexObs).isEqualTo(24);
                        break;
                    case 119:
                        assertThat(o.getSeriesKey()).isEqualTo(Key.of("A", "HRV", "1", "0", "0", "0", "ZUTN"));
                        assertThat(o.getSeriesFrequency()).isEqualTo(Frequency.ANNUAL);
                        assertThat(o.nextObs()).isFalse();
                        break;
                }
            }
            assertThat(indexSeries).isEqualTo(119);
        }
    }

    private final XMLInputFactory xif = StaxUtil.getInputFactoryWithoutNamespace();
}
