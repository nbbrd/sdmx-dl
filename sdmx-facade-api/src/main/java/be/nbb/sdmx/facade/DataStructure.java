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

import java.util.Set;

/**
 * Dataset Structure Definition (DSD) is a set of structural metadata associated
 * to a data set, which includes information about how concepts are associated
 * with the measures, dimensions, and attributes of a data cube, along with
 * information about the representation of data and related descriptive
 * metadata.
 *
 * @author Philippe Charles
 */
@lombok.Value(staticConstructor = "of")
public class DataStructure {

    /**
     * Non-null unique reference to this data structure.
     */
    @lombok.NonNull
    ResourceRef dataStructureRef;

    /**
     * Non-null list of statistical concepts used to identify a statistical
     * series or individual observations.
     */
    @lombok.NonNull
    Set<Dimension> dimensions;

    String name;

    String timeDimensionId;

    String primaryMeasureId;
}
