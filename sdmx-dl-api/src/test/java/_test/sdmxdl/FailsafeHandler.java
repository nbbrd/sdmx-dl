/*
 * Copyright 2019 National Bank of Belgium
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
package _test.sdmxdl;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public final class FailsafeHandler implements BiConsumer<String, RuntimeException>, Consumer<String> {

    private final Queue<String> messages = new LinkedList<>();
    private final Queue<Exception> errors = new LinkedList<>();

    @Override
    public void accept(String msg, RuntimeException ex) {
        messages.add(Objects.requireNonNull(msg));
        errors.add(Objects.requireNonNull(ex));
    }

    @Override
    public void accept(String msg) {
        messages.add(Objects.requireNonNull(msg));
    }

    public void reset() {
        messages.clear();
        errors.clear();
    }

    public void assertEmpty() {
        assertThat(messages).isEmpty();
        assertThat(errors).isEmpty();
    }

    public void assertUnexpectedError(String msg, Class<? extends RuntimeException> ex) {
        assertThat(messages).singleElement().asString().containsIgnoringCase(msg);
        assertThat(errors).singleElement().isInstanceOf(ex);
    }

    public void assertUnexpectedNull(String msg) {
        assertThat(messages).singleElement().asString().containsIgnoringCase(msg);
        assertThat(errors).isEmpty();
    }
}
