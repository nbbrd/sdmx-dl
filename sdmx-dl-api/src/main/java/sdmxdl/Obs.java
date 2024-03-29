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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class Obs implements Comparable<Obs> {

    @lombok.NonNull
    TimeInterval period;

    double value;

    @lombok.NonNull
    @lombok.Singular("meta")
    Map<String, String> meta;

    @Override
    public int compareTo(Obs that) {
        return COMPARATOR.compare(this, that);
    }

    private LocalDateTime getStartTime() {
        return period.getStart();
    }

    private static final Comparator<Obs> COMPARATOR = Comparator.comparing(Obs::getStartTime).thenComparingDouble(Obs::getValue);
}
