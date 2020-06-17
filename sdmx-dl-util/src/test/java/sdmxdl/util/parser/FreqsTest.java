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

import sdmxdl.Frequency;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import sdmxdl.util.parser.Freqs;

/**
 *
 * @author Philippe Charles
 */
public class FreqsTest {

    @Test
    @SuppressWarnings("null")
    public void testParseByFreq() {
        assertThatNullPointerException().isThrownBy(() -> Freqs.parseByFreq(null));

        assertThat(Freqs.parseByFreq("A")).isEqualTo(Frequency.ANNUAL);
        assertThat(Freqs.parseByFreq("S")).isEqualTo(Frequency.HALF_YEARLY);
        assertThat(Freqs.parseByFreq("Q")).isEqualTo(Frequency.QUARTERLY);
        assertThat(Freqs.parseByFreq("M")).isEqualTo(Frequency.MONTHLY);
        assertThat(Freqs.parseByFreq("W")).isEqualTo(Frequency.WEEKLY);
        assertThat(Freqs.parseByFreq("D")).isEqualTo(Frequency.DAILY);
        assertThat(Freqs.parseByFreq("H")).isEqualTo(Frequency.HOURLY);
        assertThat(Freqs.parseByFreq("B")).isEqualTo(Frequency.DAILY_BUSINESS);
        assertThat(Freqs.parseByFreq("N")).isEqualTo(Frequency.MINUTELY);

        assertThat(Freqs.parseByFreq("A5")).isEqualTo(Frequency.ANNUAL);
        assertThat(Freqs.parseByFreq("M2")).isEqualTo(Frequency.MONTHLY);
        assertThat(Freqs.parseByFreq("W6")).isEqualTo(Frequency.WEEKLY);

        assertThat(Freqs.parseByFreq("")).isEqualTo(Frequency.UNDEFINED);
        assertThat(Freqs.parseByFreq("A0")).isEqualTo(Frequency.UNDEFINED);
        assertThat(Freqs.parseByFreq("A1")).isEqualTo(Frequency.UNDEFINED);
    }
}
