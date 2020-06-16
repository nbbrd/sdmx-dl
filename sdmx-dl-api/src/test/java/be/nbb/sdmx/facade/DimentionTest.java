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
package be.nbb.sdmx.facade;

import static be.nbb.sdmx.facade.Dimension.builder;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class DimentionTest {

    final String someId = "dim1";
    final String someLabel = "Dim 1";

    @Test
    @SuppressWarnings("null")
    public void testBuilder() {
        assertThatNullPointerException().isThrownBy(() -> builder().build()).withMessageContaining("id");
        assertThatNullPointerException().isThrownBy(() -> builder().id(null).build()).withMessageContaining("id");
        assertThatNullPointerException().isThrownBy(() -> builder().id(someId).label(null).build()).withMessageContaining("label");
        assertThatNullPointerException().isThrownBy(() -> builder().codes(null));

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> builder().id(someId).label(someLabel).build().getCodes().put("hello", "world"));

        assertThat(builder().id(someId).label(someLabel).build())
                .hasFieldOrPropertyWithValue("id", someId)
                .hasFieldOrPropertyWithValue("label", someLabel)
                .hasNoNullFieldsOrProperties();

        assertThat(builder().id(someId).label(someLabel).code("hello", "world").build().getCodes())
                .containsEntry("hello", "world")
                .hasSize(1);
    }

    @Test
    public void testEquals() {
        assertThat(builder().id("id").label("label").position(1).code("k1", "v1").code("k2", "v2").build())
                .isEqualTo(builder().id("id").label("label").position(1).code("k2", "v2").code("k1", "v1").build());
    }
}
