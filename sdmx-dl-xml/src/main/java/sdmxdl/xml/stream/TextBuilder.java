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
package sdmxdl.xml.stream;

import sdmxdl.LanguagePriorityList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
final class TextBuilder {

    @lombok.NonNull
    private final LanguagePriorityList ranges;
    private final Map<String, String> data = new LinkedHashMap<>();

    @NonNull
    public TextBuilder clear() {
        data.clear();
        return this;
    }

    @NonNull
    public TextBuilder put(@NonNull String lang, @Nullable String text) {
        Objects.requireNonNull(lang);
        if (text != null) {
            data.put(lang, text);
        }
        return this;
    }

    @Nullable
    public String build() {
        if (data.isEmpty()) {
            return null;
        }
        String lang = ranges.lookupTag(data.keySet());
        return lang != null ? data.get(lang) : data.values().iterator().next();
    }

    @NonNull
    public String build(@NonNull String defaultValue) {
        Objects.requireNonNull(defaultValue);
        String result = build();
        return result != null ? result : defaultValue;
    }
}
