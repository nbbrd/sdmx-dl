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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class Obs implements Comparable<Obs> {

    @Nullable
    LocalDateTime period;

    @Nullable
    Double value;

    @lombok.NonNull
    @lombok.Singular("meta")
    Map<String, String> meta;

    @Override
    public int compareTo(Obs that) {
        return COMPARATOR.compare(this, that);
    }

    private static final Comparator<Obs> COMPARATOR = Comparator.comparing(Obs::getPeriod).thenComparing(Obs::getValue);
}
