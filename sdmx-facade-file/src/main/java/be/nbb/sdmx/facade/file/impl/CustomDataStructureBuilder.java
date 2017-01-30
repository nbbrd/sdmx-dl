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
package be.nbb.sdmx.facade.file.impl;

import be.nbb.sdmx.facade.Codelist;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dimension;
import be.nbb.sdmx.facade.ResourceRef;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import be.nbb.sdmx.facade.file.SdmxDecoder.FileType;
import java.util.HashMap;

/**
 *
 * @author Philippe Charles
 */
final class CustomDataStructureBuilder {

    private final LinkedHashMap<String, Set<String>> dimensions = new LinkedHashMap();
    private final LinkedHashMap<String, Set<String>> attributes = new LinkedHashMap();
    private FileType fileType = FileType.UNKNOWN;
    private String refId = null;
    private String timeDimensionId = null;
    private String primaryMeasureId = null;

    @Nonnull
    public CustomDataStructureBuilder dimension(@Nonnull String concept, @Nonnull String value) {
        putMulti(dimensions, concept, value);
        return this;
    }

    @Nonnull
    public CustomDataStructureBuilder attribute(@Nonnull String concept, @Nonnull String value) {
        putMulti(attributes, concept, value);
        return this;
    }

    @Nonnull
    public CustomDataStructureBuilder fileType(@Nonnull FileType fileType) {
        this.fileType = fileType;
        return this;
    }

    @Nonnull
    public CustomDataStructureBuilder refId(@Nonnull String refId) {
        this.refId = refId;
        return this;
    }

    @Nonnull
    public CustomDataStructureBuilder timeDimensionId(@Nullable String timeDimensionId) {
        this.timeDimensionId = timeDimensionId;
        return this;
    }

    @Nonnull
    public CustomDataStructureBuilder primaryMeasureId(@Nullable String primaryMeasureId) {
        this.primaryMeasureId = primaryMeasureId;
        return this;
    }

    @Nonnull
    public DataStructure build() {
        return DataStructure.builder()
                .dataStructureRef(ref(refId))
                .dimensions(guessDimensions())
                .name(refId)
                .timeDimensionId(timeDimensionId != null ? timeDimensionId : guessTimeDimensionId())
                .primaryMeasureId(primaryMeasureId != null ? primaryMeasureId : guessPrimaryMeasureId())
                .build();
    }

    private String guessTimeDimensionId() {
        return attributes.containsKey("TIME_FORMAT") ? "TIME_FORMAT" : dimensions.containsKey("FREQ") ? "FREQ" : "";
    }

    private String guessPrimaryMeasureId() {
        return "OBS_VALUE";
    }

    private Set<Dimension> guessDimensions() {
        Set<Dimension> result = new LinkedHashSet<>();
        int position = 1;
        boolean needsFiltering = fileType.equals(FileType.COMPACT20) || fileType.equals(FileType.COMPACT21);
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
        for (String o : item.getValue()) {
            if (WHITE_SPACE_PATTERN.matcher(o).find()) {
                return true;
            }
        }
        return false;
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

    static ResourceRef ref(String id) {
        return ResourceRef.of(null, id, null);
    }

    static Dimension dimension(String name, int pos, String... values) {
        return dimension(name, pos, Arrays.asList(values));
    }

    static Dimension dimension(String name, int pos, Iterable<String> values) {
        Map<String, String> codes = new HashMap<>();
        for (String o : values) {
            codes.put(o, o);
        }
        return Dimension.of(name, pos, Codelist.of(ref(name), codes), name);
    }
}
