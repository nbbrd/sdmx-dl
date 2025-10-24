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
package sdmxdl.provider.ext;

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.Parser;
import sdmxdl.Duration;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class TimeUnitParsers {

    @NonNull
    public Parser<Duration> onFreqCodeList() {
        return TimeUnitParsers::parseFreqCode;
    }

    @NonNull
    public Parser<Duration> onTimeFormatCodeList() {
        return TimeUnitParsers::parseTimeFormatCode;
    }

    // http://sdmx.org/wp-content/uploads/CL_FREQ_v2.0_update_April_2015.doc
    @VisibleForTesting
    Duration parseFreqCode(CharSequence code) {
        if (code == null) {
            return null;
        }
        switch (code.length()) {
            case 0:
                return null;
            case 1:
                return parseStandardFreqCode(code.charAt(0));
            default:
                Duration base = parseStandardFreqCode(code.charAt(0));
                return isMultiplier(code.toString().substring(1)) ? base : null;
        }
    }

    private Duration parseStandardFreqCode(char code) {
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

    // http://sdmx.org/wp-content/uploads/CL_TIME_FORMAT_1.0_2009.doc
    @VisibleForTesting
    Duration parseTimeFormatCode(CharSequence code) {
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

    @VisibleForTesting
    static final Duration ANNUAL = Duration.P1Y;
    @VisibleForTesting
    static final Duration HALF_YEARLY = Duration.P6M;
    @VisibleForTesting
    static final Duration QUARTERLY = Duration.P3M;
    @VisibleForTesting
    static final Duration MONTHLY = Duration.P1M;
    @VisibleForTesting
    static final Duration WEEKLY = Duration.P7D;
    @VisibleForTesting
    static final Duration DAILY = Duration.P1D;
    @VisibleForTesting
    static final Duration DAILY_BUSINESS = Duration.P1D;
    @VisibleForTesting
    static final Duration HOURLY = Duration.parse("PT1H");
    @VisibleForTesting
    static final Duration MINUTELY = Duration.parse("PT1M");
}
