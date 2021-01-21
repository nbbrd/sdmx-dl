/*
 * Copyright 2016 National Bank of Belgium
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
package sdmxdl.util;

import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public class Property<T> {

    @lombok.NonNull
    @lombok.Getter
    private final String key;

    private final T defaultValue;

    @lombok.NonNull
    private final Parser<T> parser;

    public @Nullable T get(@NonNull Map<?, ?> props) {
        Object value = props.get(key);
        if (value == null) return defaultValue;
        T result = parser.parse(value.toString());
        return result != null ? result : defaultValue;
    }
}
