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
package sdmxdl;

import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.Key.ALL;
import static sdmxdl.Key.of;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("ConstantConditions")
public class KeyTest {

    @Test
    public void testParse() {
        assertThatNullPointerException()
                .isThrownBy(() -> Key.parse(null));

        assertThat(Key.parse("")).satisfies(o -> {
            assertThat(o.size()).isEqualTo(1);
            assertThat(o.get(0)).isEqualTo("");
            assertThat(o.toString()).isEqualTo("all");
        });

        assertThat(Key.parse("LOCSTL04.AUS.M")).satisfies(o -> {
            assertThat(o.size()).isEqualTo(3);
            assertThat(o.get(0)).isEqualTo("LOCSTL04");
            assertThat(o.get(1)).isEqualTo("AUS");
            assertThat(o.get(2)).isEqualTo("M");
            assertThat(o.toString()).isEqualTo("LOCSTL04.AUS.M");
        });

        assertThat(Key.parse("LOCSTL04..M")).satisfies(o -> {
            assertThat(o.size()).isEqualTo(3);
            assertThat(o.get(0)).isEqualTo("LOCSTL04");
            assertThat(o.get(1)).isEqualTo("");
            assertThat(o.get(2)).isEqualTo("M");
            assertThat(o.toString()).isEqualTo("LOCSTL04..M");
        });

        assertThat(Key.parse("LOCSTL04..")).satisfies(o -> {
            assertThat(o.size()).isEqualTo(3);
            assertThat(o.get(0)).isEqualTo("LOCSTL04");
            assertThat(o.get(1)).isEqualTo("");
            assertThat(o.get(2)).isEqualTo("");
            assertThat(o.toString()).isEqualTo("LOCSTL04..");
        });
    }

    @Test
    public void testOfArray() {
        assertThatNullPointerException()
                .isThrownBy(() -> of((String[]) null));

        assertThat(of()).satisfies(o -> {
            assertThat(o.size()).isEqualTo(1);
            assertThat(o.get(0)).isEqualTo("");
            assertThat(o.toString()).isEqualTo("all");
        });

        assertThat(of("")).satisfies(o -> {
            assertThat(o.size()).isEqualTo(1);
            assertThat(o.get(0)).isEqualTo("");
            assertThat(o.toString()).isEqualTo("all");
        });

        assertThat(of("LOCSTL04", "AUS", "M")).satisfies(o -> {
            assertThat(o.size()).isEqualTo(3);
            assertThat(o.get(0)).isEqualTo("LOCSTL04");
            assertThat(o.get(1)).isEqualTo("AUS");
            assertThat(o.get(2)).isEqualTo("M");
            assertThat(o.toString()).isEqualTo("LOCSTL04.AUS.M");
        });

        assertThat(of("LOCSTL04", "", "M")).satisfies(o -> {
            assertThat(o.size()).isEqualTo(3);
            assertThat(o.get(0)).isEqualTo("LOCSTL04");
            assertThat(o.get(1)).isEqualTo("");
            assertThat(o.get(2)).isEqualTo("M");
            assertThat(o.toString()).isEqualTo("LOCSTL04..M");
        });

        assertThat(of("LOCSTL04", null, "")).satisfies(o -> {
            assertThat(o.size()).isEqualTo(3);
            assertThat(o.get(0)).isEqualTo("LOCSTL04");
            assertThat(o.get(1)).isEqualTo("");
            assertThat(o.get(2)).isEqualTo("");
            assertThat(o.toString()).isEqualTo("LOCSTL04..");
        });
    }

    @Test
    public void testOfCollection() {
        assertThatNullPointerException()
                .isThrownBy(() -> of((Collection<String>) null));

        assertThat(of(Collections.emptyList())).satisfies(o -> {
            assertThat(o.size()).isEqualTo(1);
            assertThat(o.get(0)).isEqualTo("");
            assertThat(o.toString()).isEqualTo("all");
        });

        assertThat(of(Collections.singletonList(""))).satisfies(o -> {
            assertThat(o.size()).isEqualTo(1);
            assertThat(o.get(0)).isEqualTo("");
            assertThat(o.toString()).isEqualTo("all");
        });

        assertThat(of(asList("LOCSTL04", "AUS", "M"))).satisfies(o -> {
            assertThat(o.size()).isEqualTo(3);
            assertThat(o.get(0)).isEqualTo("LOCSTL04");
            assertThat(o.get(1)).isEqualTo("AUS");
            assertThat(o.get(2)).isEqualTo("M");
            assertThat(o.toString()).isEqualTo("LOCSTL04.AUS.M");
        });

        assertThat(of(asList("LOCSTL04", "", "M"))).satisfies(o -> {
            assertThat(o.size()).isEqualTo(3);
            assertThat(o.get(0)).isEqualTo("LOCSTL04");
            assertThat(o.get(1)).isEqualTo("");
            assertThat(o.get(2)).isEqualTo("M");
            assertThat(o.toString()).isEqualTo("LOCSTL04..M");
        });

        assertThat(of(asList("LOCSTL04", null, ""))).satisfies(o -> {
            assertThat(o.size()).isEqualTo(3);
            assertThat(o.get(0)).isEqualTo("LOCSTL04");
            assertThat(o.get(1)).isEqualTo("");
            assertThat(o.get(2)).isEqualTo("");
            assertThat(o.toString()).isEqualTo("LOCSTL04..");
        });
    }

    @Test
    public void testEqualsAndHashCode() {
        assertThat(of(""))
                .isEqualTo(of(""))
                .hasSameHashCodeAs(of(""));
        assertThat(of("LOCSTL04", "AUS", "M"))
                .isEqualTo(of("LOCSTL04", "AUS", "M"))
                .hasSameHashCodeAs(of("LOCSTL04", "AUS", "M"));
        assertThat(of("LOCSTL04", "", "M"))
                .isEqualTo(of("LOCSTL04", "*", "M"))
                .hasSameHashCodeAs(of("LOCSTL04", "*", "M"));
        assertThat(of("LOCSTL04", "AUS", "M"))
                .isNotEqualTo(of(""));
    }

    @Test
    public void testIsSeries() {
        assertThat(ALL.isSeries()).isFalse();
        assertThat(of("LOCSTL04", "*").isSeries()).isFalse();
        assertThat(of("LOCSTL04", "AUS").isSeries()).isTrue();
        assertThat(of("USD+CHF").isSeries()).isFalse();
    }

    @Test
    public void testContainsKeys() {
        assertThat(of("").containsKey(seriesOf(of("")))).isTrue();
        assertThat(ALL.containsKey(seriesOf(ALL))).isTrue();
        assertThat(ALL.containsKey(seriesOf(of("hello", "world")))).isTrue();
        assertThat(of("hello").containsKey(seriesOf(ALL))).isFalse();
        assertThat(of("LOCSTL04", "*", "M").containsKey(seriesOf(of("LOCSTL04", "AUS", "M")))).isTrue();
        assertThat(of("LOCSTL04", "*").containsKey(seriesOf(of("LOCSTL04", "AUS", "M")))).isFalse();
        assertThat(of("LOCSTL04", "AUS", "M").containsKey(seriesOf(of("LOCSTL04", "*", "M")))).isFalse();
        assertThat(of("LOCSTL04", "AUS").containsKey(seriesOf(of("LOCSTL04", "*", "M")))).isFalse();
    }

    @Test
    public void testContains() {
        assertThat(of("").contains(of(""))).isTrue();
        assertThat(ALL.contains(ALL)).isTrue();
        assertThat(ALL.contains(of("hello", "world"))).isTrue();
        assertThat(of("hello").contains(ALL)).isFalse();
        assertThat(of("LOCSTL04", "*", "M").contains(of("LOCSTL04", "AUS", "M"))).isTrue();
        assertThat(of("LOCSTL04", "*").contains(of("LOCSTL04", "AUS", "M"))).isFalse();
        assertThat(of("LOCSTL04", "AUS", "M").contains(of("LOCSTL04", "*", "M"))).isFalse();
        assertThat(of("LOCSTL04", "AUS").contains(of("LOCSTL04", "*", "M"))).isFalse();
    }

    @Test
    public void testSupersedes() {
        assertThat(of("").supersedes(of(""))).isFalse();
        assertThat(ALL.supersedes(ALL)).isFalse();
        assertThat(ALL.supersedes(of("hello", "world"))).isTrue();
        assertThat(of("hello").supersedes(ALL)).isFalse();
        assertThat(of("LOCSTL04", "*", "M").supersedes(of("LOCSTL04", "AUS", "M"))).isTrue();
        assertThat(of("LOCSTL04", "*").supersedes(of("LOCSTL04", "AUS", "M"))).isFalse();
        assertThat(of("LOCSTL04", "AUS", "M").supersedes(of("LOCSTL04", "*", "M"))).isFalse();
        assertThat(of("LOCSTL04", "AUS").supersedes(of("LOCSTL04", "*", "M"))).isFalse();
    }

    @Test
    public void testBuilderOfDimensions() {
        assertThatNullPointerException()
                .isThrownBy(() -> Key.builder((String[]) null));

        Key.Builder b;

        b = Key.builder();
        assertThat(b.clear().toString()).isEqualTo("all");
        assertThat(b.isDimension("hello")).isFalse();
        assertThat(b.build()).isEqualTo(ALL);

        b = Key.builder("SECTOR", "REGION");
        assertThat(b.clear().put("SECTOR", "IND").put("REGION", "BE").toString()).isEqualTo("IND.BE");
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").toString()).isEqualTo("IND.BE");
        assertThat(b.clear().put("SECTOR", "IND").toString()).isEqualTo("IND.");
        assertThat(b.clear().put("REGION", "BE").toString()).isEqualTo(".BE");
        assertThat(b.clear().toString()).isEqualTo(".");
        assertThat(b.isDimension("hello")).isFalse();
        assertThat(b.isDimension("SECTOR")).isTrue();
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").build()).isEqualTo(of("IND", "BE"));
        assertThat(b.clear().put("REGION", "BE").build()).isEqualTo(of("", "BE"));
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").getItem(0)).isEqualTo("IND");
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").getItem(1)).isEqualTo("BE");
        assertThat(b.clear().put("REGION", "BE").getItem(0)).isEqualTo("");
        assertThat(b.clear().put("REGION", "BE").getItem(1)).isEqualTo("BE");

        assertThat(b.clear().isSeries()).isFalse();
        assertThat(b.clear().put("SECTOR", "IND").isSeries()).isFalse();
        assertThat(b.clear().put("SECTOR", "IND").put("REGION", "BE").isSeries()).isTrue();
    }

    @Test
    public void testBuilderOfDataStructure() {
        assertThatNullPointerException()
                .isThrownBy(() -> Key.builder((DataStructure) null));

        Key.Builder b;

        b = Key.builder(DataStructure
                .builder()
                .ref(DataStructureRef.parse("ref"))
                .label("")
                .build());
        assertThat(b.clear().toString()).isEqualTo("all");
        assertThat(b.isDimension("hello")).isFalse();
        assertThat(b.build()).isEqualTo(ALL);

        b = Key.builder(DataStructure
                .builder()
                .ref(DataStructureRef.parse("ref"))
                .label("")
                .dimension(Dimension.builder().position(1).id("SECTOR").label("Sector").build())
                .dimension(Dimension.builder().position(2).id("REGION").label("Region").build())
                .build());
        assertThat(b.clear().put("SECTOR", "IND").put("REGION", "BE").toString()).isEqualTo("IND.BE");
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").toString()).isEqualTo("IND.BE");
        assertThat(b.clear().put("SECTOR", "IND").toString()).isEqualTo("IND.");
        assertThat(b.clear().put("REGION", "BE").toString()).isEqualTo(".BE");
        assertThat(b.clear().toString()).isEqualTo(".");
        assertThat(b.isDimension("hello")).isFalse();
        assertThat(b.isDimension("SECTOR")).isTrue();
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").build()).isEqualTo(of("IND", "BE"));
        assertThat(b.clear().put("REGION", "BE").build()).isEqualTo(of("", "BE"));
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").getItem(0)).isEqualTo("IND");
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").getItem(1)).isEqualTo("BE");
        assertThat(b.clear().put("REGION", "BE").getItem(0)).isEqualTo("");
        assertThat(b.clear().put("REGION", "BE").getItem(1)).isEqualTo("BE");

        assertThat(b.clear().isSeries()).isFalse();
        assertThat(b.clear().put("SECTOR", "IND").isSeries()).isFalse();
        assertThat(b.clear().put("SECTOR", "IND").put("REGION", "BE").isSeries()).isTrue();
    }

    private static Series seriesOf(Key key) {
        return Series.builder().key(key).freq(Frequency.MONTHLY).build();
    }
}
