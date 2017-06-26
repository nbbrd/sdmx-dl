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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.AccessLevel;

/**
 * Simple wrapper around list of Locale.LanguageRange.
 *
 * @author Philippe Charles
 */
@lombok.EqualsAndHashCode
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LanguagePriorityList {

    public static final LanguagePriorityList ANY = LanguagePriorityList.parse("*");

    /**
     * Parses the given input to generate a priority list.
     *
     * @param input a non-null input
     * @return a non-null priority list
     * @throws IllegalArgumentException
     * @see Locale.LanguageRange#parse(java.lang.String)
     */
    @Nonnull
    public static LanguagePriorityList parse(@Nonnull String input) throws IllegalArgumentException {
        return new LanguagePriorityList(Locale.LanguageRange.parse(input));
    }

    private final List<Locale.LanguageRange> ranges;

    /**
     *
     * @param tags
     * @return
     * @see Locale#lookupTag(java.util.List, java.util.Collection)
     */
    @Nullable
    public String lookupTag(@Nonnull Collection<String> tags) {
        return Locale.lookupTag(ranges, tags);
    }

    @Override
    public String toString() {
        return ranges.stream().
                map(LanguagePriorityList::asString)
                .collect(Collectors.joining(","));
    }

    private static String asString(Locale.LanguageRange o) {
        return o.getRange() + (o.getWeight() != 1.0 ? (";q=" + o.getWeight()) : "");
    }
}
