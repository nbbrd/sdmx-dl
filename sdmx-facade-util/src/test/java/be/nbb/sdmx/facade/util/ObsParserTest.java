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
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Philippe Charles
 */
public class ObsParserTest {

    final QuickCalendar c = new QuickCalendar();

    @Test
    public void testGetPeriod() {
        ObsParser p = new ObsParser();

        p.setTimeFormat(TimeFormat.YEARLY);
        assertEquals(c.date(2001, 0, 1), p.periodString("2001").getPeriod());
        assertEquals(c.date(2001, 0, 1), p.periodString("2001-01").getPeriod());
        assertEquals(c.date(2001, 0, 1), p.periodString("2001-A1").getPeriod());
        assertNull(p.periodString("2001-02").getPeriod());
        assertNull(p.periodString("2001-01-01").getPeriod());
        assertNull(p.periodString("hello").getPeriod());
        assertNull(p.periodString("").getPeriod());

        p.setTimeFormat(TimeFormat.HALF_YEARLY);
        assertEquals(c.date(2001, 0, 1), p.periodString("2001-01").getPeriod());
        assertEquals(c.date(2001, 6, 1), p.periodString("2001-07").getPeriod());
        assertEquals(c.date(2001, 0, 1), p.periodString("2001-S1").getPeriod());
        assertEquals(c.date(2001, 6, 1), p.periodString("2001-S2").getPeriod());
        assertEquals(c.date(2001, 6, 1), p.periodString("2001S2").getPeriod());
        assertNull(p.periodString("2001S0").getPeriod());
        assertNull(p.periodString("2001S3").getPeriod());
        assertNull(p.periodString("hello").getPeriod());
        assertNull(p.periodString("").getPeriod());

        p.setTimeFormat(TimeFormat.QUADRI_MONTHLY);
        assertEquals(c.date(2001, 0, 1), p.periodString("2001-01").getPeriod());
        assertEquals(c.date(2001, 4, 1), p.periodString("2001-05").getPeriod());
        assertEquals(c.date(2001, 0, 1), p.periodString("2001-T1").getPeriod());
        assertEquals(c.date(2001, 4, 1), p.periodString("2001-T2").getPeriod());
        assertEquals(c.date(2001, 4, 1), p.periodString("2001T2").getPeriod());
        assertNull(p.periodString("2001T0").getPeriod());
        assertNull(p.periodString("hello").getPeriod());
        assertNull(p.periodString("").getPeriod());

        p.setTimeFormat(TimeFormat.QUARTERLY);
        assertEquals(c.date(2001, 0, 1), p.periodString("2001-01").getPeriod());
        assertEquals(c.date(2001, 3, 1), p.periodString("2001-04").getPeriod());
        assertEquals(c.date(2001, 0, 1), p.periodString("2001-Q1").getPeriod());
        assertEquals(c.date(2001, 3, 1), p.periodString("2001-Q2").getPeriod());
        assertEquals(c.date(2001, 3, 1), p.periodString("2001Q2").getPeriod());
        assertNull(p.periodString("2001-Q0").getPeriod());
        assertNull(p.periodString("hello").getPeriod());
        assertNull(p.periodString("").getPeriod());

        p.setTimeFormat(TimeFormat.MONTHLY);
        assertEquals(c.date(2001, 0, 1), p.periodString("2001-01").getPeriod());
        assertEquals(c.date(2001, 1, 1), p.periodString("2001-02").getPeriod());
        assertEquals(c.date(2001, 0, 1), p.periodString("2001-M1").getPeriod());
        assertEquals(c.date(2001, 0, 1), p.periodString("2001M1").getPeriod());
        assertNull(p.periodString("2001-M0").getPeriod());
        assertNull(p.periodString("hello").getPeriod());
        assertNull(p.periodString("").getPeriod());
    }
}
