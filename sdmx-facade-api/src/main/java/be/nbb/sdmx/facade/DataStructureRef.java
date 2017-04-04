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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Identifier of a data structure.
 *
 * @author Philippe Charles
 */
public final class DataStructureRef extends ResourceRef {

    private DataStructureRef(String agencyId, String id, String version) {
        super(agencyId, id, version);
    }

    @Nonnull
    public static DataStructureRef parse(@Nonnull String input) throws IllegalArgumentException {
        return ResourceRef.parse(input, DataStructureRef::new);
    }

    @Nonnull
    public static DataStructureRef of(@Nullable String agencyId, @Nonnull String flowId, @Nullable String version) throws IllegalArgumentException {
        return ResourceRef.of(agencyId, flowId, version, DataStructureRef::new);
    }
}
