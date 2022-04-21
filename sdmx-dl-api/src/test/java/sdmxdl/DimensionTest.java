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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("ConstantConditions")
public class DimensionTest {

    final String someId = "dim1";
    final String someLabel = "Dim 1";
    private final Codelist someCodelist = Codelist.builder().ref(CodelistRef.parse("ABC")).code("hello", "world").build();

    @Test
    @SuppressWarnings("null")
    public void testBuilder() {
        assertThatNullPointerException().isThrownBy(() -> Dimension.builder().build()).withMessageContaining("id");
        assertThatNullPointerException().isThrownBy(() -> Dimension.builder().id(null).build()).withMessageContaining("id");
        assertThatNullPointerException().isThrownBy(() -> Dimension.builder().id(someId).label(null).build()).withMessageContaining("label");

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> Dimension.builder().id(someId).label(someLabel).codelist(someCodelist).build().getCodes().put("hello", "world"));

        assertThat(Dimension.builder().id(someId).label(someLabel).codelist(someCodelist).build())
                .hasFieldOrPropertyWithValue("id", someId)
                .hasFieldOrPropertyWithValue("codelist", someCodelist)
                .hasFieldOrPropertyWithValue("label", someLabel)
                .hasFieldOrPropertyWithValue("position", 0);
    }

    @Test
    public void testEquals() {
        assertThat(Dimension.builder().id("id").label("label").position(1).codelist(someCodelist).build())
                .isEqualTo(Dimension.builder().id("id").label("label").position(1).codelist(someCodelist).build());
    }

    @Test
    public void testComparable() {
        Dimension d1a = Dimension.builder().id("id1a").label("label1").position(1).codelist(someCodelist).build();
        Dimension d1b = Dimension.builder().id("id1b").label("label1").position(1).codelist(someCodelist).build();
        Dimension d2 = Dimension.builder().id("id2").label("label2").position(2).codelist(someCodelist).build();

        assertThat(d1a).isEqualByComparingTo(d1a);
        assertThat(d1a).isLessThan(d2);
        assertThat(d2).isGreaterThan(d1a);
        assertThat(d1a).isLessThan(d1b);
        assertThat(d1b).isLessThan(d2);
    }
}
