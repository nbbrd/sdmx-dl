/*
 * Copyright 2015 National Bank of Belgium
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

import internal.sdmxdl.Chars;
import lombok.NonNull;
import nbbrd.design.*;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Parameter that defines the dimension values of the data to be returned.
 *
 * @author Philippe Charles
 */
@Immutable
@RepresentableAsString
public final class Key {

    private static final char SEP_CHAR = '.';
    private static final char OR_CHAR = '+';

    private static final String WILDCARD_CODE = "";

    @VisibleForTesting
    static final String ALL_KEYWORD = "all";

    public static final Key ALL = new Key(new String[]{WILDCARD_CODE});

    private final String[] items;

    private Key(@NonNull String[] items) {
        this.items = items;
    }

    @NonNegative
    public int size() {
        return items.length;
    }

    @NonNull
    public String get(@NonNegative int index) throws IndexOutOfBoundsException {
        return items[index];
    }

    public @NonNull Key with(@NonNull String item, @NonNegative int index) throws IndexOutOfBoundsException {
        String[] result = Arrays.copyOf(items, items.length);
        result[index] = parseCode(item);
        return new Key(result);
    }

    public boolean isWildcard(@NonNegative int index) throws IndexOutOfBoundsException {
        return isWildcardCode(items[index]);
    }

    private boolean isMulti(@NonNegative int index) throws IndexOutOfBoundsException {
        return isMultiCode(items[index]);
    }

    private boolean contains(@NonNegative int index, @NonNull String code) throws IndexOutOfBoundsException {
        return containsCode(items[index], code);
    }

    public boolean isSeries() {
        for (int i = 0; i < items.length; i++) {
            if (isWildcard(i) || isMulti(i)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsKey(@NonNull Series series) {
        return contains(series.getKey());
    }

    public boolean contains(@NonNull Key that) {
        if (this == ALL) {
            return true;
        }
        if (this.size() != that.size()) {
            return false;
        }
        for (int i = 0; i < this.size(); i++) {
            if (!this.isWildcard(i) && !this.contains(i, that.get(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean supersedes(@NonNull Key that) {
        return !equals(that) && contains(that);
    }

    @Nullable
    public String validateOn(@NonNull Structure dsd) {
        if (this == ALL) {
            return null;
        }

        List<Dimension> dimensions = dsd.getDimensions();

        if (dimensions.size() != size()) {
            return String.format(Locale.ROOT, "Expecting key '%s' to have %d dimensions instead of %d", this, dimensions.size(), size());
        }

        for (int i = 0; i < dimensions.size(); i++) {
            Dimension dimension = dimensions.get(i);
            if (dimension.isCoded()) {
                for (String code : Chars.splitToArray(get(i), OR_CHAR)) {
                    if (!isWildcardCode(code) && !dimension.getCodes().containsKey(code)) {
                        return String.format(Locale.ROOT, "Expecting key '%s' to have a known code at position %d for dimension '%s' instead of '%s'", this, i + 1, dimension.getId(), code);
                    }
                }
            }
        }

        return null;
    }

    public @NonNull Key expand(@NonNull Structure dsd) {
        if (equals(Key.ALL)) {
            String[] expanded = new String[dsd.getDimensions().size()];
            Arrays.fill(expanded, "");
            return Key.of(expanded);
        }
        return this;
    }

    @Override
    public String toString() {
        return formatToString(items);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(items);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Key && equals((Key) obj));
    }

    private boolean equals(Key that) {
        return Arrays.equals(this.items, that.items);
    }

    @StaticFactoryMethod
    public static @NonNull Key parse(@NonNull CharSequence input) {
        if (ALL_KEYWORD.contentEquals(input)) {
            return ALL;
        }
        String[] result = Chars.splitToArray(input.toString(), SEP_CHAR);
        for (int i = 0; i < result.length; i++) {
            result[i] = parseCode(result[i]);
        }
        return new Key(result);
    }

    @StaticFactoryMethod
    public static @NonNull Key of(@NonNull List<String> input) {
        if (input.isEmpty()) {
            return ALL;
        }
        return new Key(input
                .stream()
                .map(Key::parseCode)
                .toArray(String[]::new)
        );
    }

    @StaticFactoryMethod
    public static @NonNull Key of(@NonNull String... input) {
        if (input.length == 0) {
            return ALL;
        }
        String[] result = new String[input.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = parseCode(input[i]);
        }
        return new Key(result);
    }

    @NonNull
    public static Builder builder(@NonNull Structure dfs) {
        List<Dimension> dimensions = dfs.getDimensions();
        Map<String, Integer> result = new HashMap<>();
        for (int i = 0; i < dimensions.size(); i++) {
            result.put(dimensions.get(i).getId(), i);
        }
        return new Builder(result);
    }

    @NonNull
    public static Builder builder(@NonNull List<String> dimensionNames) {
        Map<String, Integer> result = new HashMap<>();
        for (int i = 0; i < dimensionNames.size(); i++) {
            result.put(dimensionNames.get(i), i);
        }
        return new Builder(result);
    }

    public static final class Builder {

        private final Map<String, Integer> index;
        private final String[] items;

        private Builder(Map<String, Integer> index) {
            this.index = index;
            this.items = new String[index.size()];
            Arrays.fill(items, WILDCARD_CODE);
        }

        @NonNull
        public Builder put(@Nullable String id, @Nullable String value) {
            if (id != null) {
                Integer position = index.get(id);
                if (position != null) {
                    items[position] = value != null ? value : WILDCARD_CODE;
                }
            }
            return this;
        }

        @NonNull
        public Builder clear() {
            Arrays.fill(items, WILDCARD_CODE);
            return this;
        }

        @NonNull
        public String getItem(@NonNegative int index) throws IndexOutOfBoundsException {
            return items[index];
        }

        public boolean isDimension(@Nullable String id) {
            return index.containsKey(id);
        }

        public boolean isSeries() {
            for (String item : items) {
                if (WILDCARD_CODE.equals(item)) {
                    return false;
                }
            }
            return true;
        }

        @NonNull
        public Key build() {
            return Key.of(items);
        }

        public String toString() {
            return Key.formatToString(items);
        }

        @NonNegative
        public int size() {
            return items.length;
        }
    }

    private static String parseCode(String code) {
        if (code == null) {
            return WILDCARD_CODE;
        }
        code = code.trim();
        switch (code.length()) {
            case 0:
                return WILDCARD_CODE;
            case 1:
                char c = code.charAt(0);
                return c == '*' || c == OR_CHAR ? WILDCARD_CODE : code;
            default:
                return isMultiCode(code) ? reorderMultiCode(code) : code;
        }
    }

    private static boolean isWildcardCode(String code) {
        return WILDCARD_CODE.equals(code);
    }

    private static boolean isMultiCode(String code) throws IndexOutOfBoundsException {
        return Chars.contains(code, OR_CHAR);
    }

    private static boolean containsCode(String multiCode, String code) throws IndexOutOfBoundsException {
        int index = multiCode.indexOf(code);
        if (index == Chars.NOT_FOUND) return false;
        int left = index - 1;
        int right = index + code.length();
        return (left < 0 || multiCode.charAt(left) == OR_CHAR)
                && (right >= multiCode.length() || multiCode.charAt(right) == OR_CHAR);
    }

    private static String reorderMultiCode(String multiCode) {
        String[] result = Chars.splitToArray(multiCode, OR_CHAR);
        Arrays.sort(result);
        return String.join(String.valueOf(OR_CHAR), result);
    }

    private static String formatToString(String[] codes) {
        return isAll(codes) ? ALL_KEYWORD : Chars.join(SEP_CHAR, codes);
    }

    private static boolean isAll(String[] codes) {
        return codes.length == 0 || (codes.length == 1 && isWildcardCode(codes[0]));
    }
}
