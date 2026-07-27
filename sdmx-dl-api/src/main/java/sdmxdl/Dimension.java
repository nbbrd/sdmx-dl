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

import org.jspecify.annotations.Nullable;

/**
 * Statistical concept used in combination with other statistical concepts to identify a statistical series or individual observations.
 * <p>
 * Following the SDMX Information Model, a dimension's value representation
 * ({@code LocalRepresentation}) can be defined in two ways:
 * <ul>
 *     <li><b>Enumerated</b>: the dimension is linked to a {@link Codelist} that
 *     lists its allowed values. In this case {@link #getCodelist()} returns a
 *     non-null codelist and {@link #isCoded()} returns {@code true}.</li>
 *     <li><b>Non-enumerated</b>: the dimension has no codelist and its valid
 *     values are instead described by a text format specification (type such as
 *     String, Alpha or Numeric, minimum/maximum length, …). In this case
 *     {@link #getCodelist()} returns {@code null}, {@link #isCoded()} returns
 *     {@code false} and {@link #getCodes()} returns an empty map.</li>
 * </ul>
 * Note that the text format specification itself is not currently modeled; a
 * non-enumerated dimension is only distinguishable by the absence of a codelist.
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.EqualsAndHashCode(callSuper = false)
public class Dimension extends Component {

    @lombok.NonNull
    String id;

    @lombok.NonNull
    String name;

    /**
     * The codelist enumerating the allowed values of this dimension, or
     * {@code null} when the dimension is non-enumerated (i.e. described by a
     * text format instead of a codelist).
     */
    @Nullable
    Codelist codelist;

    public static final class Builder extends Component.Builder<Dimension.Builder> {
    }
}
