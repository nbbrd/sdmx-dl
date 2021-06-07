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
package sdmxdl.util;

import nbbrd.design.StringValue;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Collections.emptyMap;

/**
 * @author Philippe Charles
 */
@StringValue
@lombok.AllArgsConstructor
public final class MediaType {

    public static @NonNull MediaType parse(@NonNull CharSequence text) throws IllegalArgumentException {
        String input = text.toString();

        if (isEmptyOrTrimable(input)) {
            throw new IllegalArgumentException();
        }

        int subtypeIndex = input.indexOf("/");
        if (subtypeIndex == -1) {
            throw new IllegalArgumentException();
        }
        String type = input.substring(0, subtypeIndex).toLowerCase();
        if (isEmptyOrTrimable(type)) {
            throw new IllegalArgumentException();
        }

        int paramsIndex = input.indexOf(";", subtypeIndex);
        String subType = input.substring(subtypeIndex + 1, paramsIndex != -1 ? paramsIndex : input.length()).toLowerCase();
        if (isEmptyOrTrimable(subType)) {
            throw new IllegalArgumentException();
        }

        if (paramsIndex == -1) {
            return new MediaType(type, subType, emptyMap());
        }
        Map<String, Collection<String>> parameters = new HashMap<>();
        for (String item : input.substring(paramsIndex + 1).trim().split(";", -1)) {
            String[] tmp = item.split("=", -1);
            if (tmp.length != 2) {
                throw new IllegalArgumentException();
            }
            parameters.computeIfAbsent(tmp[0].toLowerCase(), o -> new ArrayList()).add(tmp[1]);
        }
        return new MediaType(type, subType, parameters);
    }

    private static boolean isEmptyOrTrimable(String o) {
        return o.isEmpty() || !o.trim().equals(o);
    }

    public static final String WILDCARD = "*";

    @lombok.NonNull
    private final String type;

    @lombok.NonNull
    private final String subtype;

    @lombok.NonNull
    private final Map<String, Collection<String>> parameters;

    public boolean isCompatible(@NonNull MediaType other) {
        return (other.type.equals(WILDCARD) || other.type.equals(this.type))
                && (other.subtype.equals(WILDCARD) || other.subtype.equals(this.subtype))
                && containsAll(this.parameters, other.parameters);
    }

    @NonNull
    public MediaType withoutParameters() {
        return parameters.isEmpty() ? this : new MediaType(type, subtype, emptyMap());
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
}