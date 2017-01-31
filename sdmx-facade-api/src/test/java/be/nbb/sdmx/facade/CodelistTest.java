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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class CodelistTest {

    @Test
    public void testBuilder() {
        final CodelistRef someRef = CodelistRef.of(null, "myResourceId", null);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                Codelist.builder().build();
            }
        }).as("Codelist#getRef() must be non-null")
                .isInstanceOf(NullPointerException.class).hasMessage("ref");

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                Codelist.builder().ref(null).build();
            }
        }).as("Codelist#getRef() must be non-null")
                .isInstanceOf(NullPointerException.class).hasMessage("ref");

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                Codelist.builder().ref(someRef).codes(null).build();
            }
        }).as("Codelist#getCodes() must be non-null")
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                Codelist.builder().ref(someRef).build().getCodes().put("hello", "world");
            }
        }).as("Codelist#getCodes() must return immutable map")
                .isInstanceOf(UnsupportedOperationException.class);

        assertThat(Codelist.builder().ref(someRef).build())
                .hasFieldOrPropertyWithValue("ref", someRef)
                .hasNoNullFieldsOrProperties();
        assertThat(Codelist.builder().ref(someRef).code("hello", "world").build().getCodes())
                .containsEntry("hello", "world")
                .hasSize(1);
    }
}
