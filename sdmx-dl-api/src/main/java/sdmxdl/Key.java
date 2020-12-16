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
import nbbrd.design.Immutable;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.StringValue;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Parameter that defines the dimension values of the data to be returned.
 *
 * @author Philippe Charles
 */
@Immutable
@StringValue
public final class Key {

    private static final String WILDCARD = "";
    private static final char CODE_SEP = '.';
    private static final String ALL_CODE = "all";

    public static final Key ALL = new Key(new String[]{WILDCARD});

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

    public boolean isWildcard(@NonNegative int index) throws IndexOutOfBoundsException {
        return WILDCARD.equals(items[index]);
    }

    private boolean isMultiValue(@NonNegative int index) throws IndexOutOfBoundsException {
        return items[index].contains("+");
    }

    public boolean isSeries() {
        for (int i = 0; i < items.length; i++) {
            if (isWildcard(i) || isMultiValue(i)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsKey(@NonNull Series series) {
        return contains(series.getKey());
    }

    public boolean contains(@NonNull Key input) {
        if (this == ALL) {
            return true;
        }
        if (size() != input.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!isWildcard(i) && !get(i).equals(input.get(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean supersedes(@NonNull Key that) {
        return !equals(that) && contains(that);
    }

    @Override
    public String toString() {
        return toString(items);
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
    @NonNull
    public static Key parse(@NonNull CharSequence input) {
        if (ALL_CODE.contentEquals(input)) {
            return ALL;
        }
        String[] result = Chars.splitToArray(input, CODE_SEP);
        for (int i = 0; i < result.length; i++) {
            result[i] = trimAndFixWildcard(result[i]);
        }
        return new Key(result);
    }

    @StaticFactoryMethod
    @NonNull
    public static Key of(@NonNull Collection<String> input) {
        if (input.isEmpty()) {
            return ALL;
        }
        return new Key(input
                .stream()
                .map(Key::trimAndFixWildcard)
                .toArray(String[]::new)
        );
    }

    @StaticFactoryMethod
    @NonNull
    public static Key of(@NonNull String... input) {
        if (input.length == 0) {
            return ALL;
        }
        String[] result = new String[input.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = trimAndFixWildcard(input[i]);
        }
        return new Key(result);
    }

    @NonNull
    public static Builder builder(@NonNull DataStructure dfs) {
        Map<String, Integer> index = new HashMap<>();
        dfs.getDimensions().forEach(o -> index.put(o.getId(), o.getPosition() - 1));
        return new Builder(index);
    }

    @NonNull
    public static Builder builder(@NonNull String... dimensions) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < dimensions.length; i++) {
            index.put(dimensions[i], i);
        }
        return new Builder(index);
    }

    public static final class Builder {

        private final Map<String, Integer> index;
        private final String[] items;

        private Builder(Map<String, Integer> index) {
            this.index = index;
            this.items = new String[index.size()];
            Arrays.fill(items, WILDCARD);
        }

        @NonNull
        public Builder put(@Nullable String id, @Nullable String value) {
            if (id != null) {
                Integer position = index.get(id);
                if (position != null) {
                    items[position] = value != null ? value : WILDCARD;
                }
            }
            return this;
        }

        @NonNull
        public Builder clear() {
            Arrays.fill(items, WILDCARD);
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
                if (WILDCARD.equals(item)) {
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
            return Key.toString(items);
        }

        @NonNegative
        public int size() {
            return items.length;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static String trimAndFixWildcard(String item) {
        if (item == null) {
            item = WILDCARD;
        } else {
            item = item.trim();
            switch (item) {
                case "*":
                case "+":
                    item = WILDCARD;
            }
        }
        return item;
    }

    private static String toString(String[] items) {
        return (items.length == 0 || (items.length == 1 && WILDCARD.equals(items[0])))
                ? ALL_CODE
                : Chars.join(CODE_SEP, items);
    }
    //</editor-fold>
}
