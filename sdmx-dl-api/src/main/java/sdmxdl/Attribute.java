/*
 * Copyright 2020 National Bank of Belgium
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

import org.jspecify.annotations.Nullable;

/**
 * Statistical concept providing qualitative information about a specific statistical object.
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.EqualsAndHashCode(callSuper = false)
public class Attribute extends Component {

    @lombok.NonNull
    String id;

    @lombok.NonNull
    String name;

    @Nullable
    Codelist codelist;

    @lombok.NonNull
    @lombok.Builder.Default
    AttributeRelationship relationship = AttributeRelationship.UNKNOWN;

    public static final class Builder extends Component.Builder<Attribute.Builder> {
    }
}
