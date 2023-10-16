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
package sdmxdl;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a language priority list. This class is an immutable convenient
 * wrapper around list of Locale.LanguageRange. It is designed to be used
 * directly in the "Accept-Language" header of an HTTP request.
 *
 * @author Philippe Charles
 * @see Locale.LanguageRange
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Language">https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Language</a>
 * @see <a href="https://github.com/sdmx-twg/sdmx-rest/wiki/HTTP-content-negotiation">https://github.com/sdmx-twg/sdmx-rest/wiki/HTTP-content-negotiation</a>
 */
@RepresentableAsString
@lombok.EqualsAndHashCode
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Languages {

    public static final String ANY_KEYWORD = "*";

    /**
     * Any language.
     */
    public static final Languages ANY = Languages.parse(ANY_KEYWORD);

    /**
     * Parses the given ranges to generate a priority list.
     *
     * @param ranges a non-null list of comma-separated language ranges or a
     *               list of language ranges in the form of the "Accept-Language" header
     *               defined in <a href="http://tools.ietf.org/html/rfc2616">RFC 2616</a>
     * @return a non-null priority list
     * @throws NullPointerException     if {@code ranges} is null
     * @throws IllegalArgumentException if a language range or a weight found in
     *                                  the given {@code ranges} is ill-formed
     */
    @StaticFactoryMethod
    public static @NonNull Languages parse(@NonNull CharSequence ranges) throws IllegalArgumentException {
        return new Languages(Locale.LanguageRange.parse(ranges.toString()));
    }

    private final List<Locale.LanguageRange> list;

    /**
     * Returns the best-matching language tag using the lookup mechanism defined
     * in RFC 4647.
     *
     * @param tags a non-null list of language tags used for matching
     * @return the best matching language tag chosen based on priority or
     * weight, or {@code null} if nothing matches.
     * @throws NullPointerException if {@code tags} is {@code null}
     */
    @Nullable
    public String lookupTag(@NonNull Collection<String> tags) {
        return Locale.lookupTag(list, tags);
    }

    @Override
    public String toString() {
        return asString(list);
    }

    public @Nullable String select(@NonNull Map<String, String> data) {
        if (data.isEmpty()) {
            return null;
        }
        String lang = lookupTag(data.keySet());
        return lang != null ? data.get(lang) : getFallbackValue(data);
    }

    private static String getFallbackValue(Map<String, String> data) {
        if (!(data instanceof SortedMap)) {
            String result = data.get(FALLBACK_LANG);
            if (isNonBlank(result)) return result;
        }
        return getFirstNonBlankValue(data);
    }

    private static final String FALLBACK_LANG = "en";

    @MightBePromoted
    private static boolean isNonBlank(@Nullable String value) {
        return value != null && !value.isEmpty();
    }

    private static String getFirstNonBlankValue(Map<String, String> data) {
        return data
                .values()
                .stream()
                .filter(Languages::isNonBlank)
                .findFirst()
                .orElse(null);
    }

    private static String asString(List<Locale.LanguageRange> list) {
        return list.stream()
                .map(Languages::asString)
                .collect(Collectors.joining(","));
    }

    private static String asString(Locale.LanguageRange o) {
        return o.getRange() + (o.getWeight() != 1.0 ? (";q=" + o.getWeight()) : "");
    }
}
