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
package sdmxdl;

import internal.sdmxdl.ResourceRefs;
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
public class DataStructureRef implements ResourceRef<DataStructureRef> {

    @lombok.NonNull
    String agency;

    @lombok.NonNull
    String id;

    @lombok.NonNull
    String version;

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
