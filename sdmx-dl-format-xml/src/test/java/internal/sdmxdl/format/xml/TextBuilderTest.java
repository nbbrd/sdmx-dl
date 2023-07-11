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
package internal.sdmxdl.format.xml;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.Languages.ANY;
import static sdmxdl.Languages.parse;

/**
 * @author Philippe Charles
 */
public class TextBuilderTest {

    @Test
    @SuppressWarnings("null")
    public void test() {
        assertThat(new TextBuilder(ANY).build()).isNull();
        assertThat(new TextBuilder(ANY).put("en", null).build()).isNull();
        assertThat(new TextBuilder(ANY).put("en", "hello").clear().build()).isNull();
        assertThat(new TextBuilder(ANY).put("en", "hello").build()).isEqualTo("hello");
        assertThat(new TextBuilder(ANY).put("en", "hello").put("fr", "bonjour").build()).isEqualTo("hello");
        assertThat(new TextBuilder(ANY).put("fr", "bonjour").put("en", "hello").build()).isEqualTo("hello");
        assertThat(new TextBuilder(ANY).put("fr", "bonjour").put("aa", "hello").build()).isEqualTo("bonjour");

        assertThatNullPointerException().isThrownBy(() -> new TextBuilder(null));
        assertThatNullPointerException().isThrownBy(() -> new TextBuilder(ANY).put(null, "hello"));
        assertThatNullPointerException().isThrownBy(() -> new TextBuilder(ANY).put("en", "hello").build(null));

        assertThat(new TextBuilder(parse("fr")).put("en", "hello").put("fr", "bonjour").build()).isEqualTo("bonjour");
        assertThat(new TextBuilder(parse("en")).put("fr", "bonjour").put("en", "hello").build()).isEqualTo("hello");
    }

    @Test
    public void testBlankText() {
        assertThat(new TextBuilder(ANY).put("en", "").put("de", "Gewinn- und Verlustrechnung").build())
                .describedAs("No language priority should return first non blank")
                .isEqualTo("Gewinn- und Verlustrechnung");

        assertThat(new TextBuilder(parse("en")).put("en", "").put("de", "Gewinn- und Verlustrechnung").build())
                .describedAs("Specified language priority should return any text even if blank")
                .isEqualTo("");
    }
}
