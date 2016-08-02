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

import static be.nbb.sdmx.facade.FlowRef.ALL_AGENCIES;
import static be.nbb.sdmx.facade.FlowRef.LATEST_VERSION;
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
        assertEquals(FlowRef.of(null, "", null), FlowRef.parse(""));
        assertEquals(FlowRef.of(null, "hello", null), FlowRef.parse("hello"));
        assertEquals(FlowRef.of("world", "hello", null), FlowRef.parse("world,hello"));
        assertEquals(FlowRef.of("world", "hello", "123"), FlowRef.parse("world,hello,123"));
        assertEquals(FlowRef.of("world", "hello", LATEST_VERSION), FlowRef.parse("world,hello,"));
        assertEquals(FlowRef.of(ALL_AGENCIES, "hello", LATEST_VERSION), FlowRef.parse(",hello,"));
        assertEquals(FlowRef.of(ALL_AGENCIES, "", LATEST_VERSION), FlowRef.parse(",,"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalid() {
        FlowRef.parse(",,,,");
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("null")
    public void testParseNull() {
        FlowRef.parse(null);
    }

    @Test
    public void testValueOf() {
        FlowRef flowRef;

        flowRef = FlowRef.of(null, "", null);
        assertEquals(ALL_AGENCIES, flowRef.getAgencyId());
        assertEquals("", flowRef.getFlowId());
        assertEquals(LATEST_VERSION, flowRef.getVersion());
        assertEquals("all,,latest", flowRef.toString());

        flowRef = FlowRef.of("", "hello", null);
        assertEquals(ALL_AGENCIES, flowRef.getAgencyId());
        assertEquals("hello", flowRef.getFlowId());
        assertEquals(LATEST_VERSION, flowRef.getVersion());
        assertEquals("all,hello,latest", flowRef.toString());

        flowRef = FlowRef.of("world", "hello", null);
        assertEquals("world", flowRef.getAgencyId());
        assertEquals("hello", flowRef.getFlowId());
        assertEquals(LATEST_VERSION, flowRef.getVersion());
        assertEquals("world,hello,latest", flowRef.toString());

        flowRef = FlowRef.of("world", "hello", "123");
        assertEquals("world", flowRef.getAgencyId());
        assertEquals("hello", flowRef.getFlowId());
        assertEquals("123", flowRef.getVersion());
        assertEquals("world,hello,123", flowRef.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfInvalid() {
        FlowRef.of(null, "world,hello", null);
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("null")
    public void testValueOfNull() {
        FlowRef.of(null, null, null);
    }

    @Test
    public void testEquals() {
        assertEquals(FlowRef.of("", "", ""), FlowRef.of("", "", ""));
        assertEquals(FlowRef.of("world", "hello", "123"), FlowRef.of("world", "hello", "123"));
        assertNotEquals(FlowRef.of("world", "hello", "123"), FlowRef.of("world", "other", "123"));
        assertNotEquals(FlowRef.of("world", "hello", "123"), FlowRef.of("", "", ""));
    }

    @Test
    public void testContains() {
        assertTrue(FlowRef.of("world", "hello", "123").contains(FlowRef.of("world", "hello", "123")));
        assertTrue(FlowRef.of(ALL_AGENCIES, "hello", "123").contains(FlowRef.of("world", "hello", "123")));
        assertFalse(FlowRef.of("world", "hello", "123").contains(FlowRef.of(ALL_AGENCIES, "hello", "123")));
        assertTrue(FlowRef.of("world", "hello", LATEST_VERSION).contains(FlowRef.of("world", "hello", "123")));
        assertFalse(FlowRef.of("world", "hello", "123").contains(FlowRef.of("world", "hello", LATEST_VERSION)));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("null")
    public void testContainsNull() {
        FlowRef.of("world", "hello", "123").contains(null);
    }
}
