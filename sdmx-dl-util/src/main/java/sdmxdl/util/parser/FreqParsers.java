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
package sdmxdl.util.parser;

import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.Frequency;

import static sdmxdl.Frequency.*;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class FreqParsers {

    @NonNull
    public Parser<Frequency> onFreqCodeList() {
        return FreqParsers::parseFreqCode;
    }

    @NonNull
    public Parser<Frequency> onTimeFormatCodeList() {
        return FreqParsers::parseTimeFormatCode;
    }

    /**
     * @param code
     * @return
     * @see http://sdmx.org/wp-content/uploads/CL_FREQ_v2.0_update_April_2015.doc
     */
    Frequency parseFreqCode(CharSequence code) {
        if (code == null) {
            return null;
        }
        switch (code.length()) {
            case 0:
                return null;
            case 1:
                return parseStandardFreqCode(code.charAt(0));
            default:
                Frequency base = parseStandardFreqCode(code.charAt(0));
                return isMultiplier(code.toString().substring(1)) ? base : null;
        }
    }

    private Frequency parseStandardFreqCode(char code) {
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
                return null;
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
     * @param code
     * @return
     * @see http://sdmx.org/wp-content/uploads/CL_TIME_FORMAT_1.0_2009.doc
     */
    Frequency parseTimeFormatCode(CharSequence code) {
        if (code == null) {
            return null;
        }
        switch (code.toString()) {
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
                return null;
        }
    }
}
