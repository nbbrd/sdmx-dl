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

import org.checkerframework.checker.index.qual.NonNegative;

import java.util.Comparator;

/**
 * Statistical concept used in combination with other statistical concepts to identify a statistical series or individual observations.
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.EqualsAndHashCode(callSuper = false)
public class Dimension extends Component implements Comparable<Dimension> {

    @lombok.NonNull
    String id;

    @lombok.NonNull
    String label;

    @lombok.NonNull
    Codelist codelist;

    @NonNegative
    int position;

    @Override
    public int compareTo(Dimension that) {
        return COMPARATOR.compare(this, that);
    }

    public static final class Builder extends Component.Builder<Dimension.Builder> {
    }

    private static final Comparator<Dimension> COMPARATOR = Comparator.comparingInt(Dimension::getPosition).thenComparing(Dimension::getId);
}
