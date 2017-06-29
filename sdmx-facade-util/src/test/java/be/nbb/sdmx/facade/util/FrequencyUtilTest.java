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
package be.nbb.sdmx.facade.util;

import be.nbb.sdmx.facade.Frequency;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class FrequencyUtilTest {

    @Test
    @SuppressWarnings("null")
    public void testParseByFreq() {
        assertThatThrownBy(() -> FreqUtil.parseByFreq(null)).isInstanceOf(NullPointerException.class);

        assertThat(FreqUtil.parseByFreq("A")).isEqualTo(Frequency.ANNUAL);
        assertThat(FreqUtil.parseByFreq("S")).isEqualTo(Frequency.HALF_YEARLY);
        assertThat(FreqUtil.parseByFreq("Q")).isEqualTo(Frequency.QUARTERLY);
        assertThat(FreqUtil.parseByFreq("M")).isEqualTo(Frequency.MONTHLY);
        assertThat(FreqUtil.parseByFreq("W")).isEqualTo(Frequency.WEEKLY);
        assertThat(FreqUtil.parseByFreq("D")).isEqualTo(Frequency.DAILY);
        assertThat(FreqUtil.parseByFreq("H")).isEqualTo(Frequency.HOURLY);
        assertThat(FreqUtil.parseByFreq("B")).isEqualTo(Frequency.DAILY_BUSINESS);
        assertThat(FreqUtil.parseByFreq("N")).isEqualTo(Frequency.MINUTELY);

        assertThat(FreqUtil.parseByFreq("A5")).isEqualTo(Frequency.ANNUAL);
        assertThat(FreqUtil.parseByFreq("M2")).isEqualTo(Frequency.MONTHLY);
        assertThat(FreqUtil.parseByFreq("W6")).isEqualTo(Frequency.WEEKLY);

        assertThat(FreqUtil.parseByFreq("")).isEqualTo(Frequency.UNDEFINED);
        assertThat(FreqUtil.parseByFreq("A0")).isEqualTo(Frequency.UNDEFINED);
        assertThat(FreqUtil.parseByFreq("A1")).isEqualTo(Frequency.UNDEFINED);
    }
}
