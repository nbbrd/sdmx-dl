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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.AccessLevel;

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

    public boolean equalsRef(@Nonnull DataStructure that) {
        return equals(that.getRef());
    }

    @Override
    public String toString() {
        return ResourceRefs.toString(this);
    }

    @Nonnull
    public static DataStructureRef parse(@Nonnull String input) throws IllegalArgumentException {
        return ResourceRefs.parse(input, DataStructureRef::new);
    }

    @Nonnull
    public static DataStructureRef of(@Nullable String agency, @Nonnull String id, @Nullable String version) throws IllegalArgumentException {
        return ResourceRefs.of(agency, id, version, DataStructureRef::new);
    }
}
