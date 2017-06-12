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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class DimentionTest {

    final String someId = "dim1";
    final String someLabel = "Dim 1";

    @Test
    public void testBuilder() {
        assertThatThrownBy(Dimension.builder()::build)
                .as("Codelist#getId() must be non-null")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("id");

        assertThatThrownBy(Dimension.builder().id(null)::build)
                .as("Codelist#getId() must be non-null")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("id");

        assertThatThrownBy(Dimension.builder().id(someId).label(null)::build)
                .as("Codelist#getLabel() must be non-null")
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Dimension.builder().codes(null))
                .as("Codelist#getCodes() must be non-null")
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Dimension.builder().id(someId).label(someLabel).build().getCodes().put("hello", "world"))
                .as("Codelist#getCodes() must return immutable map")
                .isInstanceOf(UnsupportedOperationException.class);

        assertThat(Dimension.builder().id(someId).label(someLabel).build())
                .hasFieldOrPropertyWithValue("id", someId)
                .hasFieldOrPropertyWithValue("label", someLabel)
                .hasNoNullFieldsOrProperties();

        assertThat(Dimension.builder().id(someId).label(someLabel).code("hello", "world").build().getCodes())
                .containsEntry("hello", "world")
                .hasSize(1);
    }

    @Test
    public void testEquals() {
        assertThat(Dimension.builder().id("id").label("label").position(1).code("k1", "v1").code("k2", "v2").build())
                .isEqualTo(Dimension.builder().id("id").label("label").position(1).code("k2", "v2").code("k1", "v1").build());
    }
}
