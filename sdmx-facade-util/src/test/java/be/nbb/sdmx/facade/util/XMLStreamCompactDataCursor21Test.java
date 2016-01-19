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
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author charphi
 */
public class XMLStreamCompactDataCursor21Test {

    private final QuickStax stax = new QuickStax();
    private final QuickCalendar cal = new QuickCalendar();

    @Test
    public void testCompactData21() throws Exception {
        Key.Builder keyBuilder = Key.builder("FREQ", "AME_REF_AREA", "AME_TRANSFORMATION", "AME_AGG_METHOD", "AME_UNIT", "AME_REFERENCE", "AME_ITEM");
        Key key = Key.valueOf("A", "BEL", "1", "0", "0", "0", "OVGD");
        SdmxTestResource xml = SdmxTestResource.onResource("CompactData21.xml");

        try (DataCursor cursor = new XMLStreamCompactDataCursor21(stax.open(xml), keyBuilder, 0, "TIME_PERIOD", "OBS_VALUE")) {
            assertTrue(cursor.nextSeries());
            assertEquals(key, cursor.getKey());
            assertEquals(TimeFormat.YEARLY, cursor.getTimeFormat());
            int indexObs = -1;
            while (cursor.nextObs()) {
                switch (++indexObs) {
                    case 0:
                        assertEquals(cal.getDate(1960, 0, 1), cursor.getPeriod());
                        assertEquals(92.0142, cursor.getValue(), 0d);
                        break;
                    case 56:
                        assertEquals(cal.getDate(2016, 0, 1), cursor.getPeriod());
                        assertEquals(386.5655, cursor.getValue(), 0d);
                        break;
                }
            }
            assertEquals(56, indexObs);
            assertFalse(cursor.nextSeries());
        }
    }
}
