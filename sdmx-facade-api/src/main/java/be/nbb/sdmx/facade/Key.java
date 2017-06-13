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
package be.nbb.sdmx.facade;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Parameter that defines the dimension values of the data to be returned.
 *
 * @author Philippe Charles
 */
@Immutable
public final class Key {

    private static final String WILDCARD = "";

    public static final Key ALL = new Key(new String[]{WILDCARD});

    private final String[] items;

    private Key(@Nonnull String[] items) {
        this.items = items;
    }

    @Nonnegative
    public int size() {
        return items.length;
    }

    @Nonnull
    public String get(@Nonnegative int index) throws IndexOutOfBoundsException {
        return items[index];
    }

    public boolean isWildcard(@Nonnegative int index) throws IndexOutOfBoundsException {
        return WILDCARD.equals(items[index]);
    }

    public boolean contains(@Nonnull Key input) {
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

    public boolean supersedes(@Nonnull Key that) {
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

    @Nonnull
    public static Key parse(@Nonnull String input) {
        return "all".equals(input.trim()) ? ALL : of(input.split("\\.", -1));
    }

    @Nonnull
    public static Key of(@Nonnull String... input) {
        if (input.length == 0) {
            return ALL;
        }
        String[] result = new String[input.length];
        for (int i = 0; i < result.length; i++) {
            String item = input[i];
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
            result[i] = item;
        }
        return new Key(result);
    }

    @Nonnull
    public static Builder builder(@Nonnull DataStructure dfs) {
        Map<String, Integer> index = new HashMap<>();
        dfs.getDimensions().forEach(o -> index.put(o.getId(), o.getPosition() - 1));
        return new BuilderImpl(index);
    }

    @Nonnull
    public static Builder builder(@Nonnull String... dimensions) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < dimensions.length; i++) {
            index.put(dimensions[i], i);
        }
        return new BuilderImpl(index);
    }

    public interface Builder {

        @Nonnull
        Key build();

        @Nonnull
        Builder clear();

        @Nonnull
        String getItem(@Nonnegative int index) throws IndexOutOfBoundsException;

        boolean isDimension(@Nullable String id);

        @Nonnull
        Builder put(@Nullable String id, @Nullable String value);

        @Override
        public String toString();
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static String toString(String[] items) {
        if (items.length == 0 || (items.length == 1 && WILDCARD.equals(items[0]))) {
            return "all";
        }
        StringBuilder result = new StringBuilder();
        result.append(items[0]);
        for (int i = 1; i < items.length; i++) {
            result.append('.').append(items[i]);
        }
        return result.toString();
    }

    private static final class BuilderImpl implements Builder {

        private final Map<String, Integer> index;
        private final String[] items;

        private BuilderImpl(Map<String, Integer> index) {
            this.index = index;
            this.items = new String[index.size()];
            Arrays.fill(items, WILDCARD);
        }

        @Override
        public Builder put(String id, String value) {
            if (id != null) {
                Integer position = index.get(id);
                if (position != null) {
                    items[position] = value != null ? value : WILDCARD;
                }
            }
            return this;
        }

        @Override
        public Builder clear() {
            Arrays.fill(items, WILDCARD);
            return this;
        }

        @Override
        public String getItem(int index) throws IndexOutOfBoundsException {
            return items[index];
        }

        @Override
        public boolean isDimension(String id) {
            return index.containsKey(id);
        }

        @Override
        public Key build() {
            return Key.of(items);
        }

        @Override
        public String toString() {
            return Key.toString(items);
        }
    }
    //</editor-fold>
}
