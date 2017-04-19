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
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class KeyTest {

    @Test
    public void testParse() {
        assertThat(Key.parse("")).satisfies(o -> {
            assertThat(o.getSize()).isEqualTo(1);
            assertThat(o.getItem(0)).isEqualTo("");
            assertThat(o.toString()).isEqualTo("all");
        });

        assertThat(Key.parse("LOCSTL04.AUS.M")).satisfies(o -> {
            assertThat(o.getSize()).isEqualTo(3);
            assertThat(o.getItem(0)).isEqualTo("LOCSTL04");
            assertThat(o.getItem(1)).isEqualTo("AUS");
            assertThat(o.getItem(2)).isEqualTo("M");
            assertThat(o.toString()).isEqualTo("LOCSTL04.AUS.M");
        });

        assertThat(Key.parse("LOCSTL04..M")).satisfies(o -> {
            assertThat(o.getSize()).isEqualTo(3);
            assertThat(o.getItem(0)).isEqualTo("LOCSTL04");
            assertThat(o.getItem(1)).isEqualTo("");
            assertThat(o.getItem(2)).isEqualTo("M");
            assertThat(o.toString()).isEqualTo("LOCSTL04..M");
        });

        assertThat(Key.parse("LOCSTL04..")).satisfies(o -> {
            assertThat(o.getSize()).isEqualTo(3);
            assertThat(o.getItem(0)).isEqualTo("LOCSTL04");
            assertThat(o.getItem(1)).isEqualTo("");
            assertThat(o.getItem(2)).isEqualTo("");
            assertThat(o.toString()).isEqualTo("LOCSTL04..");
        });
    }

    @Test
    public void testValueOf() {
        assertThat(Key.of()).satisfies(o -> {
            assertThat(o.getSize()).isEqualTo(1);
            assertThat(o.getItem(0)).isEqualTo("");
            assertThat(o.toString()).isEqualTo("all");
        });

        assertThat(Key.of("")).satisfies(o -> {
            assertThat(o.getSize()).isEqualTo(1);
            assertThat(o.getItem(0)).isEqualTo("");
            assertThat(o.toString()).isEqualTo("all");
        });

        assertThat(Key.of("LOCSTL04", "AUS", "M")).satisfies(o -> {
            assertThat(o.getSize()).isEqualTo(3);
            assertThat(o.getItem(0)).isEqualTo("LOCSTL04");
            assertThat(o.getItem(1)).isEqualTo("AUS");
            assertThat(o.getItem(2)).isEqualTo("M");
            assertThat(o.toString()).isEqualTo("LOCSTL04.AUS.M");
        });

        assertThat(Key.of("LOCSTL04", "", "M")).satisfies(o -> {
            assertThat(o.getSize()).isEqualTo(3);
            assertThat(o.getItem(0)).isEqualTo("LOCSTL04");
            assertThat(o.getItem(1)).isEqualTo("");
            assertThat(o.getItem(2)).isEqualTo("M");
            assertThat(o.toString()).isEqualTo("LOCSTL04..M");
        });

        assertThat(Key.of("LOCSTL04", "", "")).satisfies(o -> {
            assertThat(o.getSize()).isEqualTo(3);
            assertThat(o.getItem(0)).isEqualTo("LOCSTL04");
            assertThat(o.getItem(1)).isEqualTo("");
            assertThat(o.getItem(2)).isEqualTo("");
            assertThat(o.toString()).isEqualTo("LOCSTL04..");
        });
    }

    @Test
    public void testEquals() {
        assertThat(Key.of("")).isEqualTo(Key.of(""));
        assertThat(Key.of("LOCSTL04", "AUS", "M")).isEqualTo(Key.of("LOCSTL04", "AUS", "M"));
        assertThat(Key.of("LOCSTL04", "", "M")).isEqualTo(Key.of("LOCSTL04", "*", "M"));
        assertThat(Key.of("LOCSTL04", "AUS", "M")).isNotEqualTo(Key.of(""));
    }

    @Test
    public void testContains() {
        assertThat(Key.of("").contains(Key.of(""))).isTrue();
        assertThat(Key.ALL.contains(Key.ALL)).isTrue();
        assertThat(Key.ALL.contains(Key.of("hello"))).isTrue();
        assertThat(Key.of("hello").contains(Key.ALL)).isFalse();
        assertThat(Key.of("LOCSTL04", "*", "M").contains(Key.of("LOCSTL04", "AUS", "M"))).isTrue();
        assertThat(Key.of("LOCSTL04", "AUS", "M").contains(Key.of("LOCSTL04", "*", "M"))).isFalse();
    }

    @Test
    public void testBuilder() {
        Key.Builder b;

        b = Key.builder();
        assertThat(b.clear().toString()).isEqualTo("all");
        assertThat(b.isDimension("hello")).isFalse();

        b = Key.builder("SECTOR", "REGION");
        assertThat(b.clear().put("SECTOR", "IND").put("REGION", "BE").toString()).isEqualTo("IND.BE");
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").toString()).isEqualTo("IND.BE");
        assertThat(b.clear().put("SECTOR", "IND").toString()).isEqualTo("IND.");
        assertThat(b.clear().put("REGION", "BE").toString()).isEqualTo(".BE");
//        assertThat(b.clear().toString()).isEqualTo("all");
        assertThat(b.isDimension("hello")).isFalse();
        assertThat(b.isDimension("SECTOR")).isTrue();
    }
}
