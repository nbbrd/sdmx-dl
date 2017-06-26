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
package be.nbb.sdmx.facade.xml.stream;

import be.nbb.sdmx.facade.LanguagePriorityList;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
final class TextBuilder {

    private final LanguagePriorityList ranges;
    private final Map<String, String> data = new HashMap<>();

    @Nonnull
    public TextBuilder clear() {
        data.clear();
        return this;
    }

    @Nonnull
    public TextBuilder put(@Nonnull String lang, @Nullable String text) {
        if (text != null) {
            data.put(lang, text);
        }
        return this;
    }

    @Nullable
    public String build() {
        String lang = ranges.lookupTag(data.keySet());
        if (lang != null) {
            return data.get(lang);
        }
        if (!data.isEmpty()) {
            return data.values().iterator().next();
        }
        return null;
    }

    @Nonnull
    public String build(@Nonnull String defaultValue) {
        String result = build();
        return result != null ? result : defaultValue;
    }
}
