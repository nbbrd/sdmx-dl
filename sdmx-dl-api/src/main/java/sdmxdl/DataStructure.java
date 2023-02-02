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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * Dataset Structure Definition (DSD) is a set of structural metadata associated
 * to a data set, which includes information about how concepts are associated
 * with the measures, dimensions, and attributes of a data cube, along with
 * information about the representation of data and related descriptive
 * metadata.
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.EqualsAndHashCode(callSuper = false)
public class DataStructure extends Resource<DataStructureRef> implements HasName {

    /**
     * Non-null unique reference to this data structure.
     */
    @lombok.NonNull
    DataStructureRef ref;

    /**
     * Non-null list of statistical concepts used to identify a statistical
     * series or individual observations.
     */
    @lombok.NonNull
    @lombok.Singular
    SortedSet<Dimension> dimensions;

    /**
     * Non-null list of statistical concept providing qualitative information
     * about a specific statistical object
     */
    @lombok.NonNull
    @lombok.Singular
    Set<Attribute> attributes;

    String timeDimensionId;

    @lombok.NonNull
    String primaryMeasureId;

    @lombok.NonNull
    String name;

    @lombok.Getter(lazy = true)
    List<Dimension> dimensionList = new ArrayList<>(getDimensions());

    public static final class Builder {
    }
}
