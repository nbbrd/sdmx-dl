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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("ConstantConditions")
public class DimentionTest {

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

        Assertions.assertThat(Dimension.builder().id(someId).label(someLabel).codelist(someCodelist).build())
                .hasFieldOrPropertyWithValue("id", someId)
                .hasFieldOrPropertyWithValue("codelist", someCodelist)
                .hasFieldOrPropertyWithValue("label", someLabel)
                .hasFieldOrPropertyWithValue("position", 0);
    }

    @Test
    public void testEquals() {
        Assertions.assertThat(Dimension.builder().id("id").label("label").position(1).codelist(someCodelist).build())
                .isEqualTo(Dimension.builder().id("id").label("label").position(1).codelist(someCodelist).build());
    }
}
