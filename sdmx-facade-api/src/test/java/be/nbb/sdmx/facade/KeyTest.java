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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class KeyTest {

    @Test
    public void testParse() {
        Key key;

        key = Key.parse("");
        assertEquals(1, key.getSize());
        assertEquals("all", key.toString());

        key = Key.parse("LOCSTL04.AUS.M");
        assertEquals(3, key.getSize());
        assertEquals("LOCSTL04", key.getItem(0));
        assertEquals("AUS", key.getItem(1));
        assertEquals("M", key.getItem(2));
        assertEquals("LOCSTL04.AUS.M", key.toString());

        key = Key.parse("LOCSTL04..M");
        assertEquals(3, key.getSize());
        assertEquals("LOCSTL04", key.getItem(0));
        assertEquals("", key.getItem(1));
        assertEquals("M", key.getItem(2));
        assertEquals("LOCSTL04..M", key.toString());

        key = Key.parse("LOCSTL04..");
        assertEquals(3, key.getSize());
        assertEquals("LOCSTL04", key.getItem(0));
        assertEquals("", key.getItem(1));
        assertEquals("", key.getItem(2));
        assertEquals("LOCSTL04..", key.toString());
    }

    @Test
    public void testValueOf() {
        Key key;

        key = Key.of();
        assertEquals(1, key.getSize());
        assertEquals("all", key.toString());

        key = Key.of("");
        assertEquals(1, key.getSize());
        assertEquals("all", key.toString());

        key = Key.of("LOCSTL04", "AUS", "M");
        assertEquals(3, key.getSize());
        assertEquals("LOCSTL04", key.getItem(0));
        assertEquals("AUS", key.getItem(1));
        assertEquals("M", key.getItem(2));
        assertEquals("LOCSTL04.AUS.M", key.toString());

        key = Key.of("LOCSTL04", "", "M");
        assertEquals(3, key.getSize());
        assertEquals("LOCSTL04", key.getItem(0));
        assertEquals("", key.getItem(1));
        assertEquals("M", key.getItem(2));
        assertEquals("LOCSTL04..M", key.toString());

        key = Key.of("LOCSTL04", "", "");
        assertEquals(3, key.getSize());
        assertEquals("LOCSTL04", key.getItem(0));
        assertEquals("", key.getItem(1));
        assertEquals("", key.getItem(2));
        assertEquals("LOCSTL04..", key.toString());
    }

    @Test
    public void testEquals() {
        assertEquals(Key.of(""), Key.of(""));
        assertEquals(Key.of("LOCSTL04", "AUS", "M"), Key.of("LOCSTL04", "AUS", "M"));
        assertEquals(Key.of("LOCSTL04", "*", "M"), Key.of("LOCSTL04", "", "M"));
        assertNotEquals(Key.of(""), Key.of("LOCSTL04", "AUS", "M"));
    }

    @Test
    public void testContains() {
        assertTrue(Key.of("").contains(Key.of("")));
        assertTrue(Key.ALL.contains(Key.ALL));
        assertTrue(Key.ALL.contains(Key.of("hello")));
        assertFalse(Key.of("hello").contains(Key.ALL));
        assertTrue(Key.of("LOCSTL04", "*", "M").contains(Key.of("LOCSTL04", "AUS", "M")));
        assertFalse(Key.of("LOCSTL04", "AUS", "M").contains(Key.of("LOCSTL04", "*", "M")));
    }

    @Test
    public void testBuilder() {
        Key.Builder b;

        b = Key.builder();
        assertThat(b.clear().toString()).isEqualTo("all");

        b = Key.builder("SECTOR", "REGION");
        assertThat(b.clear().put("SECTOR", "IND").put("REGION", "BE").toString()).isEqualTo("IND.BE");
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").toString()).isEqualTo("IND.BE");
        assertThat(b.clear().put("SECTOR", "IND").toString()).isEqualTo("IND.");
        assertThat(b.clear().put("REGION", "BE").toString()).isEqualTo(".BE");
//        assertThat(b.clear().toString()).isEqualTo("all");
    }
}
