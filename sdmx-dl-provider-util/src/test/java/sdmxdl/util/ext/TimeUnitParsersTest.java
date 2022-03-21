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
package sdmxdl.util.ext;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class TimeUnitParsersTest {

    @Test
    @SuppressWarnings("null")
    public void testParseByFreq() {
        assertThat(TimeUnitParsers.parseFreqCode(null)).isNull();

        assertThat(TimeUnitParsers.parseFreqCode("A")).isEqualTo(SeriesMetaFactory.ANNUAL);
        assertThat(TimeUnitParsers.parseFreqCode("S")).isEqualTo(SeriesMetaFactory.HALF_YEARLY);
        assertThat(TimeUnitParsers.parseFreqCode("Q")).isEqualTo(SeriesMetaFactory.QUARTERLY);
        assertThat(TimeUnitParsers.parseFreqCode("M")).isEqualTo(SeriesMetaFactory.MONTHLY);
        assertThat(TimeUnitParsers.parseFreqCode("W")).isEqualTo(SeriesMetaFactory.WEEKLY);
        assertThat(TimeUnitParsers.parseFreqCode("D")).isEqualTo(SeriesMetaFactory.DAILY);
        assertThat(TimeUnitParsers.parseFreqCode("H")).isEqualTo(SeriesMetaFactory.HOURLY);
        assertThat(TimeUnitParsers.parseFreqCode("B")).isEqualTo(SeriesMetaFactory.DAILY_BUSINESS);
        assertThat(TimeUnitParsers.parseFreqCode("N")).isEqualTo(SeriesMetaFactory.MINUTELY);

        assertThat(TimeUnitParsers.parseFreqCode("A5")).isEqualTo(SeriesMetaFactory.ANNUAL);
        assertThat(TimeUnitParsers.parseFreqCode("M2")).isEqualTo(SeriesMetaFactory.MONTHLY);
        assertThat(TimeUnitParsers.parseFreqCode("W6")).isEqualTo(SeriesMetaFactory.WEEKLY);

        assertThat(TimeUnitParsers.parseFreqCode("")).isNull();
        assertThat(TimeUnitParsers.parseFreqCode("A0")).isNull();
        assertThat(TimeUnitParsers.parseFreqCode("A1")).isNull();
    }
}
