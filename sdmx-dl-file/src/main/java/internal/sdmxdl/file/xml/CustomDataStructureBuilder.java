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
package internal.sdmxdl.file.xml;

import sdmxdl.DataStructure;
import sdmxdl.DataStructureRef;
import sdmxdl.Dimension;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import internal.sdmxdl.file.SdmxDecoder.DataType;
import java.util.Collection;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
final class CustomDataStructureBuilder {

    private final LinkedHashMap<String, Set<String>> dimensions = new LinkedHashMap();
    private final LinkedHashMap<String, Set<String>> attributes = new LinkedHashMap();
    private DataType fileType = DataType.UNKNOWN;
    private DataStructureRef ref = null;
    private String timeDimensionId = null;
    private String primaryMeasureId = null;

    @NonNull
    public CustomDataStructureBuilder dimension(@NonNull String concept, @NonNull String value) {
        putMulti(dimensions, concept, value);
        return this;
    }

    @NonNull
    public CustomDataStructureBuilder attribute(@NonNull String concept, @NonNull String value) {
        putMulti(attributes, concept, value);
        return this;
    }

    @NonNull
    public CustomDataStructureBuilder fileType(@NonNull DataType fileType) {
        this.fileType = fileType;
        return this;
    }

    @NonNull
    public CustomDataStructureBuilder refId(@NonNull String refId) {
        return ref(DataStructureRef.of(null, refId, null));
    }

    @NonNull
    public CustomDataStructureBuilder ref(@NonNull DataStructureRef ref) {
        this.ref = ref;
        return this;
    }

    @NonNull
    public CustomDataStructureBuilder timeDimensionId(@Nullable String timeDimensionId) {
        this.timeDimensionId = timeDimensionId;
        return this;
    }

    @NonNull
    public CustomDataStructureBuilder primaryMeasureId(@Nullable String primaryMeasureId) {
        this.primaryMeasureId = primaryMeasureId;
        return this;
    }

    @NonNull
    public DataStructure build() {
        return DataStructure.builder()
                .ref(ref)
                .dimensions(guessDimensions())
                .label(ref.getId())
                .timeDimensionId(timeDimensionId != null ? timeDimensionId : "TIME_PERIOD")
                .primaryMeasureId(primaryMeasureId != null ? primaryMeasureId : "OBS_VALUE")
                .build();
    }

    private Set<Dimension> guessDimensions() {
        Set<Dimension> result = new LinkedHashSet<>();
        int position = 1;
        boolean needsFiltering = fileType.equals(DataType.COMPACT20) || fileType.equals(DataType.COMPACT21);
        for (Entry<String, Set<String>> item : dimensions.entrySet()) {
            if (needsFiltering && isAttribute(item)) {
                continue;
            }
            result.add(dimension(item.getKey(), position++, item.getValue()));
        }
        return result;
    }

    private boolean isAttribute(Entry<String, Set<String>> item) {
        if (item.getKey().contains("TITLE")) {
            return true;
        }
        return item.getValue().stream().anyMatch(o -> WHITE_SPACE_PATTERN.matcher(o).find());
    }

    private static final Pattern WHITE_SPACE_PATTERN = Pattern.compile("\\s+");

    private static void putMulti(Map<String, Set<String>> map, String key, String value) {
        Set<String> tmp = map.get(key);
        if (tmp == null) {
            tmp = new HashSet<>();
            map.put(key, tmp);
        }
        tmp.add(value);
    }

    static Dimension dimension(String name, int pos, String... values) {
        return dimension(name, pos, Arrays.asList(values));
    }

    static Dimension dimension(String name, int pos, Collection<String> values) {
        Dimension.Builder result = Dimension.builder()
                .id(name)
                .position(pos)
                .label(name);
        values.forEach(o -> result.code(o, o));
        return result.build();
    }
}
