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

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import org.jspecify.annotations.Nullable;

/**
 * Identifier of a data structure.
 *
 * @author Philippe Charles
 */
@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.EqualsAndHashCode(callSuper = false)
public class StructureRef extends ResourceRef<StructureRef> {

    @lombok.NonNull
    String agency;

    @lombok.NonNull
    String id;

    @lombok.NonNull
    String version;

    @Override
    public String toString() {
        return toString(this);
    }

    @StaticFactoryMethod
    public static @NonNull StructureRef parse(@NonNull CharSequence input) throws IllegalArgumentException {
        return create(input, StructureRef::new);
    }

    @StaticFactoryMethod
    public static @NonNull StructureRef of(@Nullable String agency, @NonNull String id, @Nullable String version) throws IllegalArgumentException {
        return of(agency, id, version, StructureRef::new);
    }
}
