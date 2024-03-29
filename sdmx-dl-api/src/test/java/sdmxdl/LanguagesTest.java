/*
 * Copyright 2017 National Bank of Belgium
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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.*;
import static sdmxdl.Languages.ANY;
import static sdmxdl.Languages.parse;

/**
 * @author Philippe Charles
 */
public class LanguagesTest {

    @Test
    @SuppressWarnings("null")
    public void testParse() {
        Assertions.assertThat(Languages.parse("*")).hasToString("*");
        Assertions.assertThat(Languages.parse("fr")).hasToString("fr");
        Assertions.assertThat(Languages.parse("fr-BE")).hasToString("fr-be");
        Assertions.assertThat(Languages.parse("fr-BE,fr;q=0.5")).hasToString("fr-be,fr;q=0.5");
        Assertions.assertThat(Languages.parse("fr-CH, fr;q=0.9, en;q=0.8, de;q=0.7, *;q=0.5")).hasToString("fr-ch,fr;q=0.9,en;q=0.8,de;q=0.7,*;q=0.5");
        assertThatIllegalArgumentException().isThrownBy(() -> Languages.parse("fr-BE;"));
        assertThatNullPointerException().isThrownBy(() -> Languages.parse(null));
    }

    @Test
    public void testEquals() {
        Assertions.assertThat(Languages.parse("*"))
                .isEqualTo(Languages.parse("*"))
                .isEqualTo(Languages.ANY);

        Assertions.assertThat(Languages.parse("fr-BE"))
                .isEqualTo(Languages.parse("fr-BE;q=1"))
                .isEqualTo(Languages.parse("fr-BE"));
    }

    @Test
    @SuppressWarnings("null")
    public void testLookupTag() {
        Assertions.assertThat(Languages.parse("fr").lookupTag(Arrays.asList("fr", "nl"))).isEqualTo("fr");
        Assertions.assertThat(Languages.parse("fr-BE").lookupTag(Arrays.asList("fr", "nl"))).isEqualTo("fr");
        Assertions.assertThat(Languages.parse("fr,nl;q=0.7,en;q=0.3").lookupTag(Arrays.asList("de", "nl", "en"))).isEqualTo("nl");
        Assertions.assertThat(Languages.parse("fr").lookupTag(Collections.singletonList("nl"))).isNull();
        assertThatNullPointerException().isThrownBy(() -> Languages.parse("fr").lookupTag(null));
    }

    @Test
    public void testSelect() {
        assertThat(ANY.select(emptyMap())).isNull();
        assertThat(ANY.select(singletonMap("en", null))).isNull();
        assertThat(ANY.select(singletonMap("en", "hello"))).isEqualTo("hello");
        assertThat(ANY.select(mapOf("en", "hello", "fr", "bonjour"))).isEqualTo("hello");
        assertThat(ANY.select(mapOf("fr", "bonjour", "en", "hello"))).isEqualTo("hello");
        assertThat(ANY.select(mapOf("fr", "bonjour", "aa", "hello"))).isEqualTo("bonjour");
        assertThat(ANY.select(mapOf("bb", "bonjour", "aa", "hello"))).isEqualTo("bonjour");
        assertThat(ANY.select(new TreeMap<>(mapOf("bb", "bonjour", "aa", "hello")))).isEqualTo("hello");

        assertThatNullPointerException().isThrownBy(() -> ANY.select(null));

        assertThat(parse("fr").select(mapOf("fr", "bonjour", "en", "hello"))).isEqualTo("bonjour");
        assertThat(parse("en").select(mapOf("en", "hello", "fr", "bonjour"))).isEqualTo("hello");
    }

    @Test
    public void testBlankText() {
        assertThat(ANY.select(mapOf("en", "", "de", "Gewinn- und Verlustrechnung")))
                .describedAs("No language priority should return first non-blank")
                .isEqualTo("Gewinn- und Verlustrechnung");

        assertThat(parse("en").select(mapOf("en", "", "de", "Gewinn- und Verlustrechnung")))
                .describedAs("Specified language priority should return any text even if blank")
                .isEqualTo("");
    }

    private static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> result = new LinkedHashMap<>();
        result.put(k1, v1);
        result.put(k2, v2);
        return result;
    }
}
