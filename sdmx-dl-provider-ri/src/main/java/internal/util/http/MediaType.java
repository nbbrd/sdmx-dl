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
package internal.util.http;

import nbbrd.design.RepresentableAsString;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

import static java.util.Collections.*;

/**
 * @author Philippe Charles
 */
@RepresentableAsString
@lombok.AllArgsConstructor
@lombok.EqualsAndHashCode
@lombok.Getter
public final class MediaType {

    public static @NonNull MediaType parse(@NonNull CharSequence text) throws IllegalArgumentException {
        String input = text.toString();

        if (isEmptyOrTrimable(input)) {
            throw new IllegalArgumentException("Content is empty or trimable");
        }

        int subtypeIndex = input.indexOf('/');
        if (subtypeIndex == -1) {
            throw new IllegalArgumentException("Missing subtype");
        }
        String type = input.substring(0, subtypeIndex).toLowerCase();
        if (isEmptyOrTrimable(type)) {
            throw new IllegalArgumentException("Type is empty or trimable");
        }

        int paramsIndex = input.indexOf(';', subtypeIndex);
        String subType = input.substring(subtypeIndex + 1, paramsIndex != -1 ? paramsIndex : input.length()).toLowerCase();
        if (isEmptyOrTrimable(subType)) {
            throw new IllegalArgumentException("Subtype is empty or trimable");
        }

        if (paramsIndex == -1) {
            return new MediaType(type, subType, emptyMap());
        }
        Map<String, Collection<String>> parameters = new HashMap<>();
        for (String parameter : input.substring(paramsIndex + 1).split(";", -1)) {
            String[] keyValuePair = parameter.split("=", -1);
            if (keyValuePair.length != 2) {
                throw new IllegalArgumentException("Invalid key-value pair");
            }
            parameters
                    .computeIfAbsent(cleanParameter(keyValuePair[0]), o -> new ArrayList<>())
                    .add(cleanParameter(keyValuePair[1]));
        }
        return new MediaType(type, subType, unmodifiableMap(parameters));
    }

    @VisibleForTesting
    static final String WILDCARD = "*";

    @VisibleForTesting
    static final String CHARSET_PARAMETER = "charset";

    public static final MediaType ANY_TYPE = new MediaType(WILDCARD, WILDCARD, emptyMap());

    /**
     * The top-level media type.
     */
    @lombok.NonNull
    private final String type;

    /**
     * The media subtype.
     */
    @lombok.NonNull
    private final String subtype;

    /**
     * The parameters of this media type.
     */
    @lombok.NonNull
    private final Map<String, Collection<String>> parameters;

    public boolean isCompatible(@NonNull MediaType other) {
        return (other.type.equals(WILDCARD) || other.type.equals(this.type))
                && (other.subtype.equals(WILDCARD) || other.subtype.equals(this.subtype))
                && containsAll(this.parameters, other.parameters);
    }

    public boolean isCompatibleWithoutParameters(@NonNull MediaType other) {
        return (other.type.equals(WILDCARD) || other.type.equals(this.type))
                && (other.subtype.equals(WILDCARD) || other.subtype.equals(this.subtype));
    }

    /**
     * Returns an optional charset from the parameters if it is available.
     *
     * @return a non-null optional charset
     */
    public @NonNull Optional<Charset> getCharset() {
        Collection<String> charsets = parameters.get(CHARSET_PARAMETER);
        return charsets != null ? findFirstCharset(charsets) : Optional.empty();
    }

    /**
     * Returns a copy of this media type without its parameters.
     *
     * @return a non-null instance
     */
    public @NonNull MediaType withoutParameters() {
        return parameters.isEmpty() ? this : new MediaType(type, subtype, emptyMap());
    }

    /**
     * Returns a copy of this media type with a specific charset parameter.
     *
     * @param charset a non-null charset
     * @return a non-null instance
     */
    public @NonNull MediaType withCharset(@NonNull Charset charset) {
        Map<String, Collection<String>> result = new HashMap<>(parameters);
        result.put(CHARSET_PARAMETER, singletonList(charset.name()));
        return new MediaType(type, subtype, unmodifiableMap(result));
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder().append(type).append("/").append(subtype);
        parameters.forEach((k, v) -> v.forEach(o -> result.append("; ").append(k).append("=").append(o)));
        return result.toString();
    }

    private static boolean containsAll(Map<String, Collection<String>> l, Map<String, Collection<String>> r) {
        for (Entry<String, Collection<String>> entry : r.entrySet()) {
            Collection<String> values = l.get(entry.getKey());
            if (values == null || !values.containsAll(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static Optional<Charset> findFirstCharset(Collection<String> charsets) {
        return charsets.stream().map(Parser.onCharset()::parse).filter(Objects::nonNull).findFirst();
    }

    private static String cleanParameter(String input) {
        return input.toLowerCase(Locale.ROOT).trim();
    }

    private static boolean isEmptyOrTrimable(String o) {
        return o.isEmpty() || !o.trim().equals(o);
    }
}