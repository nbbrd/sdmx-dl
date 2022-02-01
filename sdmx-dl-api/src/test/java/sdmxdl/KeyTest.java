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

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import java.util.AbstractList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("ConstantConditions")
public class KeyTest {

    @Test
    public void testParse() {
        assertThatNullPointerException()
                .isThrownBy(() -> Key.parse(null));

        assertThat(Key.parse(""))
                .describedAs("Empty must return 'all'")
                .hasToString(Key.ALL_KEYWORD);

        assertThat(Key.parse("*"))
                .describedAs("Star must return 'all'")
                .hasToString(Key.ALL_KEYWORD);

        assertThat(Key.parse(" "))
                .describedAs("Blank must return 'all'")
                .hasToString(Key.ALL_KEYWORD);

        assertThat(Key.parse("4.AUS.M"))
                .describedAs("Series must return series")
                .hasToString("4.AUS.M");

        assertThat(Key.parse("4..M"))
                .describedAs("Empty code must return wildcard")
                .hasToString("4..M");

        assertThat(Key.parse("4.*.M"))
                .describedAs("Star code must return wildcard")
                .hasToString("4..M");

        assertThat(Key.parse("4. .M"))
                .describedAs("Blank code must return wildcard")
                .hasToString("4..M");

        assertThat(Key.parse("4.+.M"))
                .describedAs("OR code must return wildcard")
                .hasToString("4..M");

        assertThat(Key.parse("4.CHF+AUS.M"))
                .describedAs("Multi code must return ordered")
                .hasToString("4.AUS+CHF.M");
    }

    @Test
    public void testOfArray() {
        assertThatNullPointerException()
                .isThrownBy(() -> Key.of((String[]) null));

        assertThat(Key.of())
                .describedAs("Empty must return 'all'")
                .hasToString(Key.ALL_KEYWORD);

        assertThat(Key.of("*"))
                .describedAs("Star must return 'all'")
                .hasToString(Key.ALL_KEYWORD);

        assertThat(Key.of(" "))
                .describedAs("Blank must return 'all'")
                .hasToString(Key.ALL_KEYWORD);

        assertThat(Key.of("4", "AUS", "M"))
                .describedAs("Series must return series")
                .hasToString("4.AUS.M");

        assertThat(Key.of("4", "", "M"))
                .describedAs("Empty code must return wildcard")
                .hasToString("4..M");

        assertThat(Key.of("4", "*", "M"))
                .describedAs("Star code must return wildcard")
                .hasToString("4..M");

        assertThat(Key.of("4", " ", "M"))
                .describedAs("Blank code must return wildcard")
                .hasToString("4..M");

        assertThat(Key.of("4", "+", "M"))
                .describedAs("OR code must return wildcard")
                .hasToString("4..M");

        assertThat(Key.of("4", "CHF+AUS", "M"))
                .describedAs("Multi code must return ordered")
                .hasToString("4.AUS+CHF.M");

        assertThat(Key.of("4", null, ""))
                .describedAs("Null code must return wildcard")
                .hasToString("4..");
    }

    @Test
    public void testOfList() {
        assertThatNullPointerException()
                .isThrownBy(() -> Key.of((List<String>) null));

        assertThat(Key.of(emptyList()))
                .describedAs("Empty must return 'all'")
                .hasToString(Key.ALL_KEYWORD);

        assertThat(Key.of(singletonList(" ")))
                .describedAs("Blank must return 'all'")
                .hasToString(Key.ALL_KEYWORD);

        assertThat(Key.of(asList("4", "AUS", "M")))
                .describedAs("Series must return series")
                .hasToString("4.AUS.M");

        assertThat(Key.of(asList("4", "", "M")))
                .describedAs("Empty code must return wildcard")
                .hasToString("4..M");

        assertThat(Key.of(asList("4", "*", "M")))
                .describedAs("Star code must return wildcard")
                .hasToString("4..M");

        assertThat(Key.of(asList("4", " ", "M")))
                .describedAs("Blank code must return wildcard")
                .hasToString("4..M");

        assertThat(Key.of(asList("4", "+", "M")))
                .describedAs("OR code must return wildcard")
                .hasToString("4..M");

        assertThat(Key.of(asList("4", "CHF+AUS", "M")))
                .describedAs("Multi code must return ordered")
                .hasToString("4.AUS+CHF.M");

        assertThat(Key.of(asList("4", null, "")))
                .describedAs("Null code must return wildcard")
                .hasToString("4..");
    }

    @Test
    public void testGetAndSize() {
        assertThat(Key.parse(""))
                .describedAs("Empty must return 'all'")
                .extracting(KeyTest::keyAsList, LIST)
                .containsExactly("");

        assertThat(Key.parse("4.AUS.M"))
                .describedAs("Series must return series")
                .extracting(KeyTest::keyAsList, LIST)
                .containsExactly("4", "AUS", "M");

        assertThat(Key.parse("4..M"))
                .describedAs("Empty code must return wildcard")
                .extracting(KeyTest::keyAsList, LIST)
                .containsExactly("4", "", "M");

        assertThat(Key.parse("4.*.M"))
                .describedAs("Star code must return wildcard")
                .extracting(KeyTest::keyAsList, LIST)
                .containsExactly("4", "", "M");

        assertThat(Key.parse("4. .M"))
                .describedAs("Blank code must return wildcard")
                .extracting(KeyTest::keyAsList, LIST)
                .containsExactly("4", "", "M");
    }

    @Test
    public void testEqualsAndHashCode() {
        assertThat(Key.of(""))
                .describedAs("Empty")
                .isEqualTo(Key.ALL)
                .hasSameHashCodeAs(Key.ALL);

        assertThat(Key.of("*"))
                .describedAs("Star")
                .isEqualTo(Key.ALL)
                .hasSameHashCodeAs(Key.ALL);

        assertThat(Key.of(" "))
                .describedAs("Blank")
                .isEqualTo(Key.ALL)
                .hasSameHashCodeAs(Key.ALL);

        assertThat(Key.of("4", "AUS", "M"))
                .describedAs("Series")
                .isEqualTo(Key.of("4", "AUS", "M"))
                .hasSameHashCodeAs(Key.of("4", "AUS", "M"));

        assertThat(Key.of("4", "", "M"))
                .describedAs("Empty code")
                .isEqualTo(Key.of("4", "", "M"))
                .hasSameHashCodeAs(Key.of("4", "", "M"));

        assertThat(Key.of("4", "*", "M"))
                .describedAs("Star code")
                .isEqualTo(Key.of("4", "", "M"))
                .hasSameHashCodeAs(Key.of("4", "", "M"));

        assertThat(Key.of("4", " ", "M"))
                .describedAs("Blank code")
                .isEqualTo(Key.of("4", "", "M"))
                .hasSameHashCodeAs(Key.of("4", "", "M"));
    }

    private static Condition<Key> series() {
        return new Condition<>(Key::isSeries, "Must be a series");
    }

    @Test
    public void testIsSeries() {
        assertThat(Key.ALL).isNot(series());
        assertThat(Key.parse("4.AUS.M")).is(series());
        assertThat(Key.parse("4.AUS+CHF.M")).isNot(series());
        assertThat(Key.parse("4..M")).isNot(series());
    }

    private static Condition<Key> containing(String key) {
        return new Condition<>(parent -> parent.contains(Key.parse(key)), "Must contain %s", key);
    }

    @Test
    public void testContains() {
        assertThat(Key.ALL)
                .is(containing("4.AUS.M"))
                .is(containing("4.CHF.M"))
                .is(containing("4.AUS+CHF.M"))
                .is(containing("4..M"))
                .is(containing("4.M"))
                .is(containing("4.AUS.M.X"))
                .is(containing(""));

        assertThat(Key.parse("4.AUS.M"))
                .is(containing("4.AUS.M"))
                .isNot(containing("4.CHF.M"))
                .isNot(containing("4.AUS+CHF.M"))
                .isNot(containing("4..M"))
                .isNot(containing("4.M"))
                .isNot(containing("4.AUS.M.X"))
                .isNot(containing(""));

        assertThat(Key.parse("4..M"))
                .is(containing("4.AUS.M"))
                .is(containing("4.CHF.M"))
                .is(containing("4.AUS+CHF.M"))
                .is(containing("4..M"))
                .isNot(containing("4.M"))
                .isNot(containing("4.AUS.M.X"))
                .isNot(containing(""));

        assertThat(Key.parse("4.AUS+CHF.M"))
                .is(containing("4.AUS.M"))
                .is(containing("4.CHF.M"))
                .is(containing("4.AUS+CHF.M"))
                .isNot(containing("4..M"))
                .isNot(containing("4.M"))
                .isNot(containing("4.AUS.M.X"))
                .isNot(containing(""));
    }

    private static Condition<Key> containingKey(String key) {
        Series series = Series.builder().key(Key.parse(key)).freq(Frequency.MONTHLY).build();
        return new Condition<>(parent -> parent.containsKey(series), "Must contain key %s", key);
    }

    @Test
    public void testContainsKey() {
        assertThat(Key.ALL)
                .is(containingKey("4.AUS.M"))
                .is(containingKey("4.CHF.M"))
                .is(containingKey("4.AUS+CHF.M"))
                .is(containingKey("4..M"))
                .is(containingKey("4.M"))
                .is(containingKey("4.AUS.M.X"))
                .is(containingKey(""));

        assertThat(Key.parse("4.AUS.M"))
                .is(containingKey("4.AUS.M"))
                .isNot(containingKey("4.CHF.M"))
                .isNot(containingKey("4.AUS+CHF.M"))
                .isNot(containingKey("4..M"))
                .isNot(containingKey("4.M"))
                .isNot(containingKey("4.AUS.M.X"))
                .isNot(containingKey(""));

        assertThat(Key.parse("4..M"))
                .is(containingKey("4.AUS.M"))
                .is(containingKey("4.CHF.M"))
                .is(containingKey("4.AUS+CHF.M"))
                .is(containingKey("4..M"))
                .isNot(containingKey("4.M"))
                .isNot(containingKey("4.AUS.M.X"))
                .isNot(containingKey(""));

        assertThat(Key.parse("4.AUS+CHF.M"))
                .is(containingKey("4.AUS.M"))
                .is(containingKey("4.CHF.M"))
                .is(containingKey("4.AUS+CHF.M"))
                .isNot(containingKey("4..M"))
                .isNot(containingKey("4.M"))
                .isNot(containingKey("4.AUS.M.X"))
                .isNot(containingKey(""));
    }

    private static Condition<Key> superseding(String key) {
        return new Condition<>(parent -> parent.supersedes(Key.parse(key)), "Must contain key %s", key);
    }

    @Test
    public void testSupersedes() {
        assertThat(Key.ALL)
                .is(superseding("4.AUS.M"))
                .is(superseding("4.CHF.M"))
                .is(superseding("4.AUS+CHF.M"))
                .is(superseding("4..M"))
                .is(superseding("4.M"))
                .is(superseding("4.AUS.M.X"))
                .is(superseding("5.AUS.M"))
                .isNot(superseding(""));

        assertThat(Key.parse("4.AUS.M"))
                .isNot(superseding("4.AUS.M"))
                .isNot(superseding("4.CHF.M"))
                .isNot(superseding("4.AUS+CHF.M"))
                .isNot(superseding("4..M"))
                .isNot(superseding("4.M"))
                .isNot(superseding("4.AUS.M.X"))
                .isNot(superseding("5.AUS.M"))
                .isNot(superseding(""));

        assertThat(Key.parse("4..M"))
                .is(superseding("4.AUS.M"))
                .is(superseding("4.CHF.M"))
                .is(superseding("4.AUS+CHF.M"))
                .isNot(superseding("4..M"))
                .isNot(superseding("4.M"))
                .isNot(superseding("4.AUS.M.X"))
                .isNot(superseding("5.AUS.M"))
                .isNot(superseding(""));

        assertThat(Key.parse("4.AUS+CHF.M"))
                .is(superseding("4.AUS.M"))
                .is(superseding("4.CHF.M"))
                .isNot(superseding("4.AUS+CHF.M"))
                .isNot(superseding("4..M"))
                .isNot(superseding("4.M"))
                .isNot(superseding("4.AUS.M.X"))
                .isNot(superseding("5.AUS.M"))
                .isNot(superseding(""));

        assertThat(Key.parse("4.M"))
                .isNot(superseding("4.AUS.M"))
                .isNot(superseding("4.CHF.M"))
                .isNot(superseding("4.AUS+CHF.M"))
                .isNot(superseding("4..M"))
                .isNot(superseding("4.M"))
                .isNot(superseding("4.AUS.M.X"))
                .isNot(superseding("5.AUS.M"))
                .isNot(superseding(""));
    }

    @Test
    public void testIsValidOn() {
        assertThatNullPointerException()
                .isThrownBy(() -> Key.of("IND", "BE").validateOn(null));

        assertThat(Key.of())
                .is(validOn(dsd0))
                .is(validOn(dsd2));

        assertThat(Key.of("IND"))
                .isNot(validOn(dsd0))
                .isNot(validOn(dsd2));

        assertThat(Key.of("IND", "BE", "XX"))
                .isNot(validOn(dsd0))
                .isNot(validOn(dsd2));

        assertThat(Key.of("IND", "BE"))
                .isNot(validOn(dsd0))
                .is(validOn(dsd2));

        assertThat(Key.of("IND", "XX"))
                .isNot(validOn(dsd0))
                .isNot(validOn(dsd2));

        assertThat(Key.of("IND", ""))
                .isNot(validOn(dsd0))
                .is(validOn(dsd2));

        assertThat(Key.of("IND", "BE+"))
                .isNot(validOn(dsd0))
                .is(validOn(dsd2));

        assertThat(Key.of("IND", "BE+LU"))
                .isNot(validOn(dsd0))
                .is(validOn(dsd2));

        assertThat(Key.of("IND", "BE+XX"))
                .isNot(validOn(dsd0))
                .isNot(validOn(dsd2));

        assertThat(Key.of("IND").validateOn(dsd2))
                .isEqualTo("Expected key 'IND' to have 2 dimensions instead of 1");

        assertThat(Key.of("IND", "XX").validateOn(dsd2))
                .isEqualTo("Expected key 'IND.XX' to have a known code at position 2 for dimension 'REGION' instead of 'XX'");
    }

    private static Condition<Key> validOn(DataStructure dsd) {
        return new Condition<>(parent -> parent.validateOn(dsd) == null, "valid on dsd %s", dsd);
    }

    @Test
    public void testBuilderOfDimensions() {
        assertThatNullPointerException()
                .isThrownBy(() -> Key.builder((List<String>) null));

        Key.Builder b;

        b = Key.builder(emptyList());
        assertThat(b.clear().toString()).isEqualTo(Key.ALL_KEYWORD);
        assertThat(b.isDimension("hello")).isFalse();
        assertThat(b.build()).isEqualTo(Key.ALL);

        b = Key.builder(asList("SECTOR", "REGION"));
        assertThat(b.clear().put("SECTOR", "IND").put("REGION", "BE").toString()).isEqualTo("IND.BE");
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").toString()).isEqualTo("IND.BE");
        assertThat(b.clear().put("SECTOR", "IND").toString()).isEqualTo("IND.");
        assertThat(b.clear().put("REGION", "BE").toString()).isEqualTo(".BE");
        assertThat(b.clear().toString()).isEqualTo(".");
        assertThat(b.isDimension("hello")).isFalse();
        assertThat(b.isDimension("SECTOR")).isTrue();
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").build()).isEqualTo(Key.of("IND", "BE"));
        assertThat(b.clear().put("REGION", "BE").build()).isEqualTo(Key.of("", "BE"));
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

        b = Key.builder(dsd0);
        assertThat(b.clear().toString()).isEqualTo(Key.ALL_KEYWORD);
        assertThat(b.isDimension("hello")).isFalse();
        assertThat(b.build()).isEqualTo(Key.ALL);

        b = Key.builder(dsd2);
        assertThat(b.clear().put("SECTOR", "IND").put("REGION", "BE").toString()).isEqualTo("IND.BE");
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").toString()).isEqualTo("IND.BE");
        assertThat(b.clear().put("SECTOR", "IND").toString()).isEqualTo("IND.");
        assertThat(b.clear().put("REGION", "BE").toString()).isEqualTo(".BE");
        assertThat(b.clear().toString()).isEqualTo(".");
        assertThat(b.isDimension("hello")).isFalse();
        assertThat(b.isDimension("SECTOR")).isTrue();
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").build()).isEqualTo(Key.of("IND", "BE"));
        assertThat(b.clear().put("REGION", "BE").build()).isEqualTo(Key.of("", "BE"));
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").getItem(0)).isEqualTo("IND");
        assertThat(b.clear().put("REGION", "BE").put("SECTOR", "IND").getItem(1)).isEqualTo("BE");
        assertThat(b.clear().put("REGION", "BE").getItem(0)).isEqualTo("");
        assertThat(b.clear().put("REGION", "BE").getItem(1)).isEqualTo("BE");

        assertThat(b.clear().isSeries()).isFalse();
        assertThat(b.clear().put("SECTOR", "IND").isSeries()).isFalse();
        assertThat(b.clear().put("SECTOR", "IND").put("REGION", "BE").isSeries()).isTrue();
    }

    private final DataStructure dsd0 = DataStructure
            .builder()
            .ref(DataStructureRef.parse("ref"))
            .primaryMeasureId("")
            .label("")
            .build();

    private final Codelist clSector = Codelist.builder().ref(CodelistRef.parse("CL_SECTOR")).code("IND", "Industry").build();
    private final Codelist clRegion = Codelist.builder().ref(CodelistRef.parse("CL_REGION")).code("BE", "Belgium").code("LU", "Luxembourg").build();

    private final Dimension sector = Dimension.builder().position(1).id("SECTOR").label("Sector").codelist(clSector).build();
    private final Dimension region = Dimension.builder().position(3).id("REGION").label("Region").codelist(clRegion).build();

    private final DataStructure dsd2 = dsd0
            .toBuilder()
            .dimension(sector)
            .dimension(region)
            .build();

    private static List<String> keyAsList(Key key) {
        return new AbstractList<String>() {
            @Override
            public int size() {
                return key.size();
            }

            @Override
            public String get(int index) {
                return key.get(index);
            }
        };
    }
}
