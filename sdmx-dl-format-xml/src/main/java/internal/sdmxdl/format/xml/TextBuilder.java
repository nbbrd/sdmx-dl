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

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.Languages;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
final class TextBuilder {

    @lombok.NonNull
    private final Languages ranges;

    // this map preserves insertion order but is not sortable !
    private final Map<String, String> data = new LinkedHashMap<>();

    @NonNull
    public TextBuilder clear() {
        data.clear();
        return this;
    }

    @NonNull
    public TextBuilder put(@NonNull String lang, @Nullable String text) {
        if (text != null) {
            data.put(lang, text);
        }
        return this;
    }

    @Nullable
    public String build() {
        return ranges.select(data);
    }

    @NonNull
    public String build(@NonNull String defaultValue) {
        String result = build();
        return result != null ? result : defaultValue;
    }
}
