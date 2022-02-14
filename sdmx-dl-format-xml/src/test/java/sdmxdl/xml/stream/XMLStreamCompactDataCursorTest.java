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

import nbbrd.io.Resource;
import org.junit.jupiter.api.Test;
import sdmxdl.xml.DataCursor;
import sdmxdl.DataFilter;
import sdmxdl.Frequency;
import sdmxdl.Key;
import sdmxdl.ext.ObsParser;
import sdmxdl.samples.ByteSource;
import sdmxdl.samples.SdmxSource;
import sdmxdl.xml.DataCursorAssert;
import sdmxdl.util.parser.DefaultObsParser;
import sdmxdl.util.parser.FreqFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class XMLStreamCompactDataCursorTest {

    @Test
    public void testCompactData20() throws Exception {
        ByteSource xml = SdmxSource.OTHER_COMPACT20;
        Key.Builder builder = Key.builder(asList("FREQ", "COLLECTION", "VIS_CTY", "JD_TYPE", "JD_CATEGORY"));

        DataCursorAssert.assertCompliance(() -> {
            InputStream stream = xml.openStream();
            ObsParser obsParser = DefaultObsParser.builder().freqFactory(FreqFactory.sdmx20(FreqFactory.TIME_FORMAT_CONCEPT)).build();
            try {
                return new XMLStreamCompactDataCursor(xif.createXMLStreamReader(stream), stream, builder, obsParser, "TIME_PERIOD", "OBS_VALUE");
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
        }, Key.ALL, DataFilter.FULL);

        ObsParser obsParser = DefaultObsParser.builder().freqFactory(FreqFactory.sdmx20(FreqFactory.TIME_FORMAT_CONCEPT)).build();
        try (InputStream stream = xml.openStream();
             DataCursor o = new XMLStreamCompactDataCursor(xif.createXMLStreamReader(stream), stream, builder, obsParser, "TIME_PERIOD", "OBS_VALUE")) {
            int indexSeries = -1;
            while (o.nextSeries()) {
                switch (++indexSeries) {
                    case 0:
                        assertThat(o.getSeriesKey()).isEqualTo(Key.of("M", "B", "MX", "P", "A"));
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
                                    assertThat(o.getObsPeriod()).isEqualTo("2000-01-01T00:00:00");
                                    assertThat(o.getObsValue()).isEqualTo(3.14);
                                    break;
                                case 11:
                                    assertThat(o.getObsPeriod()).isEqualTo("2000-12-01T00:00:00");
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
        Key.Builder builder = Key.builder(asList("FREQ", "AME_REF_AREA", "AME_TRANSFORMATION", "AME_AGG_METHOD", "AME_UNIT", "AME_REFERENCE", "AME_ITEM"));

        DataCursorAssert.assertCompliance(() -> {
            InputStream stream = xml.openStream();
            ObsParser obsParser = DefaultObsParser.builder().freqFactory(FreqFactory.sdmx21(0)).build();
            try {
                return new XMLStreamCompactDataCursor(xif.createXMLStreamReader(stream), stream, builder, obsParser, "TIME_PERIOD", "OBS_VALUE");
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
        }, Key.ALL, DataFilter.FULL);

        ObsParser obsParser = DefaultObsParser.builder().freqFactory(FreqFactory.sdmx21(0)).build();
        try (InputStream stream = xml.openStream();
             DataCursor o = new XMLStreamCompactDataCursor(xif.createXMLStreamReader(stream), stream, builder, obsParser, "TIME_PERIOD", "OBS_VALUE")) {
            assertThat(o.nextSeries()).isTrue();
            assertThat(o.getSeriesKey()).isEqualTo(Key.of("A", "BEL", "1", "0", "0", "0", "OVGD"));
            assertThat(o.getSeriesFrequency()).isEqualTo(Frequency.ANNUAL);
            assertThat(o.getSeriesAttributes())
                    .hasSize(3)
                    .containsEntry("EXT_TITLE", "Belgium - Gross domestic product at 2010 market prices")
                    .containsEntry("EXT_UNIT", "Mrd EURO-BEF")
                    .containsEntry("TITLE_COMPL", "Belgium - Gross domestic product at 2010 market prices - Mrd EURO-BEF - AMECO data class: Data at constant prices");
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
    public void testMissingSeriesHeader() throws IOException, XMLStreamException {
        ByteSource xml = () -> Resource.getResourceAsStream(XMLStreamCompactDataCursorTest.class, "10100139_187.xml").orElseThrow(FileNotFoundException::new);
        Key.Builder builder = Key.builder(asList("A", "B"));
        ObsParser obsParser = DefaultObsParser.builder().build();

        try (InputStream stream = xml.openStream();
             DataCursor o = new XMLStreamCompactDataCursor(xif.createXMLStreamReader(stream), stream, builder, obsParser, "TIME_PERIOD", "OBS_VALUE")) {
            assertThat(o.nextSeries()).isFalse();
        }
    }

    private final XMLInputFactory xif = StaxUtil.getInputFactoryWithoutNamespace();
}
