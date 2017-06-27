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
package be.nbb.sdmx.facade.util;

import be.nbb.sdmx.facade.Frequency;
import static be.nbb.sdmx.facade.Frequency.DAILY;
import static be.nbb.sdmx.facade.Frequency.HALF_YEARLY;
import static be.nbb.sdmx.facade.Frequency.HOURLY;
import static be.nbb.sdmx.facade.Frequency.MINUTELY;
import static be.nbb.sdmx.facade.Frequency.MONTHLY;
import static be.nbb.sdmx.facade.Frequency.QUARTERLY;
import static be.nbb.sdmx.facade.Frequency.UNDEFINED;
import static be.nbb.sdmx.facade.Frequency.WEEKLY;
import javax.annotation.Nonnull;
import static be.nbb.sdmx.facade.Frequency.ANNUAL;
import static be.nbb.sdmx.facade.Frequency.DAILY_BUSINESS;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class FrequencyUtil {

    public final String FREQ_CONCEPT = "FREQ";
    public final String TIME_FORMAT_CONCEPT = "TIME_FORMAT";

    /**
     *
     * @param code
     * @return
     * @see
     * http://sdmx.org/wp-content/uploads/CL_FREQ_v2.0_update_April_2015.doc
     */
    @Nonnull
    public Frequency parseByFreq(@Nonnull String code) {
        switch (code.length()) {
            case 0:
                return Frequency.UNDEFINED;
            case 1:
                return parseByStandardFreq(code.charAt(0));
            default:
                Frequency base = parseByStandardFreq(code.charAt(0));
                return isMultiplier(code.substring(1)) ? base : Frequency.UNDEFINED;
        }
    }

    private Frequency parseByStandardFreq(char code) {
        switch (code) {
            case 'A':
                return ANNUAL;
            case 'S':
                return HALF_YEARLY;
            case 'Q':
                return QUARTERLY;
            case 'M':
                return MONTHLY;
            case 'W':
                return WEEKLY;
            case 'D':
                return DAILY;
            case 'H':
                return HOURLY;
            case 'B':
                return DAILY_BUSINESS;
            case 'N':
                return MINUTELY;
            default:
                return UNDEFINED;
        }
    }

    private boolean isMultiplier(String input) {
        try {
            return Integer.parseInt(input) > 1;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     *
     * @param code
     * @return
     * @see http://sdmx.org/wp-content/uploads/CL_TIME_FORMAT_1.0_2009.doc
     */
    @Nonnull
    public Frequency parseByTimeFormat(@Nonnull String code) {
        switch (code) {
            case "P1Y":
                return ANNUAL;
            case "P6M":
                return HALF_YEARLY;
            case "P3M":
                return QUARTERLY;
            case "P1M":
                return MONTHLY;
            case "P7D":
                return WEEKLY;
            case "P1D":
                return DAILY;
            case "PT1M":
                return MINUTELY;
            default:
                return UNDEFINED;
        }
    }
}
