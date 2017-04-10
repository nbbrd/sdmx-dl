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
import static org.junit.Assert.assertNull;

/**
 *
 * @author charphi
 */
public class XMLStreamGenericDataCursor20Test {

    private final QuickCalendar cal = new QuickCalendar();

    @Test
    public void testCursor() throws Exception {
        Key.Builder keyBuilder = Key.builder("SUBJECT", "LOCATION", "FREQUENCY");
        Key singleKey = Key.of("LOCSTL04", "AUS", "M");

        try (DataCursor cursor = new XMLStreamGenericDataCursor20(SdmxTestResource.NBB_DATA.open(), keyBuilder)) {
            int indexSeries = -1;
            while (cursor.nextSeries()) {
                switch (++indexSeries) {
                    case 0:
                        assertEquals(singleKey, cursor.getSeriesKey());
                        assertEquals(TimeFormat.MONTHLY, cursor.getSeriesTimeFormat());
                        int indexObs = -1;
                        while (cursor.nextObs()) {
                            switch (++indexObs) {
                                case 0:
                                    assertEquals(cal.date(1966, 1, 1), cursor.getObsPeriod());
                                    assertEquals(98.68823, cursor.getObsValue(), 0d);
                                    break;
                                case 188:
                                    assertEquals(cal.date(1970, 7, 1), cursor.getObsPeriod());
                                    assertEquals(101.1945, cursor.getObsValue(), 0d);
                                    break;
                                case 199:
                                    assertNull(cursor.getObsPeriod());
                                    assertEquals(93.7211, cursor.getObsValue(), 0d);
                                    break;
                            }
                        }
                        assertEquals(199, indexObs);
                        break;
                }
            }
            assertEquals(0, indexSeries);
        }
    }
}
