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

import be.nbb.sdmx.facade.TimeFormat;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ObsParserTest {

    @Test
    public void testGetPeriod() {
        ObsParser p = new ObsParser();

        p.setTimeFormat(TimeFormat.YEARLY);
        assertThat(p.periodString("2001").getPeriod()).isEqualTo("2001-01-01");
        assertThat(p.periodString("2001-01").getPeriod()).isEqualTo("2001-01-01");
        assertThat(p.periodString("2001-A1").getPeriod()).isEqualTo("2001-01-01");
        assertThat(p.periodString("2001-02").getPeriod()).isNull();
        assertThat(p.periodString("2001-01-01").getPeriod()).isNull();
        assertThat(p.periodString("hello").getPeriod()).isNull();
        assertThat(p.periodString("").getPeriod()).isNull();

        p.setTimeFormat(TimeFormat.HALF_YEARLY);
        assertThat(p.periodString("2001-01").getPeriod()).isEqualTo("2001-01-01");
        assertThat(p.periodString("2001-07").getPeriod()).isEqualTo("2001-07-01");
        assertThat(p.periodString("2001-S1").getPeriod()).isEqualTo("2001-01-01");
        assertThat(p.periodString("2001-S2").getPeriod()).isEqualTo("2001-07-01");
        assertThat(p.periodString("2001S2").getPeriod()).isEqualTo("2001-07-01");
        assertThat(p.periodString("2001S0").getPeriod()).isNull();
        assertThat(p.periodString("2001S3").getPeriod()).isNull();
        assertThat(p.periodString("hello").getPeriod()).isNull();
        assertThat(p.periodString("").getPeriod()).isNull();

        p.setTimeFormat(TimeFormat.QUADRI_MONTHLY);
        assertThat(p.periodString("2001-01").getPeriod()).isEqualTo("2001-01-01");
        assertThat(p.periodString("2001-05").getPeriod()).isEqualTo("2001-05-01");
        assertThat(p.periodString("2001-T1").getPeriod()).isEqualTo("2001-01-01");
        assertThat(p.periodString("2001-T2").getPeriod()).isEqualTo("2001-05-01");
        assertThat(p.periodString("2001T2").getPeriod()).isEqualTo("2001-05-01");
        assertThat(p.periodString("2001T0").getPeriod()).isNull();
        assertThat(p.periodString("hello").getPeriod()).isNull();
        assertThat(p.periodString("").getPeriod()).isNull();

        p.setTimeFormat(TimeFormat.QUARTERLY);
        assertThat(p.periodString("2001-01").getPeriod()).isEqualTo("2001-01-01");
        assertThat(p.periodString("2001-04").getPeriod()).isEqualTo("2001-04-01");
        assertThat(p.periodString("2001-Q1").getPeriod()).isEqualTo("2001-01-01");
        assertThat(p.periodString("2001-Q2").getPeriod()).isEqualTo("2001-04-01");
        assertThat(p.periodString("2001Q2").getPeriod()).isEqualTo("2001-04-01");
        assertThat(p.periodString("2001-Q0").getPeriod()).isNull();
        assertThat(p.periodString("hello").getPeriod()).isNull();
        assertThat(p.periodString("").getPeriod()).isNull();

        p.setTimeFormat(TimeFormat.MONTHLY);
        assertThat(p.periodString("2001-01").getPeriod()).isEqualTo("2001-01-01");
        assertThat(p.periodString("2001-02").getPeriod()).isEqualTo("2001-02-01");
        assertThat(p.periodString("2001-M1").getPeriod()).isEqualTo("2001-01-01");
        assertThat(p.periodString("2001M1").getPeriod()).isEqualTo("2001-01-01");
        assertThat(p.periodString("2001-M0").getPeriod()).isNull();
        assertThat(p.periodString("hello").getPeriod()).isNull();
        assertThat(p.periodString("").getPeriod()).isNull();
    }
}
