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

import java.util.Collection;
import java.util.Comparator;

/**
 * Defines standard frequencies of time series.
 *
 * @author Philippe Charles
 */
public enum Frequency implements Comparable<Frequency> {

    ANNUAL,
    HALF_YEARLY,
    QUARTERLY,
    MONTHLY,
    WEEKLY,
    DAILY,
    DAILY_BUSINESS,
    HOURLY,
    MINUTELY,
    UNDEFINED;

    public boolean hasTime() {
        switch (this) {
            case HOURLY:
            case MINUTELY:
                return true;
            default:
                return false;
        }
    }

    public static Frequency getHighest(Collection<Series> data) {
        return data.stream()
                .map(Series::getFreq)
                .sorted(Comparator.reverseOrder())
                .findFirst()
                .orElse(Frequency.UNDEFINED);
    }
}
