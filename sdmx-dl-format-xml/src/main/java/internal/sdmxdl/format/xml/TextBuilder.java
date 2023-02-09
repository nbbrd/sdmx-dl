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
import nbbrd.design.MightBePromoted;
import nbbrd.design.VisibleForTesting;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.LanguagePriorityList;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
final class TextBuilder {

    @lombok.NonNull
    private final LanguagePriorityList ranges;
    private final Map<String, String> data = new TreeMap<>(ENGLISH_FIRST_THEN_LEXICOGRAPHICALLY);

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

    private static final Comparator<String> ENGLISH_FIRST_THEN_LEXICOGRAPHICALLY = getComparatorWithFixedValueFirst("en");

    @MightBePromoted
    @VisibleForTesting
    static Comparator<String> getComparatorWithFixedValueFirst(String fixedValue) {
        return (l, r) -> {
            boolean bl = l.compareTo(fixedValue) == 0;
            boolean br = r.compareTo(fixedValue) == 0;
            return bl ? (br ? 0 : -1) : (br ? 1 : l.compareTo(r));
        };
    }
}
