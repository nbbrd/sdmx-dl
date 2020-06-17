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
package sdmxdl.util.parser;

import sdmxdl.Frequency;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import sdmxdl.util.parser.ObsParser;

/**
 *
 * @author Philippe Charles
 */
public class ObsParserTest {

    @Test
    public void testPeriod() {
        ObsParser p = ObsParser.standard();

        p.frequency(Frequency.ANNUAL);
        assertThat(p.period("2001").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-01").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-A1").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-02").parsePeriod()).isNull();
        assertThat(p.period("2001-01-01").parsePeriod()).isNull();
        assertThat(p.period("hello").parsePeriod()).isNull();
        assertThat(p.period("").parsePeriod()).isNull();

        p.frequency(Frequency.HALF_YEARLY);
        assertThat(p.period("2001-01").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-07").parsePeriod()).isEqualTo("2001-07-01T00:00:00");
        assertThat(p.period("2001-S1").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-S2").parsePeriod()).isEqualTo("2001-07-01T00:00:00");
        assertThat(p.period("2001S2").parsePeriod()).isEqualTo("2001-07-01T00:00:00");
        assertThat(p.period("2001S0").parsePeriod()).isNull();
        assertThat(p.period("2001S3").parsePeriod()).isNull();
        assertThat(p.period("hello").parsePeriod()).isNull();
        assertThat(p.period("").parsePeriod()).isNull();

        p.frequency(Frequency.QUARTERLY);
        assertThat(p.period("2001-01").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-04").parsePeriod()).isEqualTo("2001-04-01T00:00:00");
        assertThat(p.period("2001-Q1").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-Q2").parsePeriod()).isEqualTo("2001-04-01T00:00:00");
        assertThat(p.period("2001Q2").parsePeriod()).isEqualTo("2001-04-01T00:00:00");
        assertThat(p.period("2001-Q0").parsePeriod()).isNull();
        assertThat(p.period("hello").parsePeriod()).isNull();
        assertThat(p.period("").parsePeriod()).isNull();

        p.frequency(Frequency.MONTHLY);
        assertThat(p.period("2001-01").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-02").parsePeriod()).isEqualTo("2001-02-01T00:00:00");
        assertThat(p.period("2001-M1").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001M1").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-M0").parsePeriod()).isNull();
        assertThat(p.period("hello").parsePeriod()).isNull();
        assertThat(p.period("").parsePeriod()).isNull();
    }
}
