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
package be.nbb.sdmx.facade;

import internal.util.ResourceRefs;
import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Identifier of a data structure.
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataStructureRef implements ResourceRef {

    @lombok.NonNull
    private String agency;

    @lombok.NonNull
    private String id;

    @lombok.NonNull
    private String version;

    public boolean containsRef(@NonNull DataStructure that) {
        return contains(that.getRef());
    }

    public boolean contains(@NonNull DataStructureRef that) {
        return (this.agency.equals(ALL_AGENCIES) || this.agency.equals(that.agency))
                && (this.id.equals(that.id))
                && (this.version.equals(LATEST_VERSION) || this.version.equals(that.version));
    }

    public boolean equalsRef(@NonNull DataStructure that) {
        return equals(that.getRef());
    }

    @Override
    public String toString() {
        return ResourceRefs.toString(this);
    }

    @NonNull
    public static DataStructureRef parse(@NonNull String input) throws IllegalArgumentException {
        return ResourceRefs.parse(input, DataStructureRef::new);
    }

    @NonNull
    public static DataStructureRef of(@Nullable String agency, @NonNull String id, @Nullable String version) throws IllegalArgumentException {
        return ResourceRefs.of(agency, id, version, DataStructureRef::new);
    }
}
