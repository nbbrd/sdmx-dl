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
package be.nbb.sdmx.facade;

import static be.nbb.sdmx.facade.DataflowRef.ALL_AGENCIES;
import static be.nbb.sdmx.facade.DataflowRef.LATEST_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class FlowRefTest {

    @Test
    public void testParse() {
        assertEquals(DataflowRef.of(null, "", null), DataflowRef.parse(""));
        assertEquals(DataflowRef.of(null, "hello", null), DataflowRef.parse("hello"));
        assertEquals(DataflowRef.of("world", "hello", null), DataflowRef.parse("world,hello"));
        assertEquals(DataflowRef.of("world", "hello", "123"), DataflowRef.parse("world,hello,123"));
        assertEquals(DataflowRef.of("world", "hello", LATEST_VERSION), DataflowRef.parse("world,hello,"));
        assertEquals(DataflowRef.of(ALL_AGENCIES, "hello", LATEST_VERSION), DataflowRef.parse(",hello,"));
        assertEquals(DataflowRef.of(ALL_AGENCIES, "", LATEST_VERSION), DataflowRef.parse(",,"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalid() {
        DataflowRef.parse(",,,,");
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("null")
    public void testParseNull() {
        DataflowRef.parse(null);
    }

    @Test
    public void testValueOf() {
        DataflowRef flowRef;

        flowRef = DataflowRef.of(null, "", null);
        assertEquals(ALL_AGENCIES, flowRef.getAgencyId());
        assertEquals("", flowRef.getId());
        assertEquals(LATEST_VERSION, flowRef.getVersion());
        assertEquals("all,,latest", flowRef.toString());

        flowRef = DataflowRef.of("", "hello", null);
        assertEquals(ALL_AGENCIES, flowRef.getAgencyId());
        assertEquals("hello", flowRef.getId());
        assertEquals(LATEST_VERSION, flowRef.getVersion());
        assertEquals("all,hello,latest", flowRef.toString());

        flowRef = DataflowRef.of("world", "hello", null);
        assertEquals("world", flowRef.getAgencyId());
        assertEquals("hello", flowRef.getId());
        assertEquals(LATEST_VERSION, flowRef.getVersion());
        assertEquals("world,hello,latest", flowRef.toString());

        flowRef = DataflowRef.of("world", "hello", "123");
        assertEquals("world", flowRef.getAgencyId());
        assertEquals("hello", flowRef.getId());
        assertEquals("123", flowRef.getVersion());
        assertEquals("world,hello,123", flowRef.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfInvalid() {
        DataflowRef.of(null, "world,hello", null);
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("null")
    public void testValueOfNull() {
        DataflowRef.of(null, null, null);
    }

    @Test
    public void testEquals() {
        assertEquals(DataflowRef.of("", "", ""), DataflowRef.of("", "", ""));
        assertEquals(DataflowRef.of("world", "hello", "123"), DataflowRef.of("world", "hello", "123"));
        assertNotEquals(DataflowRef.of("world", "hello", "123"), DataflowRef.of("world", "other", "123"));
        assertNotEquals(DataflowRef.of("world", "hello", "123"), DataflowRef.of("", "", ""));
    }

    @Test
    public void testContains() {
        assertTrue(DataflowRef.of("world", "hello", "123").contains(DataflowRef.of("world", "hello", "123")));
        assertTrue(DataflowRef.of(ALL_AGENCIES, "hello", "123").contains(DataflowRef.of("world", "hello", "123")));
        assertFalse(DataflowRef.of("world", "hello", "123").contains(DataflowRef.of(ALL_AGENCIES, "hello", "123")));
        assertTrue(DataflowRef.of("world", "hello", LATEST_VERSION).contains(DataflowRef.of("world", "hello", "123")));
        assertFalse(DataflowRef.of("world", "hello", "123").contains(DataflowRef.of("world", "hello", LATEST_VERSION)));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("null")
    public void testContainsNull() {
        DataflowRef.of("world", "hello", "123").contains(null);
    }
}
