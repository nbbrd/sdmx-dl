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
package sdmxdl.util.parser;

import org.junit.Test;
import sdmxdl.Frequency;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class FreqParsersTest {

    @Test
    @SuppressWarnings("null")
    public void testParseByFreq() {
        assertThat(FreqParsers.parseFreqCode(null)).isNull();

        assertThat(FreqParsers.parseFreqCode("A")).isEqualTo(Frequency.ANNUAL);
        assertThat(FreqParsers.parseFreqCode("S")).isEqualTo(Frequency.HALF_YEARLY);
        assertThat(FreqParsers.parseFreqCode("Q")).isEqualTo(Frequency.QUARTERLY);
        assertThat(FreqParsers.parseFreqCode("M")).isEqualTo(Frequency.MONTHLY);
        assertThat(FreqParsers.parseFreqCode("W")).isEqualTo(Frequency.WEEKLY);
        assertThat(FreqParsers.parseFreqCode("D")).isEqualTo(Frequency.DAILY);
        assertThat(FreqParsers.parseFreqCode("H")).isEqualTo(Frequency.HOURLY);
        assertThat(FreqParsers.parseFreqCode("B")).isEqualTo(Frequency.DAILY_BUSINESS);
        assertThat(FreqParsers.parseFreqCode("N")).isEqualTo(Frequency.MINUTELY);

        assertThat(FreqParsers.parseFreqCode("A5")).isEqualTo(Frequency.ANNUAL);
        assertThat(FreqParsers.parseFreqCode("M2")).isEqualTo(Frequency.MONTHLY);
        assertThat(FreqParsers.parseFreqCode("W6")).isEqualTo(Frequency.WEEKLY);

        assertThat(FreqParsers.parseFreqCode("")).isNull();
        assertThat(FreqParsers.parseFreqCode("A0")).isNull();
        assertThat(FreqParsers.parseFreqCode("A1")).isNull();
    }
}
