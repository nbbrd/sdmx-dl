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
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.StringValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a language priority list. This class is an immutable convenient
 * wrapper around list of Locale.LanguageRange. It is designed to be used
 * directly in the "Accept-Language" header of an HTTP request.
 *
 * @author Philippe Charles
 * @see Locale.LanguageRange
 * @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Language
 * @see https://github.com/sdmx-twg/sdmx-rest/wiki/HTTP-content-negotiation
 */
@StringValue
@lombok.EqualsAndHashCode
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LanguagePriorityList {

    /**
     * Any language.
     */
    public static final LanguagePriorityList ANY = LanguagePriorityList.parse("*");

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
    @NonNull
    public static LanguagePriorityList parse(@NonNull CharSequence ranges) throws IllegalArgumentException {
        return new LanguagePriorityList(Locale.LanguageRange.parse(ranges.toString()));
    }

    @NonNull
    public static Optional<LanguagePriorityList> tryParse(@NonNull String ranges) {
        try {
            return Optional.of(parse(ranges));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
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

    private static String asString(List<Locale.LanguageRange> list) {
        return list.stream()
                .map(LanguagePriorityList::asString)
                .collect(Collectors.joining(","));
    }

    private static String asString(Locale.LanguageRange o) {
        return o.getRange() + (o.getWeight() != 1.0 ? (";q=" + o.getWeight()) : "");
    }
}
