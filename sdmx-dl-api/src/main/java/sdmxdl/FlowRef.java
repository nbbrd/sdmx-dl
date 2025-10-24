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

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import org.jspecify.annotations.Nullable;

/**
 * Identifier of a data flow used in a data (or meta data) query.
 * <p>
 * The syntax is agency id, artefact id, version, separated by a “,”. For
 * example: AGENCY_ID,FLOW_ID,VERSION
 * <p>
 * In case the string only contains one out of these 3 elements, it is
 * considered to be the flow id, i.e. ALL,FLOW_ID,LATEST
 * <p>
 * In case the string only contains two out of these 3 elements, they are
 * considered to be the agency id and the flow id, i.e. AGENCY_ID,FLOW_ID,LATEST
 *
 * @author Philippe Charles
 */
@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.EqualsAndHashCode(callSuper = false)
public class FlowRef extends ResourceRef<FlowRef> {

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
    public static @NonNull FlowRef parse(@NonNull CharSequence input) throws IllegalArgumentException {
        return create(input, FlowRef::new);
    }

    @StaticFactoryMethod
    public static @NonNull FlowRef of(@Nullable String agency, @NonNull String id, @Nullable String version) throws IllegalArgumentException {
        return of(agency, id, version, FlowRef::new);
    }
}
