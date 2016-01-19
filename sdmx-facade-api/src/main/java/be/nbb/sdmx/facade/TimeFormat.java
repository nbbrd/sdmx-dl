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
package be.nbb.sdmx.facade;

import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public enum TimeFormat {

    YEARLY,
    HALF_YEARLY,
    QUADRI_MONTHLY,
    QUARTERLY,
    MONTHLY,
    WEEKLY,
    DAILY,
    HOURLY,
    MINUTELY,
    UNDEFINED;

    @Nonnull
    public static TimeFormat parseByFrequencyCodeId(@Nonnull String input) {
        switch (input) {
            case "A":
                return YEARLY;
            case "S":
                return HALF_YEARLY;
            case "T":
                return QUADRI_MONTHLY;
            case "Q":
                return QUARTERLY;
            case "M":
                return MONTHLY;
            case "W":
                return WEEKLY;
            case "D":
                return DAILY;
            case "H":
                return HOURLY;
            case "I":
                return MINUTELY;
            default:
                return UNDEFINED;
        }
    }

    @Nonnull
    public static TimeFormat parseByTimeFormat(@Nonnull String input) {
        switch (input) {
            case "P1Y":
                return YEARLY;
            case "P6M":
                return HALF_YEARLY;
            case "P4M":
                return QUADRI_MONTHLY;
            case "P3M":
                return QUARTERLY;
            case "P1M":
                return MONTHLY;
            case "P7D":
                return WEEKLY;
            case "P1D":
                return DAILY;
//            case "???":
//                return HOURLY;
            case "PT1M":
                return MINUTELY;
            default:
                return UNDEFINED;
        }
    }
}
